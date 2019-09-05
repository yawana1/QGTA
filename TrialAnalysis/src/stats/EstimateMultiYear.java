package stats;

import java.math.RoundingMode;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import asreml.AsremlTrait;
import asreml.input.AsremlModel;
import asreml.input.AsremlRandomEffect;
import asreml.input.AsremlRandomEffects;
import asreml.output.AsrData;
import asreml.output.AsremlOutput;
import asreml.output.Sln;
import asreml.output.Slns;
import data.collection.ExpFBK;
import data.collection.ExpFBKs;
import report.Summary;
import utils.Funcs;
import utils.Globals.StatGroup;

public class EstimateMultiYear {

	static Logger logger = Logger.getLogger(EstimateMultiYear.class.getName());
	protected static int ROUND = 6;
	protected static RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
	
	/***
	 * Add estimate, error, lowCI and hiCI to summary.
	 * @param summaries
	 * @param model
	 * @param trait
	 * @param fbks
	 */
	public static void getEstimates(boolean isConverged, Collection<Summary> summaries, AsremlModel model, AsremlTrait trait, ExpFBKs fbks){
		try{
//			if(isConverged && summaries != null){
//				
//				//get Asr
//				AsrData asr = model.getOutputs().get(trait).getAsr();
//				Double varianceComponet = asr.getVarianceComponets("Variance", trait);
//				
//				//get average of other fixed effects
//				//get averages of effects
//				Summary firstSummary = summaries.iterator().next();
//				String colName = Slns.createColumnName(firstSummary.getFilters().keySet());
////				Entry<BigDecimal, Double> effectAverages = getFixedEffectsMean(model, colName, trait, fbks, null);
////				BigDecimal averageEffects = effectAverages.getKey();
//				
//				for(Summary summary : summaries){
//					Map.Entry<BigDecimal, Double> estimates = getAnalysisMean(model, summary.getFilters(), trait, fbks);
//					
//					BigDecimal estimate = estimates.getKey();
//					estimate = estimate.add(averageEffects);
//					
//					//get error
//					int count = Integer.parseInt(summary.getValues().get("rawCount").toString());
//					Double error = calcError(varianceComponet, count);
//					
//					summary.getValues().put("estimate", estimate.doubleValue());
//					summary.getValues().put("stdError", error);
//		
//					//CI
//					Double lowCI = null;
//					Double highCI = null;
//					if(null != estimate && null != error){
//						lowCI = estimate.doubleValue()-(1.95*error);
//						highCI = estimate.doubleValue()+(1.95*error);
//					}
//					summary.getValues().put("lowCI", lowCI);
//					summary.getValues().put("hiCI", highCI);
//				}
//			}
		}
		catch (Exception e) {
			logger.warn("",e);
		}
	}
	
//	private static Map.Entry<BigDecimal, Double> getAnalysisMean(AsremlModel model, LinkedHashMap<String, Integer> filters, AsremlTrait trait, ExpFBKs fbks){
//		Map.Entry<BigDecimal, Double> estimates = null;
//		BigDecimal estimate = new BigDecimal(0).setScale(ROUND);
//		Double error = 0d;
//		AsremlOutput asremlOutput = model.getOutputs().get(trait);
//		
//		String colName = Slns.createColumnName(filters.keySet());
//		colName = Slns.createColumnName(filters.keySet());
//		
//		Slns slns = asremlOutput.getSlns();
//		
//		for(Map.Entry<String,Integer> filter : filters.entrySet()){
//			//apply single effects
//			int[] ids = new int[1];
//			ids[0] = filter.getValue();
//			Sln sln = slns.get(filter.getKey(), ids);
//			estimate = estimate.add(new BigDecimal(sln.getEffect()).setScale(ROUND, ROUNDING_MODE));
//			error += Math.pow(sln.getError(), 2);
//
//			//add interactions
//			Map.Entry<BigDecimal, Double> interactionEffect = getFixedEffectsMean(model, colName, trait, fbks, ids[0]);
//			estimate = estimate.add(interactionEffect.getKey());
//			error += interactionEffect.getValue();
//		}
//			
//		Sln mu = slns.getMu();
//		if(null != mu){
//			estimate = estimate.add(new BigDecimal(mu.getEffect()).setScale(ROUND,ROUNDING_MODE));
//			error += Math.pow(mu.getError(), 2);
//		}
//		
//		error = Math.sqrt(error);
//		estimates = new AbstractMap.SimpleEntry<BigDecimal, Double>(estimate, error);
//		
//		return estimates;
//	}
	
