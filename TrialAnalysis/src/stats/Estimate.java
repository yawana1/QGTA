package stats;

import java.math.BigDecimal;
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
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import asreml.AsremlTrait;
import asreml.input.AsremlFixedEffect;
import asreml.input.AsremlFixedEffects;
import asreml.input.AsremlModel;
import asreml.input.AsremlRandomEffect;
import asreml.input.AsremlRandomEffects;
import asreml.output.AsrData;
import asreml.output.AsremlOutput;
import asreml.output.Pv;
import asreml.output.Pvs;
import asreml.output.Sln;
import asreml.output.Slns;
import data.collection.ExpFBK;
import data.collection.ExpFBKs;
import report.Summary;
import utils.ObjectQuery;

/**
 * Calculate report Estimates, CI's, and LS Means
 * Currently calculating by averaging fixed effects.
 */
public class Estimate {

	static Logger logger = Logger.getLogger(Estimate.class.getName());
	protected static int ROUND = 6;
	protected static RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
	
	/***
	 * Add estimate, error, lowCI and hiCI to summary.
	 * @param summaries
	 * @param model
	 * @param trait
	 * @param fbks
	 * @param addRandomEffects 
	 */
	public static void getEstimates(boolean isConverged, Collection<Summary> summaries, AsremlModel model, AsremlTrait trait, ExpFBKs fbks, boolean addRandomEffects){
		try{
			if(isConverged && summaries != null){
				
				//get Asr
				AsrData asr = model.getOutputs().get(trait).getAsr();
				Double varianceComponet = asr.getVarianceComponets("Variance", trait);
				
				//get average of other fixed effects
				//get averages of effects
				Summary firstSummary = summaries.iterator().next();
				String colName = Slns.createColumnName(firstSummary.getFilters().keySet());
				Entry<BigDecimal, Double> effectAverages = getFixedEffectsMean(model, colName, trait, fbks, null);
				BigDecimal averageEffects = effectAverages.getKey();
				if(addRandomEffects){
					Entry<BigDecimal, Double> randomEffectAverages = getRandomEffectsMean(model, colName, trait, fbks, null);
					averageEffects.add(randomEffectAverages.getKey());
				}
				
				for(Summary summary : summaries){
					Map.Entry<BigDecimal, Double> estimates = getAnalysisMean(model, summary.getFilters(), trait, fbks);
					
					BigDecimal estimate = estimates.getKey();
					estimate = estimate.add(averageEffects);
					
					//get error
					int count = Integer.parseInt(summary.getValues().get("rawCount").toString());
					Double error = calcError(varianceComponet, count);
					
					summary.getValues().put("estimate", estimate.doubleValue());
					summary.getValues().put("stdError", error);
		
					//CI
					Double lowCI = null;
					Double highCI = null;
					if(null != estimate && null != error){
						lowCI = estimate.doubleValue()-(1.95*error);
						highCI = estimate.doubleValue()+(1.95*error);
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

	/**
	 * StdError = VarianceCompent/count
	 * @param varianceComponet
	 * @param count
	 * @return
	 */
	public static Double calcError(Double varianceComponet, int count){
		Double result = null;
		if(count > 0){
			result = Math.sqrt(varianceComponet/count);
		}
		return result;
	}

	/***
	 * Add Mean and stdDev of Mean to the summary.
	 * @param summaries
	 * @param model
	 * @param trait
	 * @param fbks
	 */
	public static void getMean(boolean isConverged, Collection<Summary> summaries, AsremlModel model, AsremlTrait trait, ExpFBKs fbks){
		try{
			if(isConverged && summaries != null){
				
				//get Asr
				AsrData asr = model.getOutputs().get(trait).getAsr();
				Double varianceComponet = asr.getVarianceComponets("Variance", trait);
				
				//get averages of effects
				Summary firstSummary = summaries.iterator().next();
				String colName = Slns.createColumnName(firstSummary.getFilters().keySet());
				Entry<BigDecimal, Double> effectAverages = getFixedEffectsMean(model, colName, trait, fbks, null);
				BigDecimal averageEffects = effectAverages.getKey();
				
				for(Summary summary : summaries){
					Entry<BigDecimal, Double> estimates = getAnalysisMean(model, summary.getFilters(), trait, fbks);
					
					BigDecimal estimate = estimates.getKey();
					estimate = estimate.add(averageEffects);
					
					//get error
					int count = Integer.parseInt(summary.getValues().get("rawCount").toString());
					Double error = calcError(varianceComponet, count);
					
					summary.getValues().put("mean", estimate.doubleValue());
					summary.getValues().put("stdDev", error);
				}
			}
		}
		catch (Exception e) {
			logger.warn("", e);
		}
	}

	private static Map.Entry<BigDecimal, Double> getAnalysisMean(AsremlModel model, LinkedHashMap<String, Integer> filters, AsremlTrait trait, ExpFBKs fbks){
		Map.Entry<BigDecimal, Double> estimates = null;
		BigDecimal estimate = new BigDecimal(0).setScale(ROUND);
		Double error = 0d;
		AsremlOutput asremlOutput = model.getOutputs().get(trait);
		
		String colName = Slns.createColumnName(filters.keySet());
		colName = Slns.createColumnName(filters.keySet());
		
		Slns slns = asremlOutput.getSlns();
		
		for(Map.Entry<String,Integer> filter : filters.entrySet()){
			//apply single effects
			int[] ids = new int[1];
			ids[0] = filter.getValue();
			Sln sln = slns.get(filter.getKey(), ids);
			estimate = estimate.add(new BigDecimal(sln.getEffect()).setScale(ROUND, ROUNDING_MODE));
			error += Math.pow(sln.getError(), 2);

			//add interactions
			Map.Entry<BigDecimal, Double> interactionEffect = getFixedEffectsMean(model, colName, trait, fbks, ids[0]);
			estimate = estimate.add(interactionEffect.getKey());
			error += interactionEffect.getValue();
		}
			
		Sln mu = slns.getMu();
		if(null != mu){
			estimate = estimate.add(new BigDecimal(mu.getEffect()).setScale(ROUND,ROUNDING_MODE));
			error += Math.pow(mu.getError(), 2);
		}
		
		error = Math.sqrt(error);
		estimates = new AbstractMap.SimpleEntry<BigDecimal, Double>(estimate, error);
		
		return estimates;
	}
	
	/***
	 * Add estimate, error, lowCI and hiCI to summary.  Double'd up to test Asreml prediction vs our estimate.
	 * @param summaries
	 * @param model
	 * @param trait
	 */
	public static void getPrediction(boolean isConverged, Collection<Summary> summaries, AsremlModel model, AsremlTrait trait){
		try{
			if(isConverged && summaries != null){
				AsremlOutput asremlOutput = model.getOutputs().get(trait);
				Pvs pvs = asremlOutput.getPvs();
				
				for(Summary summary : summaries){
					Map.Entry<BigDecimal, BigDecimal> predictionPair = predict(pvs, summary.getFilters()); //key:prediction value:error
					
					BigDecimal prediction = predictionPair.getKey();
					BigDecimal error = predictionPair.getValue();
					
					summary.getValues().put("estimate", prediction.doubleValue());
					summary.getValues().put("stdError", error.doubleValue());
		
					//CI
					Double lowCI = null;
					Double highCI = null;
					if(null != prediction && null != error){
						BigDecimal confidence = error.multiply(new BigDecimal(1.95));
						lowCI = prediction.subtract(confidence).doubleValue();
						highCI = prediction.add(confidence).doubleValue();
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
	
	/**
	 * Pull prediction from Asreml .pvs file
	 * 
	 * 
	 * @param pvs
	 * @param filters
	 * @return
	 */
	private static Map.Entry<BigDecimal, BigDecimal> predict(Pvs pvs, LinkedHashMap<String, Integer> filters){
		BigDecimal prediction = new BigDecimal(0);
		BigDecimal error = new BigDecimal(0);
		String colName = Pvs.createColumnName(filters.keySet());
		Collection<?> data = pvs.getPvs().get(colName);
		
		//Query data collection
		String[] select = {"effect","error"};
		List<String> entryFilter = new ArrayList<String>();
		int j = 0;
		for(String filter : filters.keySet()){
			entryFilter.add(" get(keys, " + j++ + ") = '" + filters.get(filter).toString() + "' ");
		}
		
		//query PV collection
		ObjectQuery query = new ObjectQuery( Arrays.asList(select), Pv.class.getName(), entryFilter, data);
		List<Object> results = query.execute();
		
		//add data to summary
		if(results.size() > 0){
			List<?> selectValues = (List<?>) results.get(0);
			if(selectValues.size() == 2){
				prediction = (BigDecimal)selectValues.get(0);
				error = (BigDecimal)selectValues.get(1);
			}
		}

		return new AbstractMap.SimpleEntry<BigDecimal, BigDecimal>(prediction, error);
	}

	private static Map.Entry<BigDecimal, Double> getFixedEffectsMean(AsremlModel model, String colName, AsremlTrait trait, ExpFBKs fbks, Integer colId){
		AsremlOutput asremlOutput = model.getOutputs().get(trait);
		Slns slnsData = asremlOutput.getSlns();
		Map<String, Map<int[], Sln>> slns = slnsData.getData();
		AsremlFixedEffects fixedEffects = model.getFixedEffects();
		
		List<BigDecimal> effectMean = new ArrayList<>();
		List<Double> errorMean = new ArrayList<Double>();
		
		int totalMvCount = 0;
		
		if(fixedEffects != null && fixedEffects.getFixedEffects() != null){
			for (AsremlFixedEffect fixedEffect : fixedEffects.getFixedEffects()) {
				String name = fixedEffect.getName();
				if(colName !=null && name.equals(colName)) //don't avg fixed effect of the column
					continue;
				if(name.contains("." + colName) || name.contains(colName + ".") ){ //used for interactions
					if(colId == null){
						continue;
					}
				}
				
				if(colId != null){
					if(!name.contains("." + colName)){
						if(!name.contains(colName + ".")){
							continue;
						}
					}
				}
				
				BigDecimal tempMean = new BigDecimal(0).setScale(ROUND);
				Double tempError = 0d;
				
				Map<int[], Sln> slnValues = slns.get(fixedEffect.getName());
	
				Map<String, Set<Integer>> mvExclude = new HashMap<String, Set<Integer>>();
				List<String> ids = Arrays.asList(fixedEffect.getName().split("\\."));
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
				
				Integer idIndex = ids.indexOf(colName);
				for(Iterator<int[]> it = slnValues.keySet().iterator(); it.hasNext();){
					Sln sln = slnValues.get(it.next());
					if(colId==null || (colId != null && sln.getIds()[idIndex] == (colId))){
						tempMean = tempMean.add(new BigDecimal(sln.getEffect()).setScale(ROUND, ROUNDING_MODE));
						tempError = Math.pow(sln.getError(), 2);
					}
				}
				
				int nonMvCount = 1;
				for(Set<Integer> nonMv : mvExclude.values()){
					nonMvCount *= nonMv.size();
				}
				
				totalMvCount += nonMvCount;
				effectMean.add(tempMean);
				errorMean.add(tempError);
			}
		}
		
		BigDecimal mean = new BigDecimal(0).setScale(ROUND);
		Double error = 0d;
		
		for(int i=0; i < effectMean.size();i++){
			mean = mean.add(effectMean.get(i));
			error = errorMean.get(i);
		}
		
		if(totalMvCount != 0){
			mean = mean.divide(new BigDecimal(totalMvCount), ROUND, ROUNDING_MODE);
			error = error/totalMvCount;
		}
		
//		Sln mu = getMuFromSln(slnsData);
//		if(null != mu){
//			mean += mu.getEffect();
//			error += Math.pow(mu.getError(), 2);
//		}
		
		return new AbstractMap.SimpleEntry<BigDecimal, Double>(mean, error);
	}
	
	private static Map.Entry<BigDecimal, Double> getRandomEffectsMean(AsremlModel model, String colName, AsremlTrait trait, ExpFBKs fbks, Integer colId){
		AsremlOutput asremlOutput = model.getOutputs().get(trait);
		Slns slnsData = asremlOutput.getSlns();
		Map<String, Map<int[], Sln>> slns = slnsData.getData();
		AsremlRandomEffects effects = model.getRandomEffects();
		
		List<BigDecimal> effectMean = new ArrayList<>();
		List<Double> errorMean = new ArrayList<Double>();
		
		int totalMvCount = 0;
		
		if(effects != null && effects.getRandomEffects() != null){
			for (AsremlRandomEffect effect : effects.getRandomEffects()) {
				String name = effect.getName();
				if(colName !=null && name.equals(colName)) //don't avg fixed effect of the column
					continue;
				if(name.contains("." + colName) || name.contains(colName + ".") ){ //used for interactions
					if(colId == null){
						continue;
					}
				}
				
				if(colId != null){
					if(!name.contains("." + colName)){
						if(!name.contains(colName + ".")){
							continue;
						}
					}
				}
				
				BigDecimal tempMean = new BigDecimal(0).setScale(ROUND);
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
				
				Integer idIndex = ids.indexOf(colName);
				for(Iterator<int[]> it = slnValues.keySet().iterator(); it.hasNext();){
					Sln sln = slnValues.get(it.next());
					if(colId==null || (colId != null && sln.getIds()[idIndex] == (colId))){
						tempMean = tempMean.add(new BigDecimal(sln.getEffect()).setScale(ROUND, ROUNDING_MODE));
						tempError = Math.pow(sln.getError(), 2);
					}
				}
				
				int nonMvCount = 1;
				for(Set<Integer> nonMv : mvExclude.values()){
					nonMvCount *= nonMv.size();
				}
				
				totalMvCount += nonMvCount;
				effectMean.add(tempMean);
				errorMean.add(tempError);
			}
		}
		
		BigDecimal mean = new BigDecimal(0).setScale(ROUND);
		Double error = 0d;
		
		for(int i=0; i < effectMean.size();i++){
			mean = mean.add(effectMean.get(i));
			error = errorMean.get(i);
		}
		
		if(totalMvCount != 0){
			mean = mean.divide(new BigDecimal(totalMvCount), ROUND, ROUNDING_MODE);
			error = error/totalMvCount;
		}
		
		return new AbstractMap.SimpleEntry<BigDecimal, Double>(mean, error);
	}
		
	public static Sln getMuFromSln(Slns slns){
		Map<int[], Sln> mus = slns.getData().get("mu");
		for(Object ids : mus.keySet()){
			if(ids != null && mus.get(ids) != null){
				return mus.get(ids);
			}
		}
	return null;
	}
}