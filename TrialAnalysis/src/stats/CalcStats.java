package stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import asreml.AsremlTrait;
import asreml.input.AsremlModel;
import asreml.output.AsremlOutput;
import data.xml.objects.Analysis;
import data.xml.objects.Constants;
import data.xml.objects.Traits;
import data.xml.objects.Trial;
import report.ReportOutput;
import report.ReportOutputs;
import report.Summary;
import utils.Funcs;
import utils.Globals.StatGroup;
import utils.Globals.SummaryType;
import utils.Globals.TrialType;
import utils.Parallel;

/**
 * Each {@link Analysis} has a list of different summaries based on {@link SummaryType}
 * 
 * @author Scott Smith
 *
 */
public class CalcStats {

	static Logger log = Logger.getLogger(CalcStats.class.getName());
	/**
	 * Call calStats for each analysis run by the trial.  Note analysis sequence does matter 
	 * as with when Anova is calculated first and then if converged a Spatial model stats would 
	 * then overwrite certain Anova statistics.
	 * 
	 * @param reportOutputs - 
	 * @param trial
	 * @return
	 */
	public static ReportOutput calcStats(final ReportOutputs reportOutputs, final Trial trial){
		try{		
			//run for each model/trait/summary ie anove/yield/entrySummary
			for(final Analysis a : trial.getAnalyses()){
				CalcStats.calcStats(reportOutputs, a.getAsremlModel(), a.getSummaries(), trial);
    		}
		}
		catch (Exception e) {
			log.warn("", e);
		}
		long time = System.currentTimeMillis();
		ReportOutput reportOutput = finalizeStats(trial, reportOutputs);
		log.debug(String.format("TIME - %s = %s", "finalize", System.currentTimeMillis() - time));
		return reportOutput;
	}
	
	/**
	 * Start a new thread for each trait and block at the end waiting for all trait threads to finish before completing.
	 * 
	 * @param reportOutputs
	 * @param model
	 * @param reportSummaries
	 * @param trial
	 * @return
	 */
	public static ReportOutputs calcStats(final ReportOutputs reportOutputs, 
			final AsremlModel model, 
			final List<Summary> reportSummaries, 
			final Trial trial){
		try{
			//setup thread pool at define max number of threads
			Parallel parallel = new Parallel(trial.getConcurrentProcessMax());
			List<AsremlTrait> traits = model.getTraits();
			Collection<Runnable> tasks = new ArrayList<>();

			//create Runnable task for each trait.
			for(final AsremlTrait trait : traits){
				Runnable task = 
						new Thread(
								new Runnable() {
									public void run() {
										//return reportOutput for this trait if it doesn't exist make a new one 
										//and put it in the reportOutputs collection
										ReportOutput reportOutput = reportOutputs.get(Traits.INSTANCE.get(trait.toString()), true);

										//process the different summaries for this analysis, specified in the trial xml ie.  Entry,Location,Experiment,etc..
										if(null != reportSummaries){
											for(Summary summary : reportSummaries){
												try{
													Collection<Summary> summaries = null;

													if(SummaryType.outlierSummary.equals(summary.getType())){
														if(!reportOutput.getSummaryTypes().contains(SummaryType.outlierSummary)){
															reportOutput.add(SummaryType.outlierSummary, new ArrayList<Summary>());
														}
														summaries = reportOutput.get(summary.getType());
														Outlier.outlier(summaries, Traits.INSTANCE.get(trait.toString()), model.getOutputs().get(trait), 
																trial.getFbks(), trial.getTraits());
													}
													else{
														//init summaries for a new summary if there are no summaries for this trait/type combination
														if(!reportOutput.getSummaryTypes().contains(summary.getType())){
															Collection<String> filters = summary.getFilters().keySet();
															summaries = GeneralStats.createSummaries(trial.getFbks().getColMap(), 
																	Traits.INSTANCE.get(trait.toString()), summary.getType(), filters, trial);
															reportOutput.add(summary.getType(), summaries);
														}
														else{
															summaries = reportOutput.get(summary.getType());
														}

														if(null != summaries){
															processStats(trial, summary.getStatGroups(), model, summary.getFilters(), 
																	trait, summaries, reportOutput);
														}
													}
												}
												catch (Exception e) {
													log.warn(summary.getType(), e);
												}
											}
										}
									}
								});
				tasks.add(task);
			}

			parallel.run(tasks);
		}
		catch(Exception e){
			log.warn("", e);
		}
		return reportOutputs;
	}
	