	/***
	 * Add estimate, error, lowCI and hiCI to summary.
	 * @param summaries
	 * @param model
	 * @param trait
	 * @param fbks
	 */
	public static void getEstimates(boolean isConverged, Collection<Summary> summaries, AsremlModel model, AsremlTrait trait, ExpFBKs fbks, StatGroup statGroup){
		try{
			if(isConverged && summaries != null){
				
				//get Asr
				AsrData asr = model.getOutputs().get(trait).getAsr();
				Double varianceComponet = asr.getVarianceComponets("Variance", trait);
				
				for(Summary summary : summaries){
					Map.Entry<Double, Double> estimates = getAnalysisMean(model, summary.getFilters(), trait, fbks, statGroup);
					
					Double estimate = estimates.getKey();
					
					//get error
					int count = Integer.parseInt(summary.getValues().get("rawCount").toString());
					Double error = Estimate.calcError(varianceComponet, count);
					
					summary.getValues().put("estimate", estimate);
					summary.getValues().put("stdError", error);
		
					//CI
					Double lowCI = null;
					Double highCI = null;
					if(null != estimate && null != error){
						lowCI = estimate-(1.95*error);
						highCI = estimate+(1.95*error);
					}
					summary.getValues().put("lowCI", lowCI);
					summary.getValues().put("hiCI", highCI);
				}
			}
		}
		catch (Exception e) {
			logger.warn("",e);
		}
	}

	/***
	 * Add Mean and stdDev of Mean to the summary.
	 * @param summaries
	 * @param model
	 * @param trait
	 * @param fbks
	 */
	public static void getMean(boolean isConverged, Collection<Summary> summaries, AsremlModel model, AsremlTrait trait, ExpFBKs fbks, StatGroup statGroup){
		try{
			if(isConverged && summaries != null){
				
				//get Asr
				AsrData asr = model.getOutputs().get(trait).getAsr();
				Double varianceComponet = asr.getVarianceComponets("Variance", trait);
				
				for(Summary summary : summaries){
					Map.Entry<Double, Double> estimates = getAnalysisMean(model, summary.getFilters(), trait, fbks, statGroup);
					
					Double estimate = estimates.getKey();
					
					//get error
					int count = Integer.parseInt(summary.getValues().get("rawCount").toString());
					Double error = Estimate.calcError(varianceComponet, count);
					
					summary.getValues().put("mean", estimate);
					summary.getValues().put("stdDev", error);
				}
			}
		}
		catch (Exception e) {
			logger.warn("", e);
		}
	}

