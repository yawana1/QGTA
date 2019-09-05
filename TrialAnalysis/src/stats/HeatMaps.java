package stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import data.collection.ExpFBK;
import data.collection.ExpFBKs;
import data.xml.objects.Trait;
import data.xml.objects.Trial;
import report.Summary;

public class HeatMaps {
	static Logger log = Logger.getLogger(CalcStats.class.getName());
//	static int count = 0;
	/**
	 * 
	 * @param summaries
	 * @param trait
	 * @param trial
	 */
	public static void generateSummary(Collection<Summary> summaries, 
			Collection<Summary> outlierSummaries,
			Trait trait, Trial trial) {
		ExpFBKs fbks = trial.getFbks(); 
		
		for (ExpFBK fbk : fbks.getFbks()) {
			createHeatmapSummary(fbk.getInt("rowId"), summaries, outlierSummaries, trait, fbk, fbks);
		}
		
	}
	
	/**
	 * Create individual summary for a single trait in a single row
	 *  
	 * @param rowId - Row identifier number
	 * @param summaries - Collection of Summary objects
	 * @param trait - Trait to be added to Summary
	 * @param fbk
	 * @param fbks
	 * @return
	 */
	private static void createHeatmapSummary(Integer rowId,
			Collection<Summary> summaries,
			Collection<Summary> outlierSummaries,
			Trait trait,
			ExpFBK fbk,
			ExpFBKs fbks) {
		Summary result = null;

		/* Already exists in summary */
		if(null != summaries){
			for(Summary summary : summaries){
				if(rowId.toString().equals(summary.getValues().get("rowId"))){
					result = summary;
					break;
				}
			}
		}
		else{
			summaries = new ArrayList<Summary>();
		}

		boolean isOutlier = false;
		int outlierRowId;

		for(Summary summary : outlierSummaries) {
			outlierRowId = Integer.parseInt(summary.getValues().get("rowId").toString());
			String traitList[] = summary.getValues().get("outlierTraits").toString().split(",");
			for (String traitStr : traitList) {
				if (rowId == outlierRowId && trait.equals(traitStr)) {
					isOutlier = true;
					break;
				}
			}
		}
		
		Object objValue;
		if(null != result){
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("traitVatString", trait.getVatName());
			values.put("locId", fbk.getData().get("locId"));
			values.put("blockId", fbk.getData().get("blockId"));
			values.put("locName", fbk.getData().get("locationName").toString().replace(',',';'));
			values.put("range", fbk.getData().get("range"));
			values.put("pass", fbk.getData().get("pass"));
			values.put("outlier", isOutlier ? 1 : 0);
			values.put("rowId", rowId);
			
			objValue = fbk.getData().get(trait.toString());
			if (objValue == null) {
				objValue = -1;
				values.put("mean", objValue);
				values.put("estimate", objValue);
			}
			else
			{
				values.put("mean", objValue);
				//objValue = SpatialEstimate.getHeatMapAdjustment(trait, fbks, fbk);
				if (objValue == null)
				{
					values.put("estimate",-1);
				}
				else {
					values.put("estimate", objValue);
				}
					
			}
			
			result.setValues(values);
		}
	}

}
