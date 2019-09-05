package report;

import java.util.ArrayList;
import java.util.List;

import utils.Globals.SummaryType;
import utils.Globals.TrialType;
import data.xml.objects.Trait;
import data.xml.objects.Trial;

/**
 * Write outlier values back to Variety.
 * Outlier_Flag is set to true or false and then the outlier_trait_s is the comma seperated list of traits that are outliers.
 * 
 * @author Scott Smith
 *
 */
public class OutputHeatmap extends OutputVariety{
	
	/**
	 * Define update sql to be run.
	 */
	protected boolean init(Trial trial) throws Exception{
		sql = " UPDATE fb SET ? = ? FROM EXP_FBK fb WHERE p_rowid = ? ;";
		summaryType = SummaryType.heatmapSummary;
		
		boolean init = trial.isDoHeatMap();
		init = TrialType.MULTI == trial.getType() ? false : init;
		
		return init;
	}

	/**
	 * Set the query params.  Write spatial adjusted value on the heap map to adj column in Variety
	 */
	protected List<Object> createParams(Summary summary, int size) {
		List<Object> param = null;

		if(summary.getValues().containsKey("estimate")){
			Trait trait = summary.getTrait();
			String varietyAdjName = trait.getVarietyAdjName(); 
			if(varietyAdjName != null){
				param = new ArrayList<Object>(size);
				param.add(varietyAdjName);
				param.add(summary.getValues().get("estimate"));
				param.add(summary.getValues().get("rowId"));
			}
		}
		return param;
	}
}