	/**
	 * u + base(i) + avg(blockId.year) + avg(year) + avg(event)
	 * 
	 * @param model
	 * @param filters
	 * @param trait
	 * @param fbks
	 * @param statGroup
	 * @return
	 * @throws Exception
	 */
	private static Map.Entry<Double, Double> getAnalysisMean(AsremlModel model, LinkedHashMap<String, Integer> filters, AsremlTrait trait, ExpFBKs fbks, StatGroup statGroup) throws Exception{
		Map.Entry<Double, Double> estimates = null;
		Double estimate = 0d;
		Double error = 0d;
		AsremlOutput asremlOutput = model.getOutputs().get(trait);

		String colName = Slns.createColumnName(filters.keySet());
		colName = Slns.createColumnName(filters.keySet());

		Slns slns = asremlOutput.getSlns();
		if(slns == null){
			throw new Exception("No solution");
		}

		//add in averages
		Map<Integer, Set<Integer>> locInYear = null;
		if(StatGroup.MULTI_ESTIMATE_BY_YEAR.equals(statGroup)){
			estimates = getEffectsMeanByYear(model, trait, fbks, null);
			locInYear = getLocInYear(fbks);
		}
		else{
			estimates = getEffectsMean(model, trait, fbks, null);
		}

		//add in Summary filter sub i values.
		int id = -1;
		for(Map.Entry<String,Integer> filter : filters.entrySet()){
			//apply single effects
			int[] ids = new int[1];
			ids[0] = filter.getValue();
			if(slns != null){
				Sln sln = slns.get(filter.getKey(), ids);
				if(sln != null){
					estimate += sln.getEffect();
					error += Math.pow(sln.getError(), 2);
				}
			}
			
			if(StatGroup.MULTI_ESTIMATE_BY_YEAR.equals(statGroup)){
				if(filter.getKey().equals("year")){
					EffectMean blockId = getEffectMean(model, "blockId.year", "year", trait, fbks, ids[0]);
					estimate += blockId.mean;
					error += blockId.error;
				}
				
				if(filter.getKey().equals("baseGeno")){
					Set<Integer> set = locInYear.get(filters.get("year"));
					EffectMean locBase = getLocBase(model, filters.get("baseGeno"), trait, set);
					estimate += locBase.mean;
					error += locBase.error;
				}
			}
			
			if(id == -1 ){
				id = ids[0];
			}
		}
		
//		if(id != -1){
//			Map.Entry<Double, Double> interactionEffect = getFixedEffectsMean(model, "year", trait, fbks, id);
//			estimate += interactionEffect.getKey();
//			error += interactionEffect.getValue();
//		}

		//add multiyear random
//		if(StatGroup.MULTI_ESTIMATE.equals(statGroup)){
//			int[] ids = new int[filters.size()];
//			int i =0;
//			for(Map.Entry<String,Integer> filter : filters.entrySet()){
//				ids[i++] = filter.getValue();	
//			}
//			String columnName = Slns.createColumnName(filters.keySet());
//			Sln sln = slns.get(columnName, ids);
//			estimate += sln.getEffect();
//			error += Math.pow(sln.getError(), 2);
//		}

		//add in mu
		Sln mu = slns.getMu();
		if(null != mu){
			estimate += mu.getEffect();
			error += Math.pow(mu.getError(), 2);
		}

		estimate += estimates.getKey();
		error += estimates.getValue();
		error = Math.sqrt(error);
		estimates = new AbstractMap.SimpleEntry<Double, Double>(estimate, error);
		
		return estimates;
	}
	
	private static Map.Entry<Double, Double> getEffectsMean(AsremlModel model, AsremlTrait trait, ExpFBKs fbks, Integer colId){
		int totalMvCount = 0;
		
		EffectMean blockId = getEffectMean(model, "blockId.year", trait, fbks, colId);
		EffectMean event = getEffectMean(model, "event", trait, fbks, colId);
		EffectMean year = getEffectMean(model, "year", trait, fbks, colId);

		Double mean = blockId.mean + event.mean + year.mean;
		Double error = blockId.error + event.error + year.error;
		
		return new AbstractMap.SimpleEntry<Double, Double>(mean, error);
	}
	
	private static Map.Entry<Double, Double> getEffectsMeanByYear(AsremlModel model, AsremlTrait trait, ExpFBKs fbks, Integer colId){
		int totalMvCount = 0;

		EffectMean event = getEffectMean(model, "event", trait, fbks, colId);
		//EffectMean year = getEffectMean(model, "locId.base", trait, fbks, colId);

		Double mean = event.mean;
		Double error = event.error;
		
		return new AbstractMap.SimpleEntry<Double, Double>(mean, error);
	}
	
	private static EffectMean getEffectMean(AsremlModel model, String effectName, AsremlTrait trait, ExpFBKs fbks, Integer colId){
		return getEffectMean(model, effectName, effectName, trait, fbks, colId);
	}
	
