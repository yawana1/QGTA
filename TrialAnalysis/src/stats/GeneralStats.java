package stats;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import asreml.AsremlAsd;
import asreml.output.Tab;
import asreml.output.Tabs;
import data.collection.ExpFBK;
import data.collection.ExpFBKs;
import data.xml.objects.Experiment;
import data.xml.objects.Season;
import data.xml.objects.Trait;
import data.xml.objects.Traits;
import data.xml.objects.Trial;
import report.ReportOutputs;
import report.Summary;
import utils.Funcs;
import utils.Globals.SummaryType;
import utils.Globals.TrialType;
import utils.ObjectQuery;

/***
 * General Class that contains static methods for creating and grouping summaries
 * 
 * @author Scott Smith
 *
 */

public class GeneralStats {

	static Logger log = Logger.getLogger(GeneralStats.class.getName());

	/***
	 * Creates a list summaries filled in with the basic information needed.
	 * Creates a summary for each unique set of filter values
	 * 
	 * @param colMap
	 * @param trait
	 * @param type
	 * @param filters
	 * @param trial
	 * @return
	 */
	public static Collection<Summary> createSummaries(Map<String,List<Object>> colMap, Trait trait, SummaryType type, Collection<String> filters, Trial trial) {
		Collection<Summary> summaries = new ArrayList<Summary>();
		String genoType = trial.getDataLevel().get("genoType");
		String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(Calendar.getInstance().getTime());
		
		//create index list for all filters
		LinkedHashMap<String, List<Integer>> filterMap = new LinkedHashMap<String, List<Integer>>();
		for(String filter : filters){
			boolean isGrm = colMap.containsKey(filter+"_index");
			List<Integer> index = new ArrayList<Integer>(colMap.size());
			int size = colMap.get(filter).size();
			for(int x=1; x <= size; x++){
				if(isGrm){
					index.add((Integer)colMap.get(filter+AsremlAsd.INDEX_COLUMN_SUFFIX).get(x-1));
				}
				else{
					index.add(x);
				}
			}
			filterMap.put(filter, index);
		}

		//create cartesina product from all filters
		List<LinkedHashMap<String, Integer>> filterProduct = cartesianProduct(filterMap);
		
		//List of experiments if MULTI run
		List<data.xml.objects.Experiment> experiments = trial.getExperiments();
		if(experiments == null || experiments.size() == 0){
			experiments = GeneralStats.getExperiments(trial);
			trial.setExperiments(experiments);
		}
		
		for(LinkedHashMap<String, Integer> filter : filterProduct){
			Summary summary = new Summary(type, trait);

			summary.setFilters(filter);

			//set actually, unindexed, values on the summary
			for(Entry<String, Integer> mapEntry : summary.getFilters().entrySet()){
				String columnName = mapEntry.getKey();
				Integer index = mapEntry.getValue();
				boolean isGrm = colMap.containsKey(columnName+"_index");
				if(isGrm){
					index = colMap.get(columnName+"_index").indexOf(index) + 1;
				}
				try{
					summary.getValues().put(columnName, colMap.get(columnName).get(index-1));
				}
				catch(Exception e){
					log.warn("", e);
				}
			}

			//set basic values most summaries need
			summary.getValues().put("traitVatString", trait.getVatName());
			summary.getValues().put("genoTypeName", genoType);
			summary.getValues().put("experiment", trial.getTrialName());
			summary.getValues().put("seasonId", trial.getSeasonId());
			summary.getValues().put("seasonName", trial.getSeasonName());
			summary.getValues().put("timestamp", timestamp);
			
			//add in extra column's from VAT processing
			if(filters.contains("genoId")){
				ExpFBKs expFBKs  = trial.getFbks();
				Map<String, Object> filterSummary =  new HashMap<String, Object>();
				filterSummary.put("genoId", summary.getValues().get(genoType));
				ExpFBK fbk = expFBKs.getFirstFBK(filterSummary);
				
				if(fbk != null){
					summary.getValues().put("genoId", fbk.getValue("genoId"));
					summary.getValues().put("baseMale", fbk.getValue("baseMale"));
					summary.getValues().put("baseFemale", fbk.getValue("baseFemale"));
					summary.getValues().put("baseGeno", fbk.getValue("baseGeno"));
				}
			}
			
			//set multiyear Regions
			if(trial.getRegion() != null){
				summary.getValues().put("region", trial.getRegion());
			}
			else{
				if(experiments.size() > 0){
					summary.getValues().put("region", experiments.get(0).getRegion());
				}
			}
			
			//add in extra info used for excel sheet creation
			//leave out of normal as it is time expensive
			if(trial.getXlsColumnFile() != null){
				ExpFBKs expFBKs = trial.getFbks();
				if(filters.contains(genoType) && filters.size() == 1){
					int id = Integer.parseInt(summary.getValues().get(genoType).toString());
					ExpFBK expFBK = expFBKs.getFirstFBK(genoType, id);
					if(expFBK != null){
						summary.getValues().put("experimentName", expFBK.getValue("expName"));
						summary.getValues().put("experimentSeasonName", expFBK.getValue("seasonName"));
						summary.getValues().put("genoName", expFBK.getData().get("genoName"));
						summary.getValues().put("entryNum", expFBK.getData().get("entryNum"));
						if(expFBK.getData().containsKey("groupId")){
							summary.getValues().put("groupId", expFBK.getData().get("groupId"));
						}
					}
				}
	
				String environment = trial.getDataLevel().get("environment");
				if(filters.contains(environment) && filters.size() == 1){
					int id = Integer.parseInt(summary.getValues().get(environment).toString());
					ExpFBK expFBK = expFBKs.getFirstFBK(environment, id);
					if(expFBK != null){
						summary.getValues().put("locName", expFBK.getData().get("locationName"));

					}
				}
			}
			
			summaries.add(summary);
		}
		return summaries;
	}
	
