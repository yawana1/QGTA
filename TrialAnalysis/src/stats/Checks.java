package stats;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import report.Summary;
import utils.Funcs;
import utils.Globals.CheckType;
import utils.Globals.SummaryType;
import data.collection.ExpFBK;
import data.collection.ExpFBKs;
import data.xml.objects.Trait;

/***
 * Class for calculating Core, Performance, Genetic, and Brm Check mean and percent of mean
 * 
 * @author Scott Smith
 *
 */
public class Checks {

	private static Logger log = Logger.getLogger(Checks.class.getName());
	
	/***
	 * Used saved Mean values from Entry Summary creation to create the Loc Summary raw check means
	 * 
	 * @param summaries -
	 * @param means - Map of Checks objects
	 * @param environment - Trial's environment data level
	 * @param checkType - ie. AVG or MAX
	 */
	public static void calcCheckMean(Collection<Summary> summaries, Map<Trait, Map<Object,data.xml.objects.Checks>> means, String environment, CheckType checkType){
		try{
			if(null != summaries){			
				for(Summary summary : summaries){
					LinkedHashMap<String, Integer> filters = summary.getFilters();
					if(!filters.containsKey(environment)){
						log.error("Summary doesn't have "+ environment + " filter");
					}
					else{
						Integer environmentId = filters.get(environment);
						Trait trait = summary.getTrait();
						
						Map<Object, data.xml.objects.Checks> checkMeans = null;
						if( (checkMeans = means.get(trait)) != null){
							data.xml.objects.Checks checkMean = checkMeans.get(environmentId);
							
							summary.getValues().put("rawCoreCheckMean", checkMean.getAvgCore(checkType));
							summary.getValues().put("rawPerfCheckMean", checkMean.getAvgPerf(checkType));
							summary.getValues().put("rawBmrCheckMean", checkMean.getAvgBmr(checkType));
							summary.getValues().put("rawGeneticCheckMean", checkMean.getAvgGenetic(checkType));
							summary.getValues().put("rawSusceptableCheckMean", checkMean.getAvgSusceptable(checkType));
						}
						else{
							log.warn("No checkMeans for trait - " + trait);
						}
					}
				}
			}
		}
		catch (Exception e) {
			log.error("", e);
		}
	}
	
	/**
	 * Weight the avg check by count or use best check.
	 * 
	 * @param checkType - ie. AVG or MAX
	 * @param check - Value of the check
	 * @param effect - Chosen effect value.  Ie estimate, rawMean. etc.
	 * @param count - Number of records
	 * @return - Running check avg value
	 */
	private static Double calcCheck(CheckType checkType, Double check, Double effect, int count){
		if(effect != null){
			if(CheckType.AVG.equals(checkType)){
				check += effect * count;
			}
			else if(CheckType.MAX.equals(checkType)){
				check = Math.max(check, effect);
			}
		}
		return check;
	}
	
	/**
	 * Group summaries by filter to find percent checks.
	 * 
	 * @param summaries
	 * @param fbks
	 * @param checkType - ie. AVG or MAX
	 * @param effect - Chosen effect value.  Ie estimate, rawMean. etc.
	 * @param genoType - Trial's genoType data level
	 */
	public static void getChecksPercentGrouped(Collection<Summary> summaries, ExpFBKs fbks, CheckType checkType, String effect, String genoType) {
		try{
			if(summaries.size() > 0){
				LinkedHashMap<String, Integer> filters = summaries.iterator().next().getFilters();
				List<List<Summary>> groupedSummeries = GeneralStats.groupByFiliter(summaries, filters, genoType); //group by filters exclude genoId
				
				for(List<Summary> filteredSummaries : groupedSummeries){
					getCheckPercent(filteredSummaries, fbks, checkType, effect,genoType);
				}
			}
		}
		catch (Exception e) {
			log.warn("",e);
		}
	}
	
	public static void getChecksDeviation(Collection<Summary> summaries, ExpFBKs fbks, CheckType checkType, String effect, String genoType) {
		try{
			if(summaries.size() > 0){
				LinkedHashMap<String, Integer> filters = summaries.iterator().next().getFilters();
				List<List<Summary>> groupedSummeries = GeneralStats.groupByFiliter(summaries, filters, genoType); //group by filters exclude genoId
				
				for(List<Summary> filteredSummaries : groupedSummeries){
					getCheckDeviation(filteredSummaries, fbks, checkType, effect,genoType);
				}
			}
		}
		catch (Exception e) {
			log.warn("",e);
		}
	}
	
