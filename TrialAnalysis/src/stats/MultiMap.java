package stats;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import data.xml.objects.Trial;
import report.Summary;
import utils.Globals.SummaryType;

public class MultiMap {

	static Logger logger = Logger.getLogger(GeneralStats.class.getName());
	
	public static SummaryType multiMap(Collection<Summary> summaries, Trial trial){
		SummaryType type = null;
		try{
						
			List<data.xml.objects.Experiment> experiments = trial.getExperiments();
			if(experiments == null || experiments.size() == 0){
				experiments = GeneralStats.getExperiments(trial);
			}
			
			boolean isMultiYear = GeneralStats.isMultiYear(experiments);
			
			for(data.xml.objects.Experiment experiment : experiments){
				SummaryType summaryType = isMultiYear ? SummaryType.multiyearMapSummary : SummaryType.multiMapSummary;
				Summary summary = new Summary(summaryType);
				summary.getValues().put("name", trial.getTrialName());
				summary.getValues().put("seasonId", trial.getSeasonId());
				summary.getValues().put("experimentName", experiment.getName());
				summary.getValues().put("experimentSeasonId", experiment.getSeason().getSeasonId());

				if(trial.getExpZone() != null){
					summary.getValues().put("zone", trial.getExpZone());					
				}
				if(trial.getRegion() != null){
					summary.getValues().put("region", trial.getRegion());
				}
				else{
					summary.getValues().put("region", experiment.getRegion());
				}
				type = summary.getType();
				summaries.add(summary);
			}
		}
		catch(Exception e){
			logger.warn("", e);
		}
		return type;
	}
}