	public static Summary createEntrySummary(Trait trait, SummaryType type, String genoType, Integer genoId, Trial trial, LinkedHashMap<String, Integer> filter){
		Summary summary = new Summary(type, trait);
		summary.getValues().put("traitVatString", trait.getVatName());
		summary.getValues().put("genoTypeName", genoType);
		summary.getValues().put("experiment", trial.getTrialName());
		summary.getValues().put("seasonId", trial.getSeasonId());
		summary.getValues().put("seasonName", trial.getSeasonName());
		summary.getValues().put(genoType, genoId);
		summary.setFilters(filter);
		String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(Calendar.getInstance().getTime());
		summary.getValues().put("timestamp", timestamp);
		return summary;
	}
	
	public static Summary createEntrySummary(Trait trait, SummaryType type, String genoType, Integer genoId, Trial trial){
		return createEntrySummary(trait, type, genoType, genoId, trial, null);
	}

	/***
	 * Pass in a Map of lists ie.
	 * 			id1 = list of id1 values
	 * 			id2 = list of id2 values
	 * 			id3 = list of id3 values
	 * Get out the cartesian Product of all those values in a list of a Map of ids
	 * @param <K>
	 * @param <V>
	 * @param lists
	 * @return
	 */
	public static <K,V> List<LinkedHashMap<K,V>> cartesianProduct(LinkedHashMap<K, List<V>> lists) {
		return cartesianProduct(0, lists);
	}

	/***
	 * Perform recursive cartesian product on values passed in.
	 * @param <K>
	 * @param <V>
	 * @param index
	 * @param lists
	 * @return
	 */
	private static <K,V> List<LinkedHashMap<K, V>> cartesianProduct(int index, LinkedHashMap<K, List<V>> lists) {
		List<LinkedHashMap<K, V>> ret = new ArrayList<LinkedHashMap<K, V>>();
		if (index == lists.size()) {
			ret.add(new LinkedHashMap<K,V>());
		} else {
			for (V obj : lists.get(lists.keySet().toArray()[index])) {
				for (LinkedHashMap<K,V> list : cartesianProduct(index+1, lists)) {
					List<K> keys = new ArrayList<K>(lists.keySet());
					list.put(keys.get(index),obj);

					//reorder map to keep filter order correct.
					LinkedHashMap<K,V> reorder = new LinkedHashMap<K, V>();
					reorder.put(keys.get(index),obj);
					reorder.putAll(list);
					ret.add(reorder);
				}
			}
		}
		return ret;
	}
	
	public static boolean isMultiYear(List<data.xml.objects.Experiment> experiments){
		boolean result = false;
		
		Season season = null;
		for(Experiment experiment : experiments){
			if(season == null){
				season = experiment.getSeason();
			}
			else if(!season.equals(experiment.getSeason())){
				result = true;
				break;
			}
		}
		
		return result;
	}
	
	public static List<Experiment> getExperiments(Trial trial){
		List<data.xml.objects.Experiment> result = new ArrayList<>();
		
		Collection<String> columns = new ArrayList<String>();
		columns.add("expName");
		columns.add("seasonId");
		columns.add("groupName");
		String columnsSql = Funcs.quote(columns, ",");
		String select = "SELECT " + columnsSql;
		String groupBy = " GROUP BY " + columnsSql; 
		List<Map<String, Object>> experiments = trial.getFbks().get(select, "", groupBy);

		for(Map<String,Object> experiment : experiments){
			String name = experiment.get("groupName").toString();
			data.xml.objects.Experiment e = new Experiment(experiment.get("expName").toString(),"", experiment.get("seasonId").toString()
					, name.substring(name.lastIndexOf("_")+1, name.length()));
			result.add(e);
		}
		
		return result;
	}

	/***
	 * Rank summaries by the rankByColumn values and direction from the trait.
	 * Rank summaries by group them by their filters.
	 * Example ExL needs to be ranked by grouping the summaries by location.
	 * @param summaries
	 * @param filters 
	 * @param genoType 
	 */
	public static void getRanks(Collection<Summary> summaries, String rankByColumn, LinkedHashMap<String, Integer> filters, String genoType){
		try{
			if(summaries.size() > 0){

				//no need to try and group when only one filter
				if(filters.size()==1){
					rank(summaries, rankByColumn);
				}
				else{

					//create groups of summaries by unquie filter combinatinos
					List<List<Summary>> groupedSummeries = groupByFiliter(summaries, filters, genoType);

					for(List<Summary> filteredSummaries : groupedSummeries){
						rank(filteredSummaries, rankByColumn);
					}
				}
			}
		}
		catch (Exception e) {
			log.warn("", e);
		}
	}

	/***
	 * Rank summaries by the rankByColumn values and direction from the trait.
	 * @param summaries
	 * @param filters 
	 * @param genoType 
	 */
	private static void rank(Collection<Summary> summaries, String rankByColumn){
		Trait trait = summaries.iterator().next().getTrait();
		List<Summary> list = new ArrayList<Summary>(summaries);
		Collections.sort(list, new SummaryComparator(trait.getDirection(),rankByColumn));
		for(int i = 0; i < summaries.size(); i++){
			Summary summary = list.get(i);
			summary.getValues().put("rank", (i+1));
		}
	}
	
	//TODO
	public static void rankByGroup(Collection<Summary> summaries, ExpFBKs fbks, String genoType){
		HashMap<Integer, List<Summary>> data = new HashMap<Integer, List<Summary>>();
		Trait trait = null;
		if(summaries.size() > 0) trait = summaries.iterator().next().getTrait();
		else return;
		for(Iterator<Summary> sit = summaries.iterator(); sit.hasNext();){
			try{
				Summary summary = sit.next();
				Integer entryId = Integer.parseInt(summary.getValues().get(genoType).toString());
//				Object entry = fbks.getColMap().get(genoType).get(entryIndex-1);
//				Integer entryId = entry == null ? null : Integer.parseInt(entry.toString());
				ExpFBK fbk = fbks.getFirstFBK(genoType, entryId);
				Integer groupId = fbk.getInt("groupId");
				summary.getValues().put("groupId", groupId);
				if(!data.containsKey(groupId)) data.put(groupId, new ArrayList<Summary>());
				data.get(groupId).add(summary);
			}catch(Exception e){
				log.warn(e.getMessage());
			}
		}
		for(Integer groupId : data.keySet()){
			try{
				List<Summary> lst = data.get(groupId);
				Collections.sort(lst, new SummaryComparator(trait.getDirection(), "estimate"));
				for(int i=0; i<lst.size(); i++){
					Summary summary = lst.get(i);
					summary.getValues().put("rank", (i+1));
				}
			}catch(Exception e){
				log.warn(e.getMessage());
			}
		}
	}
	
