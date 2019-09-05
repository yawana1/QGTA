package stats;

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

import report.Summary;
import asreml.AsremlTrait;
import asreml.input.AsremlEffect;
import asreml.input.AsremlModel;
import asreml.input.AsremlRandomEffects;
import asreml.output.AsrData;
import asreml.output.AsremlOutput;
import asreml.output.Sln;
import asreml.output.Slns;
import data.collection.ExpFBK;
import data.collection.ExpFBKs;

public class EstimateMultiYearSA {

	static Logger logger = Logger.getLogger(EstimateMultiYearSA.class.getName());
	
	/***
	 * Add estimate, error, lowCI and hiCI to summary.
	 * @param summaries
	 * @param model
	 * @param trait
	 * @param fbks
	 */
	public static void getEstimates(boolean isConverged, Collection<Summary> summaries, AsremlModel model, AsremlTrait trait, ExpFBKs fbks){
		try{
			if(isConverged && summaries != null){
				
				//get Asr
				AsrData asr = model.getOutputs().get(trait).getAsr();
				Double varianceComponet = asr.getVarianceComponets("Variance", trait);
				
				for(Summary summary : summaries){
					Map.Entry<Double, Double> estimates = getAnalysisMean(model, summary.getFilters(), trait, fbks);
					
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
	public static void getMean(boolean isConverged, Collection<Summary> summaries, AsremlModel model, AsremlTrait trait, ExpFBKs fbks){
		try{
			if(isConverged && summaries != null){
				
				//get Asr
				AsrData asr = model.getOutputs().get(trait).getAsr();
				Double varianceComponet = asr.getVarianceComponets("Variance", trait);
				
				for(Summary summary : summaries){
					Map.Entry<Double, Double> estimates = getAnalysisMean(model, summary.getFilters(), trait, fbks);
					
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

	private static Map.Entry<Double, Double> getAnalysisMean(AsremlModel model, LinkedHashMap<String, Integer> filters, AsremlTrait trait, ExpFBKs fbks){
		Map.Entry<Double, Double> estimates = null;
		Double estimate = 0d;
		Double error = 0d;
		AsremlOutput asremlOutput = model.getOutputs().get(trait);

		String colName = Slns.createColumnName(filters.keySet());
		colName = Slns.createColumnName(filters.keySet());

		estimates = getEffectAverage(model, colName, trait, fbks, null);

		int id = -1;
		for(Map.Entry<String,Integer> filter : filters.entrySet()){
			//apply single effects
			int[] ids = new int[1];
			ids[0] = filter.getValue();
			Slns slns = asremlOutput.getSlns();
			if(slns != null){
				Sln sln = slns.get(filter.getKey(), ids);
				if(sln != null){
					estimate += sln.getEffect();
					error += Math.pow(sln.getError(), 2);
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

		//add interactions
		if(filters.size() > 1){
			int[] ids = new int[filters.size()];
			int i =0;
			for(Map.Entry<String,Integer> filter : filters.entrySet()){
				ids[i++] = filter.getValue();	
			}
			String columnName = Slns.createColumnName(filters.keySet());
			Sln sln = asremlOutput.getSlns().get(columnName, ids);
			estimate += sln.getEffect();
			error += Math.pow(sln.getError(), 2);
		}

		//mu
		Sln mu = Estimate.getMuFromSln(asremlOutput.getSlns());
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
	
	private static Map.Entry<Double, Double> getEffectAverage(AsremlModel model, String colName, AsremlTrait trait, ExpFBKs fbks, Integer colId){
		AsremlOutput asremlOutput = model.getOutputs().get(trait);
		Slns slnsData = asremlOutput.getSlns();
		Map<String, Map<int[], Sln>> slns = slnsData.getData();
		AsremlRandomEffects effects = model.getRandomEffects();
		
		List<Double> effectMean = new ArrayList<Double>();
		List<Double> errorMean = new ArrayList<Double>();
		
		int totalMvCount = 0;
		
		for (AsremlEffect effect : effects.getRandomEffects()) {
			String name = effect.getName();
			boolean skip = false;
			for(String col : colName.split("\\.")){
				if(name.contains(col)){
					skip = true;
				}
			}
			
			if(skip){
				continue;
			}
			
			Double tempMean = 0d;
			Double tempError = 0d;
			
			Map<int[], Sln> slnValues = slns.get(name);

			Map<String, Set<Integer>> mvExclude = new HashMap<String, Set<Integer>>();
			List<String> ids = Arrays.asList(name.split("\\."));
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
					tempMean += sln.getEffect();
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
		
		return new AbstractMap.SimpleEntry<Double, Double>(mean, error);
	}
}