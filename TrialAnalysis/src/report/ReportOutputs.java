package report;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import utils.Globals.SummaryType;
import data.xml.objects.Trait;

/**
 * Object to stored the data to be written as a report later.
 * Stores the data by grouping Summaries together by {@link SummaryType} and then by {@link Trait}.
 * This is a match the Asreml output which will be written as analysis/trait. Ie anova/yield then mad/yield
 * @author Scott Smith
 *
 */
public class ReportOutputs {

	Map<Trait,ReportOutput> reportOutputs;

	public Map<Trait, ReportOutput> getReportOutputs() {
		return reportOutputs;
	}

	public void setReportOutputs(Map<Trait, ReportOutput> reportOutputs) {
		this.reportOutputs = reportOutputs;
	}

	public ReportOutputs() {
		this.reportOutputs = new ConcurrentHashMap<Trait, ReportOutput>();
	}
	
	public ReportOutput get(Trait trait, boolean createNew){
		if(createNew){
			if(!reportOutputs.containsKey(trait)){
				reportOutputs.put(trait, new ReportOutput());
			}
		}
		return reportOutputs.get(trait);
	}
	
	public Collection<Summary> get(Trait trait, SummaryType type){
		ReportOutput output = get(trait, false);
		return output==null ? null : output.get(type);
	}
	
	public ReportOutput getFirst(){
		ReportOutput result = null;
		
		if(reportOutputs != null && !reportOutputs.values().isEmpty()){
			result = reportOutputs.values().iterator().next();
		}
		
		return result;
	}
}