	public static void rankByGroup(Collection<Summary> summaries, ExpFBKs fbks, String genoType, String environment){
		HashMap<Integer, HashMap<Integer, List<Summary>>> data = new HashMap<Integer, HashMap<Integer,List<Summary>>>();
		Trait trait = null;
		if(summaries.size() > 0) trait = summaries.iterator().next().getTrait();
		else return;
		for(Iterator<Summary> sit = summaries.iterator(); sit.hasNext();){
			try{
				Summary summary = sit.next();
				Integer entryId = Integer.parseInt(summary.getValues().get(genoType).toString());
				Integer locId = Integer.parseInt(summary.getValues().get(environment).toString());
				Map<String,Object> filters = new HashMap<>();
				filters.put(genoType, entryId);
				filters.put(environment, locId);
				ExpFBK fbk = fbks.getFirstFBK(filters);
				Integer groupId = fbk.getInt("groupId");
				summary.getValues().put("groupId", groupId);
				if(!data.containsKey(locId)) data.put(locId, new HashMap<Integer, List<Summary>>());			
				if(!data.get(locId).containsKey(groupId)) data.get(locId).put(groupId, new ArrayList<Summary>());
				data.get(locId).get(groupId).add(summary);
			}catch(Exception e){
				log.warn(e.getMessage());
			}
		}
		for(Integer locId : data.keySet()){
			try{
				for(Integer groupId : data.get(locId).keySet()){
					try{
						List<Summary> lst = data.get(locId).get(groupId);
						Collections.sort(lst, new SummaryComparator(trait.getDirection(), "rawMean"));
						for(int i=0; i<lst.size(); i++){
							Summary summary = lst.get(i);
							summary.getValues().put("rank", (i+1));
						}
					}catch(Exception e){
						log.warn(e.getMessage());
					}
				}
			}catch(Exception e){
				log.warn(e.getMessage());
			}
		}
	}

	/***
	 * Set summary with the actual values of the filters on the summary from the colMap
	 * @param summaries
	 * @param colMap
	 */
	public static void getNonIndexedValues(Collection<Summary> summaries, Map<String,List<Object>> colMap) {
		for(Summary summary : summaries){
			getNonIndexedValues(summary, colMap);
		}
	}

	public static void getNonIndexedValues(Summary summary, Map<String,List<Object>> colMap) {
		for(String filter : summary.getFilters().keySet()){
			int index = summary.getFilters().get(filter)-1;
			summary.getValues().put(filter, colMap.get(filter).get(index));
		}
	}

	/***
	 * 
	 * @param summaries
	 * @param filters
	 * @param excludeFilter
	 * @return
	 */
	public static <T> List<List<Summary>> groupByFiliter(Collection<Summary> summaries, LinkedHashMap<String, T> filters, String excludeFilter){
		//deep copy filters to allow exclusions
		List<String> excludedFilterList = new ArrayList<String>(filters.keySet());
		Collections.copy(excludedFilterList, new ArrayList<String>(filters.keySet()));
		excludedFilterList.remove(excludeFilter);

		//maps for columns of unique ids
		Map<String,Set<Integer>> indexMap = new HashMap<String, Set<Integer>>();
		for(String filter : excludedFilterList){
			indexMap.put(filter, new HashSet<Integer>());
		}

		//get unique lists of each non genoType column.
		for(Summary summary : summaries){
			for(Entry<String, Integer> filter : summary.getFilters().entrySet()){
				if(!filter.getKey().equals(excludeFilter)){
					indexMap.get(filter.getKey()).add(filter.getValue());
				}
			}
		}

		LinkedHashMap<String, List<Integer>> productMap = new LinkedHashMap<String, List<Integer>>();
		for(String filter : excludedFilterList){
			productMap.put(filter, new ArrayList<Integer>(indexMap.get(filter)));
		}

		List<LinkedHashMap<String,Integer>> filterList = cartesianProduct(productMap);
		List<List<Summary>> groupedSummeries = new ArrayList<List<Summary>>();
		for(int i = 0; i < filterList.size(); i++){
			groupedSummeries.add(new ArrayList<Summary>());
		}


		for(Summary summary : summaries){
			for(int i = 0; i < filterList.size(); i++){
				LinkedHashMap<String, Integer> grouping = filterList.get(i);
				boolean addToList = true;
				for(Entry<String, Integer> entry : grouping.entrySet()){
					if(!summary.getFilters().get(entry.getKey()).equals(entry.getValue())){
						addToList = false;
						break;
					}
				}
				if(addToList){
					groupedSummeries.get(i).add(summary);
				}
			}
		}
		return groupedSummeries;
	}

	/***
	 * Copy value of one summary result to another.
	 * Ie. copy rawMean to CAV for certain summaries
	 * @param summaries
	 * @param src
	 * @param dest
	 */
	public static void copyColumn(Collection<Summary> summaries, String src, String dest){
		if(summaries != null){
			for(Summary summary : summaries){
				summary.getValues().put(dest, summary.getValues().get(src));
			}
		}
	}
	