	/**
	 * Calc the Core, Bmr, Perf, and Genectic checks for a Collection of summaries
	 * @param summaries
	 * @param fbks
	 * @param checkType
	 */
	public static data.xml.objects.Checks getCheckMean(Collection<Summary> summaries, ExpFBKs fbks, CheckType checkType, String effect, String genoType) {
		data.xml.objects.Checks checkObject = null;
		
		try{
			if(null != summaries && summaries.size() > 0){
				double avgCoreCheck = 0;
				int numCoreCheck = 0;
		
				double avgPerfCheck = 0;
				int numPerfCheck = 0;
		
				double avgBmrCheck = 0;
				int numBmrCheck = 0;
				
				double avgGeneticCheck = 0;
				int numGeneticCheck = 0;
				
				double avgSusceptableCheck = 0d;
				int numSusceptableCheck = 0;
				
				for(Summary summary : summaries){
					if(!summary.getValues().containsKey("rawCount")){
						continue;
					}
					else if(summary.getValues().get(effect) == null){
						log.warn("No " + effect + " for Summary " + summary);
						continue;
					}
					String genoTypeId = "" + summary.getValues().get(genoType);
					Integer count = Funcs.getInt(summary.getValues().get("rawCount").toString());
					Double effectValue = Funcs.getDbl(summary.getValues().get(effect).toString());
					
					if(count != null){
						if(fbks.getCoreChecks().contains(genoTypeId)){
							avgCoreCheck = calcCheck(checkType, avgCoreCheck, effectValue, count);
							numCoreCheck += count;
						}
						else if(fbks.getPerformanceChecks().contains(genoTypeId)){
							avgPerfCheck = calcCheck(checkType, avgPerfCheck, effectValue, count);
							numPerfCheck += count;
						}
						else if(fbks.getBmrChecks().contains(genoTypeId)){
							avgBmrCheck = calcCheck(checkType, avgBmrCheck, effectValue, count);
							numBmrCheck += count;
						}
						else if(fbks.getGeneticChecks().contains(genoTypeId)){
							avgGeneticCheck = calcCheck(checkType, avgGeneticCheck, effectValue, count);
							numGeneticCheck += count;
						}
						else if(fbks.getSusceptableChecks().contains(genoTypeId)){
							avgSusceptableCheck = calcCheck(checkType, avgSusceptableCheck, effectValue, count);
							numSusceptableCheck += count;
						}
					}
				}
				
				checkObject = new data.xml.objects.Checks();
				
				checkObject.setCore(avgCoreCheck);
				checkObject.setNumCoreCheck(numCoreCheck);
				checkObject.setPerf(avgPerfCheck);
				checkObject.setNumPerfCheck(numPerfCheck);
				checkObject.setBmr(avgBmrCheck);
				checkObject.setNumBmrCheck(numBmrCheck);
				checkObject.setGenetic(avgGeneticCheck);
				checkObject.setNumGeneticCheck(numGeneticCheck);
				checkObject.setSusceptable(avgSusceptableCheck);
				checkObject.setNumSusceptableCheck(numSusceptableCheck);
				
				//to save on processing the loc statistics check
				SummaryType template = summaries.iterator().next().getType();
				if(SummaryType.exlSummary.equals(template)){
					Summary summary = summaries.iterator().next();
					Integer locId = summary.getFilters().entrySet().iterator().next().getValue();
					Trait trait = summary.getTrait();
					Map<Object, data.xml.objects.Checks> map = fbks.getCheckMeans().get(trait);
					if(map == null){
						map = new HashMap<Object, data.xml.objects.Checks>();
					}
					map.put(locId, checkObject);
					fbks.getCheckMeans().put(trait, map);
				}
				
			}
		}
		catch (Exception e) {
			log.warn("", e);
		}
		
		return checkObject;
	}
	
