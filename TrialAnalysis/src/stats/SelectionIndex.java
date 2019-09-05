package stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;

import report.ReportOutputs;
import report.Summary;
import utils.Globals.SummaryType;
import data.xml.objects.Trait;
import data.xml.objects.Traits;
import data.xml.objects.Trial;

/**
 * Calculate Selection Index
 * 	1.25*EST_YIELD + -3*EST_MOISTURE + 1.5*EST_TEST_WT.
 */
public class SelectionIndex {

	private static Logger log = Logger.getLogger(SelectionIndex.class);
	
	public static void calcSelectionIndex(ReportOutputs reportOutputs, Trial trial){
		try{
			Collection<Summary> entryYld = reportOutputs.get(Traits.INSTANCE.get("yield"), false).get(SummaryType.entrySummary);
			Collection<Summary> entryMst = reportOutputs.get(Traits.INSTANCE.get("moisture"), false).get(SummaryType.entrySummary);
			Collection<Summary> entryTestWT = reportOutputs.get(Traits.INSTANCE.get("test_wt"), false).get(SummaryType.entrySummary);
			
			//skip if values don't exist for any data point
			if(entryYld==null || entryMst==null || entryTestWT==null){
				return;
			}
			
			String genoType = trial.getDataLevel().get("genoType");
			List<Object> genoIds = trial.getFbks().getColMap(genoType);
			Collection<Summary> collection = new ArrayList<Summary>();
			Trait trait = Traits.INSTANCE.get("selection_index");
			for(Iterator<Object> git = genoIds.iterator(); git.hasNext();){
				try{
					Object genoObj = git.next();
					Integer genoId = Integer.parseInt(genoObj.toString());
					Integer genoIndex = GeneralStats.getGenoIndex(trial, genoType, genoObj);
					LinkedHashMap<String, Integer> filter = new LinkedHashMap<String, Integer>();
					filter.put(genoType, genoIndex);
					
					double estimate = 0d;
					
					Summary summaryYield = Summary.search(entryYld, filter);
					Summary summaryMoisture = Summary.search(entryMst, filter);
					Summary summaryTestWt = Summary.search(entryTestWT, filter);
						
					Double estimateYield = Summary.getSummaryValue(summaryYield,"estimate");
					Double estimateMoisture = Summary.getSummaryValue(summaryMoisture,"estimate");
					Double estimateTestWt = Summary.getSummaryValue(summaryTestWt,"estimate");
						
					estimate = 		(1.25d * estimateYield) 
							+ 	(-3d * estimateMoisture)
							+ 	(1.5d * estimateTestWt)
							;

					Summary summaryIndex = GeneralStats.createEntrySummary(trait, SummaryType.entrySummary, genoType, genoId, trial);
					summaryIndex.getValues().put("estimate", estimate);
					collection.add(summaryIndex);
				}catch(Exception e){
					log.warn(e.getMessage(), e);
				}
			}
			if(collection.size()>0){
				reportOutputs.get(trait, true).add(SummaryType.entrySummary, collection);
			}
		}
		catch (Exception e) {
			log.warn("", e);
		}
	}
}