	public static void calcStability(Collection<Summary> summaries, Collection<Summary> exlSummaries, int iqrSrize, Map<String,String> trialLevel){
		try{
			Map<Integer, Map<String,List<Integer>>> geneRanks = new HashMap<Integer, Map<String,List<Integer>>>();
			for(Iterator<Summary> eit = exlSummaries.iterator(); eit.hasNext();){
				Summary exl = eit.next();
				String trait = exl.getTrait().getName();
				Integer geno_id = exl.getFilters().get(trialLevel.get("genoType"));
				Integer rank = (Integer)(exl.getValues().get("rank"));
				if(rank != null){
					if(!geneRanks.containsKey(geno_id)){
						geneRanks.put(geno_id, new HashMap<String, List<Integer>>());
					}
					if(!geneRanks.get(geno_id).containsKey(trait)){
						geneRanks.get(geno_id).put(trait, new ArrayList<Integer>());
					}
					geneRanks.get(geno_id).get(trait).add(rank);
				}
			}
			Integer size = summaries.size();
			for(Summary entry : summaries){
				String trait = entry.getTrait().getName();
				Integer geno_id = entry.getFilters().get(trialLevel.get("genoType"));
				if(!geneRanks.containsKey(geno_id)) continue;
				if(!geneRanks.get(geno_id).containsKey(trait)) continue;
				Integer iqr = iqr(geneRanks.get(geno_id).get(trait), size, iqrSrize);
				entry.getValues().put("stability", iqr);
			}
		}
		catch(Exception e){
			log.warn("", e);
		}
	}

	public static Integer iqr(List<Integer> vals, int n, int iqrSize){
		try{
			if(vals==null || vals.size()==0) return null;
			Integer range = null;
			if(vals.size() < iqrSize)
				range = Collections.max(vals) - Collections.min(vals);
			else{
				int p = (int) Math.floor(.25*(vals.size()+1));
				Collections.sort(vals);
				vals = vals.subList(p, vals.size()-p);
				range = Collections.max(vals) - Collections.min(vals);
			}
			return range*100/n;
		}catch(Exception e){
			log.warn("", e);
			return null;
		}
	}
	
	public static void percentOfThreshold(Collection<Summary> summaries, Trial trial, String outputName, String genoType, String operator, double threshold){
		ExpFBKs fbks = trial.getFbks();
		Collection<Summary> summariesResult = new ArrayList<>();
		for(Summary summary : summaries){
			String traitName = summary.getTrait().getName();
			int genoId = Integer.parseInt(summary.getValues().get(genoType).toString());
			String allCount = fbks.createSql(" SELECT count(*) ", String.format(" WHERE %s = %s AND %s IS NOT NULL",Funcs.quoteString(genoType), genoId, Funcs.quoteString(traitName)) , "");
			List<Object[]> result = fbks.getList(String.format(" SELECT count(*) * 100.0 / (%s) ", allCount)
					, String.format(" WHERE %s = %s AND %s %s '%s'", Funcs.quoteString(genoType), genoId, Funcs.quoteString(traitName), operator, threshold ) , "");
			
			if(!result.isEmpty()){
				Summary summaryIndex = GeneralStats.createEntrySummary(new Trait(outputName, outputName), SummaryType.entrySummary, genoType, genoId, trial);
				summaryIndex.getValues().put("estimate", result.get(0));
				summariesResult.add(summaryIndex);
			}
		}

		summaries.addAll(summariesResult);
	}

	public static void numberOf(Collection<Summary> summaries, Tabs tabs, List<String> filters, String resultColumn){
		try{
			if(filters != null && filters.size() > 1){
				String[] ids = new String[filters.size()];
				ids = filters.toArray(ids);
	
				String colName = Tabs.createId(ids);
				for(Summary summary : summaries){
					numberOf(summary, tabs.getTabs().get(colName), resultColumn);
				}
			}
			else{
				log.warn("numberOf filter needs to be greater then 1");
			}
		}
		catch (Exception e) {
			log.warn("", e);
		}
	}

	/**
	 * 
	 * @param summary
	 * @param tabs
	 * @param resultColumn - Name of the column where this will 
	 */
	public static void numberOf(Summary summary, Collection<Tab> tabs, String resultColumn){
		try{
			List<String> entryFilter = new ArrayList<>();
			int j = 1;
			for(String filter : summary.getFilters().keySet()){
				entryFilter.add(" get(keys, " + j + ") = '" + summary.getFilters().get(filter).toString() + "' ");
				j++;
			}

			List<String> groupBy = new ArrayList<>();
			groupBy.add(" get(keys, 0) ");

			//query Tab collection
			ObjectQuery query = new ObjectQuery( " count AS " + resultColumn + " ", Tab.class.getName(), entryFilter, groupBy, tabs);
			List<List<Integer>> results = query.execute();

			if(results.size() > 0){
				for(int i =0; i<query.getAlias().size(); i++){
					summary.getValues().put(query.getAlias().get(i), results.size());
				}
			}
		}
		catch (Exception e) {
			log.warn("", e);
		}
	}

	/**
	 * CAV or Custom Analysis Value is used by VAT to create specific breakouts of data.
	 * 
	 * @param summaries
	 */
	public static void CAV(Collection<Summary> summaries) {
		try{
			if(null != summaries){
				for(Summary summary : summaries){
					summary.getValues().put("CAV", summary.getValues().get("rawMean"));
				}
			}
		}
		catch (Exception e) {
			log.warn("", e);
		}
	}


	
	
	
	public static void adjustCAV(Collection<Summary> exlSummaries, Collection<Summary> locSummaries, Map<String,String> trialLevel, ExpFBKs expFBKs){
		for(Summary exl : exlSummaries){
			try{
				String environment = trialLevel.get("environment");
				Integer locIndex = exl.getFilters().get(environment);
				Double locEstimate = 0d;
				
				for(Summary loc : locSummaries){
					if(loc.getFilters().get(environment) == locIndex){
						if(loc.getValues().get("estimate")!=null){
							locEstimate = Double.valueOf(loc.getValues().get("estimate").toString());
						}

						Trait trait = loc.getTrait();
						if(null != expFBKs.getBlockCenters().get(trait) && expFBKs.getBlockCenters().get(trait).size() > 0){
							locEstimate = locEstimate - expFBKs.getLocCenters().get(trait).get(locIndex);
						}
						break;
					}
				}
				Double cav = exl.getValues().get("CAV")==null ? null : Double.valueOf(exl.getValues().get("CAV").toString());
				exl.getValues().put("estimate", cav==null || locEstimate==null ? null : cav);
				exl.getValues().put("CAV", cav==null || locEstimate==null ? null : cav-locEstimate);
			}catch(Exception e){
				log.warn("", e);
			}
		}
	}