	/**
	 * Threaded across analyses, traits and summaries.
	 * 
	 * @param trial
	 * @param statGroups
	 * @param model
	 * @param filters
	 * @param trait
	 * @param summaries
	 * @param reportOutput
	 */
	private static void processStats(Trial trial, List<StatGroup> statGroups, AsremlModel model, 
			LinkedHashMap<String, Integer> filters, AsremlTrait trait, 
			Collection<Summary> summaries, ReportOutput reportOutput){
		AsremlOutput output = model.getOutputs().get(trait);
		boolean isConverged = output.isConverged();
		
		//keep last analysis run
		if(summaries != null && isConverged){
			for(Summary summary : summaries){
				summary.getValues().put("analysisUsed", model.getTitle());
			}
		}
		
		for(StatGroup statGroup : statGroups){
			switch(statGroup){
				case TABULATE :
					Tabulate.getTabulateStatistics(summaries, trial.getFbks());
					break;
				case ENTRY_LS_MEAN :
					Estimate.getMean(isConverged, summaries, model, trait, trial.getFbks());
					break;	
				case EXL_LS_MEAN :
					Estimate.getMean(isConverged, summaries, model, trait, trial.getFbks());
					break;
				case MULTI_ESTIMATE :
					Estimate.getEstimates(isConverged, summaries, model, trait, trial.getFbks(), true);
					break;
				case ESTIMATES :
					Estimate.getEstimates(isConverged, summaries, model, trait, trial.getFbks(), false);
					break;
				case PREDICTION :
					Estimate.getPrediction(isConverged, summaries, model, trait);
					break;
				case MULTI_SA_ESTIMATE :
					EstimateMultiYearSA.getEstimates(isConverged, summaries, model, trait, trial.getFbks());
					break;
				case RANKS_GROUPED :
					GeneralStats.rankByGroup(summaries, trial.getFbks(), trial.getDataLevel().get("genoType"));
					break;
				case RANKS_RAW_GROUPED :
					GeneralStats.rankByGroup(summaries, trial.getFbks(), trial.getDataLevel().get("genoType"), 
							trial.getDataLevel().get("environment"));
					break;
				case RANKS :
					GeneralStats.getRanks(summaries, "estimate", filters, trial.getDataLevel().get("genoType"));
					break;			
				case RANKS_RAW :
					GeneralStats.getRanks(summaries, "rawMean", filters, trial.getDataLevel().get("genoType"));
					break;
				case CHECKS_PERCENT_MEAN :
					Checks.getChecksPercentGrouped(summaries, trial.getFbks(), trial.getCheckType(), "rawMean", 
							trial.getDataLevel().get("genoType"));
					break;
				case CHECKS_PERCENT_ESTIMATE :
					Checks.getChecksPercentGrouped(summaries, trial.getFbks(), trial.getCheckType(), "estimate", 
							trial.getDataLevel().get("genoType"));
					break;
				case CHECKS_PERCENT_DEVIATION :
					Checks.getChecksDeviation(summaries, trial.getFbks(), trial.getCheckType(), "estimate", trial.getDataLevel().get("genoType"));
					break;
				case CHECKS_RAW_MEAN_PERCENT_DEVIATION :
					Checks.getChecksDeviation(summaries, trial.getFbks(), trial.getCheckType(), "rawMean", trial.getDataLevel().get("genoType"));
					break;
				case GENETIC_CHECKS_PERCENT_ESTIMATE :
					Checks.calcGroupPctChecks(summaries, trial.getFbks(), trial.getDataLevel().get("genoType"));
					break;
				case GENETIC_CHECKS_PERCENT_MEAN :
					Checks.calcGroupPctChecks(summaries, trial.getFbks(), trial.getDataLevel().get("genoType"), 
							trial.getDataLevel().get("environment"));
					break;
				case CHECKS_MEAN :
					Checks.calcCheckMean(summaries, trial.getFbks().getCheckMeans(), 
							trial.getDataLevel().get("environment"), trial.getCheckType());
					break;
				case CHECKS_ESTIMATE :
					Checks.calcCheckMean(summaries, trial.getFbks().getCheckMeans(), 
							trial.getDataLevel().get("environment"), trial.getCheckType());
					break;
				case CV :
					CV.calcCV(summaries, output, trial.getFbks(), trial.getDataLevel());
					break;
				case EXPERIMENT :
					Experiment.calcExperiment(summaries, trial.getFbks(), reportOutput.get(SummaryType.entrySummary), 
							output, trial.getDataLevel(), trial.getCheckType());
					break;
				case NUMBER_LOCATIONS :
					if(trial.getDataLevel().get("genoType") != null){
						String[] filter = {"locId", trial.getDataLevel().get("genoType")};
						GeneralStats.numberOf(summaries, output.getTabs(), Arrays.asList(filter), "numberLocations");
					}
					break;
				case NUMBER_YEARS :
					if(trial.getDataLevel().get("genoType") != null){
						String[] filter = {"year", trial.getDataLevel().get("genoType")};
						GeneralStats.numberOf(summaries, output.getTabs(), Arrays.asList(filter), "numberYears");
					}
					break;
				case CAV :
					GeneralStats.CAV(summaries);
					break;
				case RAW_MEAN :
					GeneralStats.copyColumn(summaries, "rawMean", "mean");
					break;
				case BMR_TMF :
					//BMR
					LinkedHashMap<String, Integer> additiveFilter = new LinkedHashMap<String, Integer>();
					
					for(Entry<String, Integer> entry : filters.entrySet()){
						additiveFilter.put(entry.getKey(), entry.getValue());
					}
					additiveFilter.put("bmr", 2);
					Tabulate.getTabulateStatistic(summaries, additiveFilter, output.getTabs(), "hybridBmr", "mean");
					additiveFilter.remove("bmr");
					
					//TMF
					additiveFilter.put("bmr", 1);
					Tabulate.getTabulateStatistic(summaries, additiveFilter, output.getTabs(), "hybridTmf", "mean");
					break;
				case INCIDENCE :
					Incidence.calculateIncidence(summaries, trait, trial.getFbks(), trial);
					break;
				case SEVERITY:
					Severity.calculateSeverity(summaries, trait, trial.getFbks(), trial);
					break;
			default:
				break;
			}
		}
	}
	