	public static void getCheckPercent(Collection<Summary> summaries, ExpFBKs fbks, CheckType checkType, String effect, String genoType){
		try{
			if(null != summaries){
				data.xml.objects.Checks checkObject = getCheckMean(summaries, fbks, checkType, effect, genoType);
				
				double avgCoreCheck = 0;
				double avgPerfCheck = 0;
				double avgBmrCheck = 0;
				double avgSusceptableCheck = 0;
//				double avgGeneticCheck = 0;

				avgCoreCheck = checkObject.getAvgCore(checkType);
				avgPerfCheck = checkObject.getAvgPerf(checkType);
				avgBmrCheck = checkObject.getAvgBmr(checkType);
				avgSusceptableCheck = checkObject.getAvgSusceptable(checkType);
//				avgGeneticCheck = checkObject.getAvgGenetic(checkType);

				//estimate / avg or best raised to 100% scale
				for(Summary summary : summaries){
					boolean failover = false;
					if(SummaryType.exlSummary.equals(summary.getType())){
						failover = true;
					}
					
					if(summary.getValues().containsKey(effect) || (failover && summary.getValues().containsKey("rawMean"))){
						Double effectValue = Funcs.getDbl(summary.getValues().get(effect).toString());
						if(failover && effectValue == null){
							effectValue = Funcs.getDbl(summary.getValues().get("rawMean").toString());
						}
						Double outputValue = null;

						Double coreCheck = avgCoreCheck==0 ? 0 : (effectValue*100/avgCoreCheck);
						outputValue = formatCheckOutput(checkObject.getNumCoreCheck() ,coreCheck);
						summary.getValues().put("coreCheck", outputValue);

						Double perfCheck = avgPerfCheck==0 ? 0 : (effectValue*100/avgPerfCheck);
						outputValue = formatCheckOutput(checkObject.getNumPerfCheck() ,perfCheck);
						summary.getValues().put("perfCheck", outputValue);

						Double bmrCheck = avgBmrCheck==0 ? 0 : (effectValue*100/avgBmrCheck);
						outputValue = formatCheckOutput(checkObject.getNumBmrCheck() ,bmrCheck);
						summary.getValues().put("bmrCheck", outputValue);
						
						Double susCheck = avgSusceptableCheck==0 ? 0 : (effectValue*100/avgSusceptableCheck);
						outputValue = formatCheckOutput(checkObject.getNumSusceptableCheck(), susCheck);
						summary.getValues().put("susceptableCheck", outputValue);
					}
				}
			}
		}
		catch (Exception e) {
			log.warn("", e);
		}
	}
	
	public static void getCheckDeviation(Collection<Summary> summaries, ExpFBKs fbks, CheckType checkType, String effect, String genoType){
		try{
			if(null != summaries){
				data.xml.objects.Checks checkObject = getCheckMean(summaries, fbks, checkType, effect, genoType);
				
				double avgCoreCheck = 0;
				double avgPerfCheck = 0;

				avgCoreCheck = checkObject.getAvgCore(checkType);
				avgPerfCheck = checkObject.getAvgPerf(checkType);

				//estimate / avg or best raised to 100% scale
				for(Summary summary : summaries){
					boolean failover = false;
					if(SummaryType.exlSummary.equals(summary.getType())){
						failover = true;
					}
					
					if(summary.getValues().containsKey(effect) || (failover && summary.getValues().containsKey("rawMean"))){
						Double effectValue = Funcs.getDbl(summary.getValues().get(effect).toString());
						if(failover && effectValue == null){
							effectValue = Funcs.getDbl(summary.getValues().get("rawMean").toString());
						}
						Double outputValue = null;

						Double coreCheck = effectValue-avgCoreCheck;
						outputValue = formatCheckOutput(checkObject.getNumCoreCheck() ,coreCheck);
						summary.getValues().put("coreCheckDev", outputValue);

						Double perfCheck = effectValue-avgPerfCheck;
						outputValue = formatCheckOutput(checkObject.getNumPerfCheck() ,perfCheck);
						summary.getValues().put("perfCheckDev", outputValue);
					}
				}
			}
		}
		catch (Exception e) {
			log.warn("", e);
		}
	}
	
	/***
	 * Set the value of the check on the output to null if the count is 0.
	 * 
	 * @param count - Number of checks
	 * @param check - Value of the check
	 * @return
	 */
	private static Double formatCheckOutput(int count, Double check){
		return count == 0 ? null : check;
	}
	
	/***
	 * 
	 * The total number of checks based on the checkType.
	 * MAX being 1
	 * AVG being the total count
	 * 
	 * @param checkType - ie AVG or MAX
	 * @param totalCount - Denominator in AVG
	 * @return - Denominator to used basic on the average.
	 */
	public static int getNumChecks(CheckType checkType, int totalCount){
		int num = 0;
		if(CheckType.AVG.equals(checkType)){
			num = totalCount;
		}
		else if(CheckType.MAX.equals(checkType)){
			num = 1; 
		}
		return num;
	}
	