	/**
	 * Calc pi.
	 * 
	 * @param entrySummaries
	 *            the entry summaries
	 * @param exlSummaries
	 *            the exl summaries
	 * @param expSummaries
	 *            the exp summaries
	 * @param iqrSize
	 *            the iqr size
	 * @param trialLevel
	 *            the trial level
	 */
	public static void calcPI(Collection<Summary> entrySummaries, Collection<Summary> exlSummaries, Collection<Summary> expSummaries, int iqrSize, Map<String,String> trialLevel){
		try{
			Map<Integer, Map<Trait, Double>> pi = new HashMap<Integer, Map<Trait,Double>>();
			Map<Integer, Map<Trait, Double>> locationMax = new HashMap<Integer, Map<Trait,Double>>();
			setLocationMax(exlSummaries, trialLevel.get("environment"), locationMax);
			setPI(exlSummaries, expSummaries, locationMax, trialLevel, pi);
			for(Summary entry : entrySummaries){
				List<Integer> ls = new ArrayList<Integer>();
				for(Summary exl : exlSummaries){
					if(!exl.getFilters().get(trialLevel.get("genoType")).equals(entry.getFilters().get(trialLevel.get("genoType")))){
						continue;
					}
					if(exl.getValues().get("rank")!=null){
						ls.add((Integer) exl.getValues().get("rank"));
					}
				}
				if(ls.size()==0) continue;
				Integer stability = iqr(ls, entrySummaries.size(), iqrSize);
				
				Double p = pi.get(entry.getFilters().get(trialLevel.get("genoType"))).get(entry.getTrait());
				entry.getValues().put("stability", stability);
				entry.getValues().put("pi", p);
			}
		}catch(Exception e){
			log.warn("Error calculation Pi and Stability Index", e);
		}
	}

	public static void setLocationMax(Collection<Summary> exlSummaries, String environment, Map<Integer, Map<Trait,Double>> locationMax){
		for(Iterator<Summary> eit = exlSummaries.iterator(); eit.hasNext();){
			Summary entry = eit.next();
			Integer loc_id = entry.getFilters().get(environment);
			Trait trait = entry.getTrait();
			Double mean = Funcs.getDbl(entry.getValues().get("rawMean").toString());
			mean = mean == null ? 0 : mean;
			if(!locationMax.containsKey(loc_id)) locationMax.put(loc_id, new HashMap<Trait, Double>());
			Double val = !locationMax.get(loc_id).containsKey(trait) ? Double.MIN_VALUE : locationMax.get(loc_id).get(trait);
			Double max = val==null ? mean : Math.max(mean, val);
			locationMax.get(loc_id).put(trait, max);
		}
	}
	
	/**
	 * Sets the pi.
	 * 
	 * @param exlSummaries
	 *            the exl summaries
	 * @param expSummaries
	 *            the exp summaries
	 * @param locationMax
	 *            the location max
	 * @param trialLevel
	 *            the trial level
	 * @param pi
	 *            the pi
	 */
	public static void setPI(Collection<Summary> exlSummaries, Collection<Summary> expSummaries, Map<Integer, Map<Trait,Double>> locationMax, Map<String,String> trialLevel, Map<Integer, Map<Trait,Double>> pi){
		Map<Trait, Integer> numLoc = new HashMap<Trait, Integer>();
		String environement = trialLevel.get("environment");
		String genoType = trialLevel.get("genoType");
		for(Iterator<Summary> eit = expSummaries.iterator(); eit.hasNext();){
			try{
				Summary expSummary = eit.next();
				Integer num = expSummary.getValues().get("numberLocations")==null ? null : Integer.parseInt(expSummary.getValues().get("numberLocations").toString());
				
				Trait trait = expSummary.getTrait();
				numLoc.put(trait, num);
			}catch(Exception e){
				log.warn("Error getting # of locations", e);
			}
		}
		for(Iterator<Summary> eit = exlSummaries.iterator(); eit.hasNext();){
			try{
				Summary exlSummary = eit.next();
				if(exlSummary == null) continue;
				if(!locationMax.containsKey(exlSummary.getFilters().get(environement))) continue;
				if(!pi.containsKey(exlSummary.getFilters().get(genoType))){
					pi.put(exlSummary.getFilters().get(genoType), new HashMap<Trait, Double>());
				}
				Double sum = pi.get(exlSummary.getFilters().get(genoType)).containsKey(exlSummary.getTrait()) ? pi.get(exlSummary.getFilters().get(genoType)).get(exlSummary.getTrait()) : 0;
				Integer num = numLoc.get(exlSummary.getTrait()); 
				Double entryMean = (Double) exlSummary.getValues().get("rawMean");
				if(entryMean != null && num != null){
					sum += Funcs.sqr(entryMean - locationMax.get(exlSummary.getFilters().get(environement)).get(exlSummary.getTrait())) / (2*num);
					pi.get(exlSummary.getFilters().get(genoType)).put(exlSummary.getTrait(), sum);
				}
			}catch(Exception e){
				log.warn("PI \n"+e.getMessage());
			}
		}
	}

	public static Integer getGenoIndex(Trial trial, String genoType, Object genoObj){
		List<Object> genoIds = trial.getFbks().getColMap(genoType);
		List<Object> genoIdIndexes = trial.getFbks().getColMap(genoType+"_index");
		
		return getGenoIndex(genoIds, genoIdIndexes, genoObj);
	}
	
	public static Integer getGenoIndex(List<Object> ids, List<Object> indexes, Object value){
		Integer index = ids.indexOf(value);
		if(indexes != null && Integer.parseInt(indexes.get(index).toString()) != -1){
			index = Integer.parseInt(indexes.get(ids.indexOf(value)).toString());
		}
		else{
			index++;
		}
		return index;
	}
	
