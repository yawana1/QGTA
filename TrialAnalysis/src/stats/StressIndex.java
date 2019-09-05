package stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.log4j.Logger;

import data.xml.objects.Trait;
import data.xml.objects.Traits;
import data.xml.objects.Trial;
import report.ReportOutputs;
import report.Summary;
import utils.Globals.CheckType;
import utils.Globals.StatGroup;
import utils.Globals.SummaryType;

public class StressIndex {

	private static Logger log = Logger.getLogger(StressIndex.class);
	
	private static final int Ma = 1;
	private static final int X = 75;

	public static void calcStressIndex(Trial trial, ReportOutputs reportOutputs, StatGroup group){
		double factor = 1d;  //default factor

		if(StatGroup.STRESS_INDEX_SILAGE.equals(group)){
			factor = .23;
		}
		else if(StatGroup.STRESS_INDEX_2YD_LA.equals(group) || StatGroup.STRESS_INDEX_2YD_EU.equals(group)){
			factor = .08;
		}
		
		calcStressIndex( trial, reportOutputs, factor);
	}
	
	public static void calcStressIndex(Trial trial, ReportOutputs reportOutputs, double factor){
		String effect = "estimate";
		try{
			Trait majorTrait = getTrait(trial);
			Collection<Summary> locYield = reportOutputs.get(majorTrait, SummaryType.locSummary);
			Collection<Summary> exlYield = reportOutputs.get(majorTrait, SummaryType.exlSummary);
			if(locYield == null || locYield.isEmpty()) return;
			if(exlYield ==null || exlYield.isEmpty()) return;
			String genoType = trial.getDataLevel().get("genoType");
			String env = trial.getDataLevel().get("environment");
			List<Object> genoIds = trial.getFbks().getColMap(genoType);
			List<Object> genoIndices = trial.getFbks().getColMap(genoType+"_index");
			List<Object> locIds = trial.getFbks().getColMap(env);
			List<Object> locIndices = trial.getFbks().getColMap(env+"_env");
			Trait trait = Traits.INSTANCE.get("stress_index");
			Double maxLoc = 0d;
			
			Map<String, Summary> cache = new HashMap<>();
			for(Summary summary : exlYield){
				cache.put(summary.getFilters().get(env) +","+summary.getFilters().get(genoType), summary);
			}
			
			// get location information
			Map<Integer, Double> mapLoc = new HashMap<Integer, Double>();
			for(Iterator<Object> lit = locIds.iterator(); lit.hasNext();){
				try{
					Object locObj = lit.next();
					Integer locIndex = GeneralStats.getGenoIndex(locIds,locIndices, locObj);
					LinkedHashMap<String, Integer> filter = new LinkedHashMap<String, Integer>();
					filter.put(env, locIndex);
					Summary summaryLoc = Summary.search(locYield, filter);
					Double value = getSummaryValue(summaryLoc, "estimate");
					if (value == 0d) 
						value = getSummaryValue(summaryLoc, "rawMean"); 
					if(maxLoc < value){
						maxLoc = value;
					}
					mapLoc.put(locIndex, value);
				}catch(Exception e){
					log.warn("",e);
				}
			}
			mapLoc = sortByValues(mapLoc);
			Collection<Summary> collection = new ArrayList<Summary>();
			
			// compute SI for each genoId
			SimpleRegression sr = new SimpleRegression(true);
			for(Iterator<Object> git = genoIds.iterator(); git.hasNext();){
				try{
					Object genoObj = git.next();
					Integer genoId = Integer.parseInt(genoObj.toString());
					Integer genoIndex = GeneralStats.getGenoIndex(genoIds,genoIndices, genoObj);
					LinkedHashMap<String, Integer> filter = new LinkedHashMap<String, Integer>();
					Iterator<Entry<Integer, Double>> lit = mapLoc.entrySet().iterator();
					while (lit.hasNext()) {
						Map.Entry<Integer, Double> pair = lit.next();
						Integer locIndex = pair.getKey();
						Double locEst = pair.getValue();
						filter.put(env, locIndex);
						filter.put(genoType, genoIndex);
						Summary summaryExl = cache.get(locIndex +","+genoIndex);
						Double genoEst = getSummaryValue(summaryExl, "estimate");
//						Double locEst = mapLoc.get(locIndex);
						if (genoEst == 0d) 
							genoEst = getSummaryValue(summaryExl, "rawMean");
						if(genoEst > 0d){
							sr.addData(locEst, genoEst);
						}
					}

					//need more then 2 point to draw the line.
					if(sr.getN() > 2){
						//sr.regress();
						Double Mi = sr.getSlope();
						Double Hi = sr.predict(maxLoc);
						if(Mi != null && Hi != null){
							Double si = X*(Ma - Mi) + (Hi - maxLoc)/factor;
							LinkedHashMap<String, java.lang.Integer> filters = new LinkedHashMap<>();
							filters.put(genoType, genoId);
							Summary summaryIndex = GeneralStats.createEntrySummary(trait, SummaryType.entrySummary, genoType, genoId, trial, filters);
							summaryIndex.getValues().put(effect, si);
							summaryIndex.getValues().put("rawCount", 1); //needed to calc % of check
							collection.add(summaryIndex);
						}
					}
				}catch(Exception e){
					log.warn("",e);
				}
				finally {
					sr.clear();
				}
			}
			if(collection.size()>0){
				Checks.getChecksDeviation(collection, trial.getFbks(), CheckType.AVG, effect, genoType);
				reportOutputs.get(trait, true).add(SummaryType.entrySummary, collection);
			}
		}catch (Exception e) {
			log.warn("",e);
		}
	}
	
	private static Trait getTrait(Trial trial){
		Trait trait = null;
		if(trial.getTraits().contains(Traits.INSTANCE.get("tons_acre"))){
			trait = Traits.INSTANCE.get("tons_acre");
		}else if(trial.getTraits().contains(Traits.INSTANCE.get("yield_tph"))){
			trait = Traits.INSTANCE.get("yield_tph");
		}else if(trial.getTraits().contains(Traits.INSTANCE.get("yield"))){
			trait = Traits.INSTANCE.get("yield");
		}else if(trial.getTraits().contains(Traits.INSTANCE.get("sil_yield_dm_tha"))){
			trait = Traits.INSTANCE.get("sil_yield_dm_tha");
		}
		return trait;
	}
	
	private static Double getSummaryValue(Summary summary, String column){
		Double result = 0d;
		if(summary != null){
			if(summary.getValues().get(column) != null){
				String value = summary.getValues().get(column).toString();
				result = Double.parseDouble(value);
			}
		}
		return result;
	}
	
	@SuppressWarnings("hiding")
	public static <Integer, Double extends Comparable<Double>> Map<Integer, Double> sortByValues(final Map<Integer, Double> map) {
	    Comparator<Integer> valueComparator =  new Comparator<Integer>() {
	        public int compare(Integer k1, Integer k2) {
	            int compare = map.get(k1).compareTo(map.get(k2));
	            if (compare == 0) return 1;
	            else return compare;
	        }
	    };
	    Map<Integer, Double> sortedByValues = new TreeMap<Integer, Double>(valueComparator);
	    sortedByValues.putAll(map);
	    return sortedByValues;
	}
}