	/***
	 * Perform any final statistics and format data for reports.
	 * 
	 * @param trial
	 * @param reportOutputs
	 * @return
	 */
	public static ReportOutput finalizeStats(Trial trial, final ReportOutputs reportOutputs){
		ReportOutput result = new ReportOutput();
		
		try{
			//stability, pi, and adjustCAV
			for(data.xml.objects.Trait trait : trial.getTraits()){
				ReportOutput reportOutput = reportOutputs.getReportOutputs().get(trait);
				if(null != reportOutput){
					Collection<Summary> exlSummaries = null;
					Collection<Summary> entrySummaries = null;
					Collection<Summary> locSummaries = null;
					Collection<Summary> expSummaries = null;
					if((exlSummaries = reportOutput.get(SummaryType.exlSummary)) != null){
						if((entrySummaries = reportOutput.get(SummaryType.entrySummary)) != null &&
								(expSummaries = reportOutput.get(SummaryType.experimentSummary)) != null){
							GeneralStats.calcPI(entrySummaries, exlSummaries, expSummaries, 
									Funcs.getInt(Constants.INSTANCE.getConstant("iqr_size").toString()), 
									trial.getDataLevel());
						}
						//remove loc Estimate from CAV
						if((locSummaries = reportOutput.get(SummaryType.locSummary)) != null){
							GeneralStats.adjustCAV(exlSummaries, locSummaries, trial.getDataLevel(), trial.getFbks());
						}
					}
				}
			}
			
			boolean doHeatmap = false;
			List<data.xml.objects.Trait> heatmapTraits = null;
			List<Summary> heatmapReportSummaries = new ArrayList<>();
						
			for(Analysis analysis : trial.getAnalyses()){
				for(Summary summary : analysis.getSummaries()){
					if(summary.getStatGroups() != null){
						for(StatGroup statGroup : summary.getStatGroups()){
							long time = System.currentTimeMillis();
							switch(statGroup){
							case EU_GRAIN_INDICES :
								EUIndexes.calcEuGrainIndices(trial, reportOutputs);
								break;
							case EU_SILAGE_INDICES :
								EUIndexes.calcEuSilageIndices(trial, reportOutputs);
								break;
							case PRM :
								GeneralStats.calcPRM(trial, reportOutputs);
								break;
							case SELECTION_INDEX :
								SelectionIndex.calcSelectionIndex(reportOutputs, trial);
								break;
							case PCT_WINS :
								GeneralStats.pctWins(trial, reportOutputs);
								break;
							case STRESS_INDEX : 
							case STRESS_INDEX_SILAGE :
							case STRESS_INDEX_2YD_EU :
							case STRESS_INDEX_2YD_LA :
								StressIndex.calcStressIndex(trial, reportOutputs, statGroup);
								break;
							case PERCENT_OF_THRESHOLD:
								GeneralStats.percentOfThreshold(reportOutputs.get(Traits.INSTANCE.get("pct_protein_meal_dm_nir"), SummaryType.entrySummary)
										, trial, "pct_protein_meal_dm_nir_win_percent" , trial.getDataLevel().get("genoType"), ">=", 50);
								break;
							default:
								break;
							}
							log.debug(String.format("TIME - %s = %s", statGroup, System.currentTimeMillis() - time));
						}
					}
					if (SummaryType.heatmapSummary.equals(summary.getType())) {
						doHeatmap = true;
						heatmapTraits = analysis.getTraits();
						heatmapReportSummaries.add(summary);
					}
				}
			}

						
			//multi only needs one so just put it on the first trait
			if(TrialType.MULTI.equals(trial.getType())){
				ReportOutput reportOutput = reportOutputs.getFirst();
				Collection<Summary> summaries = new ArrayList<Summary>();
				SummaryType type = MultiMap.multiMap(summaries, trial);
				reportOutput.add(type, summaries);
			}
			
			//reformat output for reporting
			//starts grouped by trait then SummaryType
			//end grouped by SummaryType and then trait.
			
			for(data.xml.objects.Trait trait : reportOutputs.getReportOutputs().keySet()){
				for(SummaryType summaryType : reportOutputs.getReportOutputs().get(trait).getSummaryTypes()){
					if(SummaryType.heatmapSummary.equals(summaryType)){
						continue; /* Do these later*/
					}
					else if(SummaryType.outlierSummary.equals(summaryType)){
						if(result.get(SummaryType.outlierSummary) == null){
							result.add(SummaryType.outlierSummary, new ArrayList<Summary>());
						}
						Outlier.combineOutliers(reportOutputs.getReportOutputs().get(trait).get(summaryType), 
								result.get(SummaryType.outlierSummary));
					}
					else{
						result.add(summaryType, reportOutputs.getReportOutputs().get(trait).get(summaryType));
					}
				}
			}
			
			long time = System.currentTimeMillis();
			/* Heatmap MUST be processed here since it has to be sequential with
			 * Outlier summary creation. */
			if (doHeatmap) {
				try{
					Parallel parallel = new Parallel(trial.getConcurrentProcessMax());
					Collection<Runnable> tasks = new ArrayList<>();
					final List<Summary> reportSummaries = heatmapReportSummaries;
					final Trial ftrial = trial;
					final Collection<Summary> outlierSummaries = result.get(SummaryType.outlierSummary);
					
					if(heatmapTraits != null){
						for (final data.xml.objects.Trait trait : heatmapTraits) {
							Runnable task = new Thread(
									new Runnable() {
										public void run() {
											ReportOutput reportOutput = 
													reportOutputs.get(Traits.INSTANCE.get(trait.toString()), 
															true);
	
											if(null != reportSummaries){
												for(Summary summary : reportSummaries){
													try{
														if(!reportOutput.getSummaryTypes().contains(SummaryType.heatmapSummary)){
															reportOutput.add(SummaryType.heatmapSummary, 
																	new ArrayList<Summary>());
														}
														HeatMaps.generateSummary(reportOutput.get(summary.getType()), 
																outlierSummaries, 
																Traits.INSTANCE.get(trait.toString()), ftrial);
													}
													catch (Exception e) {
														log.warn(summary.getType(), e);
													}
												}
												
											}
										}
									});
							tasks.add(task);
						} /* for (final data.xml.objects.Trait trait : heatmapTraits) */
						parallel.run(tasks); /* Blocks until all tasks are finished */
					}
					
				}
				catch(Exception e){
					log.warn("", e);
				}
			}
			log.debug(String.format("TIME - %s = %s", "heatmap", System.currentTimeMillis() - time));
			
			//pivot data into list of summary's grouped by Summary type from being grouped by trait for heatmaps
			Collection<Summary> tmp;
			for(data.xml.objects.Trait trait : reportOutputs.getReportOutputs().keySet()){
				for(SummaryType summaryType : reportOutputs.getReportOutputs().get(trait).getSummaryTypes()){
					if(SummaryType.heatmapSummary.equals(summaryType)){
						tmp = reportOutputs.getReportOutputs().get(trait).get(summaryType);
						result.add(summaryType, tmp);
					}
				}
			}
		}
		catch (Exception e) {
			log.warn("",e);
		}
		return result;
	}
}