	public static void calcPRM(Trial trial, ReportOutputs reportOutputs){
		Trait traitMst = !trial.getTraits().contains(Traits.INSTANCE.get("moisture")) ? Traits.INSTANCE.get("sil_mst_total") : Traits.INSTANCE.get("moisture");
		SummaryType summaryType = trial.getType().equals(TrialType.MULTI) ? SummaryType.multiYearSummary : SummaryType.entrySummary;
		Trait traitRM = Traits.INSTANCE.get("rm");
		String genoType = trial.getDataLevel().get("genoType");
		List<Object> genoIds = trial.getFbks().getColMap(genoType);
		List<Object> genoIndices = trial.getFbks().getColMap(genoType+"_index");
		try{
			if(!reportOutputs.getReportOutputs().containsKey(traitMst)) return;
			if(!reportOutputs.getReportOutputs().containsKey(traitRM)) return;
			Collection<Summary> entryMst = reportOutputs.get(traitMst, false).get(summaryType);
			Collection<Summary> entryRM = reportOutputs.get(traitRM, false).get(summaryType);
			if(entryMst == null || entryMst.isEmpty()) return;
			if(entryRM == null || entryRM.isEmpty()) return;
			Integer count = 0;
			double sumX = 0d;
			double sumY = 0d;
			double sumXX = 0d;
			double sumXY = 0d;
			// calculate regression
			for(Summary summaryRM : entryRM)
				try{
					Object genoObj = summaryRM.getValues().get(genoType);
					Integer genoIndex = GeneralStats.getGenoIndex(genoIds,genoIndices, genoObj);
					LinkedHashMap<String, Integer> filter = new LinkedHashMap<String, Integer>();
					filter.put(genoType, genoIndex);
					Summary summaryMst = Summary.search(entryMst, filter);
					if(summaryMst == null) continue;
					Double y = Double.parseDouble(summaryRM.getValues().get("rawMax").toString());
					Double x = summaryMst.getEstimate();
					if(x==null || y==null)continue;
					count++;
					sumX += x;
					sumY += y;
					sumXX += x*x;
					sumXY += x*y;
				}catch(Exception e){
					log.warn(e.getMessage(), e);
				}
			double slope = ((count*sumXY)-(sumX*sumY))/((count*sumXX)-(sumX*sumX));
			double intercept = (sumY - (slope*sumX))/count;		
			// calculate PRM values from moisture using f(prm) = intercept + slope*moisture
			for(Iterator<Object> git = genoIds.iterator(); git.hasNext();)
				try{
					Object genoObj = git.next();
					Integer genoId = Integer.parseInt(genoObj.toString());
					Integer genoIndex = GeneralStats.getGenoIndex(genoIds,genoIndices, genoObj);
					LinkedHashMap<String, Integer> filter = new LinkedHashMap<String, Integer>();
					filter.put(genoType, genoIndex);
					Summary summaryMst = Summary.search(entryMst, filter);					
					Summary summaryRM = Summary.search(entryRM, filter);
					Double est = 0d;
					if(null != summaryMst && null != summaryMst.getEstimate()){
						est = intercept + (slope*summaryMst.getEstimate());
					}
					Integer rank = Funcs.round(est);
					if(summaryRM == null){
						summaryRM = createEntrySummary(traitRM, summaryType, genoType, genoId, trial,filter);
						summaryRM.getValues().put("rank", rank);
						entryRM.add(summaryRM);
					}else{
						summaryRM.getValues().put("rank", rank);
					}
				}catch(Exception e){
					log.warn(e.getMessage(), e);
				}
		}catch(Exception e){
			log.warn(e.getMessage(), e);
		}
	}
	
	@Deprecated
	public static void prmCalc(Trial trial, ReportOutputs reportOutputs){
		Trait traitMst = !trial.getTraits().contains(Traits.INSTANCE.get("moisture")) ? Traits.INSTANCE.get("sil_mst_total") : Traits.INSTANCE.get("moisture");
		try{
			String genoType = trial.getDataLevel().get("genoType");
			ExpFBKs fbks = trial.getFbks();
			Map<String, List<Double>> checks_R = new HashMap<String, List<Double>>();
			Map<String, List<Double>> checks_M = new HashMap<String, List<Double>>();
			Map<String, Double> M = new HashMap<String, Double>();
			Map<String, Double> R = new HashMap<String, Double>();
			
			Double avgR = 0d; Double avgM = 0d;
			Integer count = 0;

			for (ExpFBK fbk : fbks.getFbks()) {
				Object geno_id = fbk.getValue(genoType).toString();
				if(fbks.getCoreChecks().contains(geno_id)){
//					String sRM = fbks.getColMap().get("rm")==null ? null : fbks.getColMap().get("rm").toString(); 
//					String sMst = fbks.getColMap().get(traitMst.toString())==null ? null : fbks.getColMap().get(traitMst.toString()).toString();
//					if(sRM!=null && sMst!=null){
//						Double rm = new Double(sRM);
//						Double mst = new Double(sMst);
					Double rm = fbk.getDbl("rm");
					Double mst = fbk.getDbl(traitMst.toString());
					if(rm==null || mst==null) continue;
					avgR += rm;
					avgM += mst;
					count ++;
					if(!checks_R.containsKey(geno_id)) checks_R.put(geno_id.toString(), new ArrayList<Double>());
					if(!checks_M.containsKey(geno_id)) checks_M.put(geno_id.toString(), new ArrayList<Double>());
					checks_R.get(geno_id).add(rm);
					checks_M.get(geno_id).add(mst);
//					}
				}
			}
			avgR = count==0 ? 0 : (avgR/count);
			avgM = count==0 ? 0 : (avgM/count);
			
			// average lines
			for(Iterator<String> gid = checks_R.keySet().iterator(); gid.hasNext();){
				String geno_id = gid.next();
				Double r = Funcs.averageList(checks_R.get(geno_id));
				Double m = Funcs.averageList(checks_M.get(geno_id));
				R.put(geno_id, r);
				M.put(geno_id, m);
			}
			
			// compute sum(R-avgR) and sum(M-avgM)
			Double beta_num = 0d;
			Double beta_denom = 0d;
			for(String genoId : R.keySet()){
				Double rm = R.get(genoId);
				Double mst = M.get(genoId);
				beta_num += (mst-avgM)*(rm-avgR);
				beta_denom += Funcs.sqr(mst-avgM);
			}
			
			// calculate beta 0 and 1
			Double beta_1 = beta_num/beta_denom;
			Double beta_0 = avgR - (beta_1*avgM);
			Trait rmTrait = Traits.INSTANCE.get("rm");
			Collection<Summary> entryMst = reportOutputs.get(traitMst, false).get(SummaryType.entrySummary);
			Collection<Summary> collection = new ArrayList<Summary>();
			for(Summary summary : entryMst){
				Double est = 0d;
				if(null != summary.getEstimate()){
					est = beta_0 + (beta_1*summary.getEstimate());
				}
				Integer rank = Funcs.round(est);
				Integer genoId = Integer.parseInt(summary.getValues().get(genoType).toString());
				Summary summaryIndex = createEntrySummary(rmTrait, SummaryType.entrySummary, genoType, genoId, trial);
				summaryIndex.getValues().put("rank", rank);
				collection.add(summaryIndex);
			}
			if(collection.size()>0) reportOutputs.get(rmTrait, true).add(SummaryType.entrySummary, collection);
		}catch(Exception e){
			log.warn(e.getMessage(), e);
		}
	}
	