	public static void calcGroupPctChecks(Collection<Summary> summaries, ExpFBKs fbks, String genoType){
		HashMap<Integer, Double> avgGeneticChecks = new HashMap<Integer, Double>();
		HashMap<Integer, Integer> numGeneticChecks = new HashMap<Integer, Integer>();
		// get geneticCheck estimates
		for(Iterator<Summary> sit = summaries.iterator(); sit.hasNext();){
			Summary summary = sit.next();
			Integer entryId = Integer.parseInt(summary.getValues().get(genoType).toString());
//			Object entry = fbks.getColMap().get(genoType).get(entryIndex-1);
//			Integer entryId = entry == null ? null : Integer.parseInt(entry.toString());
			ExpFBK fbk = fbks.getFirstFBK(genoType, entryId);
			Integer groupId = fbk.getInt("groupId");
			summary.getValues().put("groupId", groupId);
			if(fbk != null && fbk.getBool("geneticCheck")){
				
				Double avg = !avgGeneticChecks.containsKey(groupId) ? 0d : avgGeneticChecks.get(groupId);
				Integer num = !numGeneticChecks.containsKey(groupId) ? 0 : numGeneticChecks.get(groupId);
				
				if(summary.getEstimate()!=null){
					avgGeneticChecks.put(groupId, avg+summary.getEstimate());
					numGeneticChecks.put(groupId, num+1);
				}
			}
		}
		// avgerage geneticCheck estimates
		for(Integer groupId : avgGeneticChecks.keySet()){
			Double avg = avgGeneticChecks.get(groupId);
			Integer num = numGeneticChecks.get(groupId);
			if(avg != null && num != null){
				avgGeneticChecks.put(groupId, avg/num);
			}
		}
		// set % geneticCheck of each summary by group
		for(Summary summary : summaries){
			Double estimate = summary.getEstimate();
			Double avg = avgGeneticChecks.get(summary.getValues().get("groupId"));
			Double pctCheck = estimate==null || avg==null ? null : estimate*100/avg;
			summary.getValues().put("geneticCheck", pctCheck);
		}
	}
	
	public static void calcGroupPctChecks(Collection<Summary> summaries, ExpFBKs fbks, String genoType, String environment){
		// locIndex x groupId maps
		Map<Integer, HashMap<Integer, Double>> avgGeneticChecks = new HashMap<Integer, HashMap<Integer,Double>>();
		Map<Integer, HashMap<Integer, Integer>> numGeneticChecks = new HashMap<Integer, HashMap<Integer,Integer>>();
		// get geneticCheck estimates
		for(Iterator<Summary> sit = summaries.iterator(); sit.hasNext();){
			Summary summary = sit.next();
			Integer entryId = Integer.parseInt(summary.getValues().get(genoType).toString());
//			Integer entryId = Integer.parseInt(fbks.getColMap().get(genoType).get(entryIndex-1).toString());
			Integer locId = Integer.parseInt(summary.getValues().get(environment).toString());
//			Integer locId = Integer.parseInt(fbks.getColMap().get(environment).get(locIndex-1).toString());
			ExpFBK fbk = fbks.getFirstFBK(genoType, entryId);
			Integer groupId = fbk.getInt("groupId");
			summary.getValues().put("groupId", groupId);
			if(fbk != null && fbk.getBool("geneticCheck")){
				if(!avgGeneticChecks.containsKey(locId)){
					avgGeneticChecks.put(locId, new HashMap<Integer, Double>());
					numGeneticChecks.put(locId, new HashMap<Integer, Integer>());
				}
				Double avg = !avgGeneticChecks.get(locId).containsKey(groupId) ? 0d : avgGeneticChecks.get(locId).get(groupId);
				Integer num = !numGeneticChecks.get(locId).containsKey(groupId) ? 0 : numGeneticChecks.get(locId).get(groupId);
				
				if(summary.getValues().get("rawMean")!=null){
					avgGeneticChecks.get(locId).put(groupId, avg+Double.parseDouble(summary.getValues().get("rawMean").toString()));
					numGeneticChecks.get(locId).put(groupId, num+1);
				}
			}
		}
		// avgerage geneticCheck estimates
		for(Integer locId : avgGeneticChecks.keySet()){
			for(Integer groupId : avgGeneticChecks.get(locId).keySet()){
				Double avg = avgGeneticChecks.get(locId).get(groupId);
				Integer num = numGeneticChecks.get(locId).get(groupId);
				if(avg != null && num != null){
					avgGeneticChecks.get(locId).put(groupId, avg/num);
				}
			}
		}
		// set % geneticCheck of each summary by group
		for(Summary summary : summaries){
			try{
				Double estimate = Double.parseDouble(summary.getValues().get("rawMean").toString());
				String environmentId = summary.getValues().get(environment).toString();
				String groupId = summary.getValues().get("groupId").toString();
				
				Map<Integer,Double> geneticChecksByGroup = avgGeneticChecks.get(Integer.parseInt(environmentId)); 
				
				if(geneticChecksByGroup != null){
					Double avg = geneticChecksByGroup.get(Integer.parseInt(groupId));
					Double pctCheck = estimate==null || avg==null ? null : estimate*100/avg;
					summary.getValues().put("geneticCheck", pctCheck);
				}
			}catch(NumberFormatException e){
				log.warn("", e);
			}catch(Exception e){
				log.warn("", e);
			}
		}
	}
}
