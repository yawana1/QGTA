package stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import report.Summary;
import utils.Globals.CheckType;
import utils.ObjectQuery;
import asreml.Conversion;
import asreml.AsremlTrait;
import asreml.output.AsrAnalysisOfVariance;
import asreml.output.AsrData;
import asreml.output.AsrVariance;
import asreml.output.AsremlOutput;
import asreml.output.Tab;
import asreml.output.Tabs;
import data.collection.ExpFBKs;
import data.xml.objects.Checks;

/**
 * Calculate all statistics for the Experiment Summary
 * See properties/Vat.xml to see the current set of stats that are sent to Vat.
 * 
 * @author Scott Smith
 *
 */
public class Experiment {

	private static Logger log = Logger.getLogger(Experiment.class.getName());
	
	public static void calcExperiment(Collection<Summary> summaries, ExpFBKs fbks, Collection<Summary> entrySummaries, AsremlOutput asremlOutput, Map<String,String> dataLevel, CheckType checkType){
		try{
			if(!asremlOutput.isConverged()){
				if(summaries != null){
					for(Iterator<Summary> summary = summaries.iterator(); summary.hasNext();){
						if(summary.next().getValues().get("analysisUsed") == null){
							summary.remove(); //don't need unconverged data
						}
					}
				}
				return;
			}
			for(Summary summary : summaries){
				String environment = dataLevel.get("environment");
				String genoType = dataLevel.get("genoType");
				
				//F stats from asr
				AsrData asr = asremlOutput.getAsr();
				
				Double entryF = null;
				Double entryP = null;
				Double locF = null;
				Double genoTypeVarianceComponent = null;
				Double gxeVarianceComponenet = null;
				Double lsd = null;
				Double broadHeritability =  null;
				Double narrowHeritability = null;
				Double errorVarianceComponent = null;
				
				//number of locations
				String[] ids = {environment};
				String tabName = Tabs.createId(ids);
				Tabs tabs = asremlOutput.getTabs();
				Collection<Tab> tabLoc = tabs.getTabs().get(tabName);
				Integer numberLocations = null;
				if(null != tabLoc){
					numberLocations = tabLoc.size();
				}
				
				//experiment estimates
				Double experimentEstimate = 0d;
				
				//Experiment Estimate avg entrySummary estimates
				if(entrySummaries != null){
					for(Summary entrySummary : entrySummaries){
						experimentEstimate += entrySummary.getEstimate() == null ? 0 : entrySummary.getEstimate();
					}
					experimentEstimate = experimentEstimate / entrySummaries.size();
				}
				
				//checks
				Checks checks = stats.Checks.getCheckMean(entrySummaries, fbks, checkType, "estimate", genoType);

				//variance components
				if(asr != null){
					//Variances
					AsrAnalysisOfVariance genoTypeAnalysisVariance = asr.getAnalysisOfVariance().get(genoType);
					AsrAnalysisOfVariance locTypeAnalysisVariance =  asr.getAnalysisOfVariance().get(environment);
					if(null != genoTypeAnalysisVariance){
						entryF = genoTypeAnalysisVariance.getfCon();
						entryP = genoTypeAnalysisVariance.getPDouble();
					}
					if(null != locTypeAnalysisVariance){
						locF = locTypeAnalysisVariance.getfCon();
					}
					
					AsremlTrait trait = Conversion.trait(summary.getTrait());
					Map<String, AsrVariance> varMatrix = asr.getVariance(trait) ;
					AsrVariance errorVariance = varMatrix.get("Variance");
					AsrVariance genoTypeVariance = varMatrix.get(dataLevel.get("genoType"));
					AsrVariance gxeVariance = varMatrix.get(environment + "." + genoType);
				
					if(null != genoTypeVariance){
						genoTypeVarianceComponent = genoTypeVariance.getComponent();
					}
	
					if(null != gxeVariance){
						gxeVarianceComponenet = gxeVariance.getComponent();
					}
					
					//heritability
					if(null != errorVariance && null != numberLocations){
						errorVarianceComponent = errorVariance.getComponent();
						lsd = 1.96* Math.sqrt(2*errorVarianceComponent/numberLocations);
	
						if(gxeVarianceComponenet != null){
							broadHeritability = broadHeritability(entryF, errorVarianceComponent, gxeVarianceComponenet, nZero(asremlOutput.getTabs(), genoType, environment), numberLocations);
						}
						else{
							broadHeritability = broadHeritability(entryF, numberLocations);
						}
						narrowHeritability = narrowHeritability(entryF);
					}
				}

				//set values to the experiment summary
				summary.getValues().put("entryF", entryF);
				summary.getValues().put("locF", locF);
				summary.getValues().put("fProbability", entryP);
				summary.getValues().put("experimentComponent", genoTypeVarianceComponent);
				summary.getValues().put("gxeComponent", gxeVarianceComponenet);
				summary.getValues().put("errorVarianceComponent", errorVarianceComponent);
				summary.getValues().put("LSD", lsd);
				summary.getValues().put("numberLocations", "" + numberLocations);
				summary.getValues().put("experimentEstimate", "" + experimentEstimate);
				summary.getValues().put("broadSenseHeritability", "" + broadHeritability);
				summary.getValues().put("narrowSenseHeritability", "" + narrowHeritability);
				if(checks != null){
					summary.getValues().put("coreCheckMean", checks.getAvgCore(checkType) == 0 ? null : checks.getAvgCore(checkType));
					summary.getValues().put("perfCheckMean", checks.getAvgPerf(checkType) == 0 ? null : checks.getAvgPerf(checkType));
					summary.getValues().put("geneticCheckMean", checks.getAvgGenetic(checkType) == 0 ? null : checks.getAvgGenetic(checkType));
					summary.getValues().put("bmrCheckMean", checks.getAvgBmr(checkType) == 0 ? null : checks.getAvgBmr(checkType));
					summary.getValues().put("susceptableCheckMean", checks.getAvgSusceptable(checkType) == 0 ? null : checks.getAvgSusceptable(checkType));
				}
			}
		}
		catch (Exception e) {
			log.warn("", e);
		}
	}
	