	public static void pctWins(Trial trial, ReportOutputs reportOutputs){
		Trait trait = null;
		if(trial.getTraits().contains(Traits.INSTANCE.get("yield"))){
			trait = Traits.INSTANCE.get("yield");
		}
		else if(trial.getTraits().contains(Traits.INSTANCE.get("tons_acre"))){
			trait = Traits.INSTANCE.get("tons_acre");
		}
		else if(trial.getTraits().contains(Traits.INSTANCE.get("yield_tph"))){
			trait = Traits.INSTANCE.get("yield_tph");
		}
		String genoType = trial.getDataLevel().get("genoType");
		String environment = trial.getDataLevel().get("environment");
		ExpFBKs fbks = trial.getFbks();
		// <locId, blockId, groupId, subGroupId, genoId, yield>
		HashMap<Object, HashMap<Object, HashMap<Object, HashMap<Object, HashMap<Object, Double>>>>> genoVals = new HashMap<>();
		// <blockId, groupId, subGroupId, checkValue>
		HashMap<Object, HashMap<Object, HashMap<Object, Double>>> checkVals = new HashMap<>();
		// <locId, blockId, groupId, subGroupId, genoId, count>
		HashMap<Object, HashMap<Object, HashMap<Object, HashMap<Object, HashMap<Object, Integer>>>>> genoCount = new HashMap<>();
		// <blockId, groupId, subGroupId, checkcount>
		HashMap<Object, HashMap<Object, HashMap<Object, Integer>>> checkCount = new HashMap<>();
		// <genoId, wins>
		HashMap<Object, Double> wins = new HashMap<>();
		// <genoId, Integer>
		HashMap<Object, Integer> denom = new HashMap<>();
		
		// sum yield values for both genotypes and checks
		for (ExpFBK fbk : fbks.getFbks()) {
			try{
				Object genoId = fbk.getValue(genoType).toString();
				Object locId = fbk.getValue(environment).toString();
				Object blockId = fbk.getValue("blockId").toString();
				Object groupId = fbk.getValue("groupId").toString();
				Object subGroupId = fbk.getValue("subGroupId").toString();
				Double yield = fbk.getDbl(trait.toString());
				Integer count = 1;
				
				Double chkYield = yield;
				Integer chkCount = count;
				
				if(yield == null) continue;
				
				if(!genoVals.containsKey(locId)){
					genoVals.put(locId, new HashMap<Object, HashMap<Object, HashMap<Object, HashMap<Object, Double>>>>());
					genoCount.put(locId, new HashMap<Object, HashMap<Object, HashMap<Object, HashMap<Object, Integer>>>>());
				}
				if(!genoVals.get(locId).containsKey(blockId)){
					genoVals.get(locId).put(blockId, new HashMap<Object, HashMap<Object, HashMap<Object, Double>>>());
					genoCount.get(locId).put(blockId, new HashMap<Object, HashMap<Object, HashMap<Object, Integer>>>());
				}
				if(!genoVals.get(locId).get(blockId).containsKey(groupId)){
					genoVals.get(locId).get(blockId).put(groupId, new HashMap<Object, HashMap<Object, Double>>());
					genoCount.get(locId).get(blockId).put(groupId, new HashMap<Object, HashMap<Object, Integer>>());
				}
				if(!genoVals.get(locId).get(blockId).get(groupId).containsKey(subGroupId)){
					genoVals.get(locId).get(blockId).get(groupId).put(subGroupId, new HashMap<Object, Double>());
					genoCount.get(locId).get(blockId).get(groupId).put(subGroupId, new HashMap<Object, Integer>());
				}
				if(genoVals.get(locId).get(blockId).get(groupId).get(subGroupId).containsKey(genoId)){
					yield += genoVals.get(locId).get(blockId).get(groupId).get(subGroupId).get(genoId);
					count += genoCount.get(locId).get(blockId).get(groupId).get(subGroupId).get(genoId);
				}
				genoVals.get(locId).get(blockId).get(groupId).get(subGroupId).put(genoId, yield);
				genoCount.get(locId).get(blockId).get(groupId).get(subGroupId).put(genoId, count);
				
				if(fbks.getGeneticChecks().contains(genoId)){
					if(!checkVals.containsKey(blockId)){
						checkVals.put(blockId, new HashMap<Object, HashMap<Object, Double>>());
						checkCount.put(blockId, new HashMap<Object, HashMap<Object, Integer>>());
					}
					if(!checkVals.get(blockId).containsKey(groupId)){
						checkVals.get(blockId).put(groupId, new HashMap<Object, Double>());
						checkCount.get(blockId).put(groupId, new HashMap<Object, Integer>());
					}
					Double chk = (checkVals.get(blockId).get(groupId).containsKey(subGroupId) ? checkVals.get(blockId).get(groupId).get(subGroupId) : 0d) + chkYield;
					Integer cnt = (checkCount.get(blockId).get(groupId).containsKey(subGroupId) ? checkCount.get(blockId).get(groupId).get(subGroupId) : 0) + chkCount;
					checkVals.get(blockId).get(groupId).put(subGroupId, chk);
					checkCount.get(blockId).get(groupId).put(subGroupId, cnt);
				}
			}catch(Exception e){
				log.warn("", e);
			}
		}
		
		// average maps and count wins
		for(Object blockId : checkVals.keySet()){
			for(Object groupId : checkVals.get(blockId).keySet()){
				for(Object subGroupId : checkVals.get(blockId).get(groupId).keySet()){
					try{
						Double val = checkVals.get(blockId).get(groupId).get(subGroupId)/checkCount.get(blockId).get(groupId).get(subGroupId);
						checkVals.get(blockId).get(groupId).put(subGroupId, val);
					}catch(Exception e){
						log.warn(e.getMessage());
					}
				}
			}
		}
		for(Object locId : genoVals.keySet()){
			try{
				HashMap<Object, HashMap<Object, HashMap<Object, HashMap<Object, Double>>>> mapLoc = genoVals.get(locId);
				HashMap<Object, HashMap<Object, HashMap<Object, HashMap<Object, Integer>>>> countLoc = genoCount.get(locId);
				for(Object blockId : mapLoc.keySet()){
					try{
						// ignore block if there is no genetic check
						if(!checkVals.containsKey(blockId)) continue;
						HashMap<Object, HashMap<Object, HashMap<Object, Double>>> mapBlock = mapLoc.get(blockId);
						HashMap<Object, HashMap<Object, HashMap<Object, Integer>>> countBlock = countLoc.get(blockId);
						for(Object groupId : mapBlock.keySet()){
							try{
								// ignore group if there is no genetic check
								if(!checkVals.get(blockId).containsKey(groupId)) continue;								
								HashMap<Object, HashMap<Object, Double>> mapGroup = mapBlock.get(groupId);
								HashMap<Object, HashMap<Object, Integer>> countGroup = countBlock.get(groupId);
								for(Object subGroupId : mapGroup.keySet()){
									// ignore sub group if there is no genetic check
									if(!checkVals.get(blockId).get(groupId).containsKey(subGroupId)) continue;
									HashMap<Object, Double> mapSubGroup = mapGroup.get(subGroupId);
									HashMap<Object, Integer> countSubGroup = countGroup.get(subGroupId);
									Double checkVal = checkVals.get(blockId).get(groupId).get(subGroupId);
									try{
										for(Object genoId : mapSubGroup.keySet()){
											try{
												Integer count = countSubGroup.get(genoId);						
												Double avg = mapSubGroup.put(genoId, mapSubGroup.get(genoId)/count);
												if(100*avg/checkVal >= 98.00){
													wins.put(genoId, !wins.containsKey(genoId) ? 1 : wins.get(genoId)+1);
												}
												denom.put(genoId, !denom.containsKey(genoId) ? count : denom.get(genoId)+count);
											}catch(Exception e){
												log.warn(e.getMessage());
											}
										}
									}catch(Exception e){
										log.warn(e.getMessage());
									}
								}
							}catch(Exception e){
								log.warn(e.getMessage());
							}
						}
					}catch(Exception e){
						log.warn(e.getMessage());
					}
				}
			}catch(Exception e){
				log.warn(e.getMessage());
			}
		}
		
		// update output reports with wins and denom
		Collection<Summary> collection = new ArrayList<Summary>();
		Trait traitWin = Traits.INSTANCE.get("wins");
		for(Iterator<Object> git = wins.keySet().iterator(); git.hasNext();){
			Object genoId = git.next();
			Double win = wins.get(genoId);
			Integer count = denom.get(genoId);
			Double estimate = win==null || count==null ? null : win*100/count;
			
			Summary summaryIndex = createEntrySummary(traitWin, SummaryType.entrySummary, genoType, Integer.parseInt(genoId.toString()), trial);
			summaryIndex.getValues().put("estimate", estimate);
			summaryIndex.getValues().put("rawCount", count);
			collection.add(summaryIndex);
		}
		if(collection.size()>0) reportOutputs.get(traitWin, true).add(SummaryType.entrySummary, collection);
	}
	
