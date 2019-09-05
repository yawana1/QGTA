package utils;

/**
 * Constant and Enums
 * 
 * @author Scott Smith
 *
 */
public class Globals {

	//need to be small caps for the covariance engine
	public static enum GrmType{
		 hybrid
		,inbred
	}
	
	public static enum TrialType{
		 SINGLE
		,MULTI
	}
	
	public static enum CheckType{
		AVG
		,MAX
	}
	
	public static enum SummaryType{
		 entrySummary
		,exlSummary
		,locSummary
		,experimentSummary
		,outlierSummary
		,g1cSummary
		,multiMapSummary
		,groupSummary
		,multiYearSummary
		,bxySummary
		,baseGeneticsMapSummary
		,multiyearMapSummary
		,heatmapSummary
	}
	
	public static enum StatGroup{
		 TABULATE
		,RAW_MEAN
		,ESTIMATES
		,PREDICTION
		,ENTRY_SPATIAL_ESTIMATES
		,LOC_SPATIAL_ESTIMATES
		,RANKS
		,CHECKS
		,ENTRY_LS_MEAN
		,EXL_LS_MEAN
		,CHECKS_PERCENT_ESTIMATE
		,CHECKS_PERCENT_DEVIATION
		,CHECKS_RAW_MEAN_PERCENT_DEVIATION
		,CHECKS_PERCENT_MEAN
		,CHECKS_ESTIMATE
		,CHECKS_MEAN
		,CV
		,STABILITY
		,EXPERIMENT
		,OUTLIERS
		,RANKS_RAW
		,NUMBER_LOCATIONS
		,CAV
		,CAV_SPATIAL
		,EU_GRAIN_INDICES
		,EU_SILAGE_INDICES
		,RANKS_GROUPED
		,RANKS_RAW_GROUPED
		,GENETIC_CHECKS_PERCENT_ESTIMATE
		,GENETIC_CHECKS_PERCENT_MEAN
		,BMR_TMF
		,PRM
		,PCT_WINS
		,NUMBER_YEARS
		,MULTI_ESTIMATE
		,SUMS_MULTI
		,MULTI_SA_ESTIMATE
		,HEATMAP
		,STRESS_INDEX
		,SELECTION_INDEX
		,MULTI_ESTIMATE_BY_YEAR
		,STRESS_INDEX_SILAGE
		,STRESS_INDEX_2YD_EU
		,STRESS_INDEX_2YD_LA
		,GENE_CALL
		,MEDIAN
		,INCIDENCE
		,SEVERITY
		,PERCENT_OF_THRESHOLD
	}
	
	public static enum Outlier{
		YES(2602)
		, NO(2603)
		,NULL(null);
		private final Integer value;
		public Integer value(){return value;}

		Outlier(Integer value){
			this.value = value;
		}
	}
	
	public static enum JobTypes{
		 trialdefinition
		,trialanalysis
		,queueToCluster
		;
	}
	
	public static enum UnitTypes{
		INCHES
		, FEET
	}
	public final static String ARCHIVE_DATE_FORMAT = "yyyyMMddHHmmss";
	public static final String ARCHIVE_FOLDER_PREFIX = "ARCHIVE_";
}