	private static EffectMean getLocBase(AsremlModel model, Integer colId, AsremlTrait trait, Set<Integer> locInYear){
		AsremlOutput asremlOutput = model.getOutputs().get(trait);
		Slns slnsData = asremlOutput.getSlns();
		Map<String, Map<int[], Sln>> slns = slnsData.getData();
		AsremlRandomEffects effects = model.getRandomEffects();
		
		String effectName = "locId.baseGeno";
		String colName = "baseGeno";
		AsremlRandomEffect effect = effects.getRandomEffect(effectName);
		
			
		Map<int[], Sln> slnValues = slns.get(effect.getName());
		
		Double mean = 0d;
		Double error = 0d;
		int nonMvCount = 0;
		List<String> ids = Arrays.asList(effect.getName().split("\\."));
		Integer idIndex = ids.indexOf(colName);
		for(Iterator<int[]> it = slnValues.keySet().iterator(); it.hasNext();){
			Sln sln = slnValues.get(it.next());
			if(sln.getEffect() == 0){
				continue;
			}
			if(colId==null || (colId != null && colId.equals(sln.getIds()[idIndex]) && locInYear.contains(sln.getIds()[0]))){
				mean += sln.getEffect();
				error = Math.pow(sln.getError(), 2);
				nonMvCount++;
			}
		}
	
		mean = mean/nonMvCount;
		error = error/nonMvCount;
		
		return new EffectMean(mean, error, nonMvCount);
	}
	
	private static EffectMean getEffectMean(AsremlModel model, String effectName, String colName, AsremlTrait trait, ExpFBKs fbks, Integer colId){
		AsremlOutput asremlOutput = model.getOutputs().get(trait);
		Slns slnsData = asremlOutput.getSlns();
		Map<String, Map<int[], Sln>> slns = slnsData.getData();
		AsremlRandomEffects effects = model.getRandomEffects();
		
		List<Double> effectMean = new ArrayList<Double>();
		List<Double> errorMean = new ArrayList<Double>();
		
		int totalMvCount = 0;

		AsremlRandomEffect effect = effects.getRandomEffect(effectName);
			
		Double tempMean = 0d;
		Double tempError = 0d;
			
		Map<int[], Sln> slnValues = slns.get(effect.getName());

		Map<String, Set<Integer>> mvExclude = new HashMap<String, Set<Integer>>();
		List<String> ids = Arrays.asList(effect.getName().split("\\."));
		for(String id : ids){
			if(colId == null || ( colId != null && !id.equals(colName))){
				for(ExpFBK fbk : fbks.getFbks()){
					if(fbk.getData().get(trait.toString()) != null){
						if(!mvExclude.containsKey(id)){
							mvExclude.put(id, new HashSet<Integer>());
						}
						mvExclude.get(id).add(fbk.getInt(id));
					}
				}
			}
		}
		
		int nonMvCount = 0;
		Integer idIndex = ids.indexOf(colName);
		for(Iterator<int[]> it = slnValues.keySet().iterator(); it.hasNext();){
			Sln sln = slnValues.get(it.next());
			if(sln.getEffect() == 0){
				continue;
			}
			if(colId==null || (colId != null && sln.getIds()[idIndex] == (colId))){
				tempMean += sln.getEffect();
				tempError = Math.pow(sln.getError(), 2);
				nonMvCount++;
			}
		}

//		for(Set<Integer> nonMv : mvExclude.values()){
//			nonMvCount *= nonMv.size();
//		}
			
		totalMvCount += nonMvCount;
		effectMean.add(tempMean);
		errorMean.add(tempError);
		
		Double mean = 0d;
		Double error = 0d;
		
		for(int i=0; i < effectMean.size();i++){
			mean += effectMean.get(i);
			error = errorMean.get(i);
		}
		
		if(totalMvCount != 0){
			mean = mean/totalMvCount;
			error = error/totalMvCount;
		}
		
		return new EffectMean(mean, error, nonMvCount);
	}
	
	private static Map<Integer, Set<Integer>> getLocInYear(ExpFBKs fbks){
		Map<Integer, Set<Integer>> result = new HashMap<>();
		List<Map<String, Object>> rs = fbks.get("SELECT distinct "+Funcs.quoteString("year") + "," + Funcs.quoteString("locId"), null, null);
		
		if(rs != null){
			for(Map<String, Object> row : rs){
				Object key = row.get("year");
				Integer id = fbks.findIndex("year", key, false);
				if(!result.containsKey(id)){
					result.put(id, new HashSet<Integer>());
				}
				Integer locId = fbks.findIndex("locId", row.get("locId"), false);
				result.get(id).add(locId);
			}
		}
		
		return result;
	}
}

class EffectMean{
	Double mean;
	Double error;
	int nonMvCount;
	
	EffectMean(Double mean, Double error, int nonMvCount) {
		this.mean = mean;
		this.error = error;
		this.nonMvCount = nonMvCount;
	}
}