	@Deprecated
	public static void calcWins(Trial trial, ReportOutputs reportOutputs){
		Trait traitYld = Traits.INSTANCE.get("yield") == null ? Traits.INSTANCE.get("tons_acre") : Traits.INSTANCE.get("yield");
		Collection<Summary> exl = reportOutputs.get(traitYld, false).get(SummaryType.exlSummary);
		HashMap<Integer, Double> wins = new HashMap<Integer, Double>();
		HashMap<Integer, Integer> num = new HashMap<Integer, Integer>();
		String genoType = trial.getDataLevel().get("genoType");
		for(Summary summary : exl){
			try{
				Double check = summary.getValues().get("geneticCheck")==null ? 0d : Double.parseDouble(summary.getValues().get("geneticCheck").toString());
				Integer genoId = Integer.parseInt(summary.getValues().get(genoType).toString());
				Double win;
				Integer n;
				if(wins.containsKey(genoId)){
					win = wins.get(genoId);
					n = num.get(genoId);
				}else{
					win = 0d;
					n = 0;
				}
				win += check >= 98d ? 1 : 0;
				n += 1;
//				Double win = (!wins.containsKey(genoId) ? 0d : wins.get(genoId)) + (check > 98d ? 1 : 0);
//				Integer n = (!num.containsKey(genoId) ? 0 : num.get(genoId)) + 1;
				wins.put(genoId, win);
				num.put(genoId, n);
			}catch(Exception e){
				log.warn("", e);
			}
		}
		try{
			Collection<Summary> collection = new ArrayList<Summary>();
			Trait traitWin = Traits.INSTANCE.get("wins");
			for(Iterator<Integer> git = wins.keySet().iterator(); git.hasNext();){
				Integer genoId = git.next();
				Double win = wins.get(genoId);
				Integer n = num.get(genoId);
				Double rank = win==null || n==null ? null : win*100/n;
				Summary summaryIndex = createEntrySummary(traitWin, SummaryType.entrySummary, genoType, genoId, trial);
				summaryIndex.getValues().put("rank", rank);
				collection.add(summaryIndex);
			}
			if(collection.size()>0) reportOutputs.get(traitWin, true).add(SummaryType.entrySummary, collection);
		}catch(Exception e){
			log.error("Error in % wins", e);
		}
	}
}