	/**
	 * No GxE random effect
	 * 	  ( 1 )
	 * 		/
	 * (1 + n / (F - 1))
	 * @param fStat
	 * @param errorVariance
	 * @param totalCount
	 * @return
	 */
	public static Double broadHeritability(Double fStat, Integer totalCount){
		Double heritability = null;
		
		if(fStat != null && totalCount != null && totalCount != 0){
			heritability = ( 1 ) / ( 1 + totalCount / (fStat - 1) );
		}
		
		return heritability;
	}
	
	/**
	 * Uses GxE component
	 * 		F - 1 
	 * 		/
	 * 		( F - 1 + (nZero * numberOfLocations * gxeVariance + nZero * numberOfLocations * errorVariance)
	 * 													/
	 * 								nZero * gxeVariance * errorVariance
	 *      )
	 * @param fStat
	 * @param errorVariance
	 * @param varianceGxE
	 * @param entryCount
	 * @return
	 */
	public static Double broadHeritability(Double fStat, Double errorVariance, Double varianceGxE, Double nZero, Integer locationCount){
		Double heritability = null;
		
		if(fStat != null && errorVariance != null && varianceGxE != null && nZero != null && locationCount != null){
			Double denominator = ( (nZero * locationCount * varianceGxE) + ( nZero * locationCount * errorVariance ) );
			denominator = denominator / ( nZero * varianceGxE + errorVariance);
			denominator = (fStat - 1) + denominator;
			heritability = (fStat - 1) /  denominator;
		}
		return heritability;
	}
	
	/***
	 * (F - 1) / F
	 * @param fStat
	 * @return
	 */
	public static Double narrowHeritability(Double fStat){
		Double heritability = null;
		if(fStat != null && fStat != 0d)
		heritability = (fStat - 1) / fStat;
		if(heritability != null && heritability < 0){
			heritability = 0d;
		}
		
		return heritability;
	}

	public static Double nZero(Tabs tabs, String genoType, String environment){
		String[] ids = {environment, genoType};
		String colName = Tabs.createId(ids);
		Collection<Tab> data = tabs.getTabs().get(colName);
		List<String> group = new ArrayList<String>();
		group.add(" get(keys, 1) ");
		ObjectQuery query = new ObjectQuery( " * ", Tab.class.getName(), null, group, data);
		List<List<String>> environmentIds = query.execute();
		
		double totalAvg = 0d;
		for(List<String> row : environmentIds){
			for(String id : row){
				List<String> where = new ArrayList<String>(); 
				where.add(" get(keys, 1) = '"+ id +"'");
				List<String> select = new ArrayList<String>();
				select.add(" sum(count) AS totalSum ");
				select.add(" count(count) AS totalCount ");
				query = new ObjectQuery(select, Tab.class.getName(), where, data);
				List<List<Object>> totals = query.execute();
				
				
				if(totals.size() > 0 ){
					List<Object> results = totals.get(0);
					Double entries = Double.parseDouble(results.get(1).toString());
					double locAvg = Double.parseDouble(results.get(0).toString())/entries;
					totalAvg += locAvg;
				}
			}
		}
		totalAvg /= environmentIds.size();
		
		return totalAvg;
	}
	
	/***
	 * Alternative calc method
	 * 
	 * N - (E n^2/N)
	 *  	/
	 *   (k -1)
	 * 
	 * @param entryCount
	 * @return
	 */
	public static Double nZero(Collection<Integer> entryCount){
		Double result = 0d;
		
		int totalObservations = 0;
		int repCount = entryCount.size();
		for(Integer count : entryCount){
			totalObservations += count;
		}
		
		double k = 0d;
		for(Integer count : entryCount){
			k = Math.pow(count, 2) / totalObservations;
		}
		
		result = (totalObservations - k) / (repCount - 1);
		return result;
	}
}