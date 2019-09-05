package data.xml.objects;

import java.util.List;

import report.Summary;
import asreml.input.AsremlModel;

/**
 * Defines what model {@link AsremlModel} to run on what traits {@link Trait} and where the xml template for that model is defined
 * {@link StatModel}.  Also contains which summaries and what statistics are to be calculated for each summary as define by the analysis
 * section in the Trial.xml file.
 * 
 * @author Scott Smith
 *
 */
public class Analysis {

	private StatModel model; //location of model config file
	private List<Trait> traits; //Traits to run
	private AsremlModel asremlModel; //actual asreml model to run
	private List<Summary> summaries; //which SummaryTypes and the statistics on each type
	
	public StatModel getModel() {
		return model;
	}
	public void setModel(StatModel model) {
		this.model = model;
	}
	public List<Trait> getTraits() {
		return traits;
	}
	public void setTraits(List<Trait> traits) {
		this.traits = traits;
	}
	public AsremlModel getAsremlModel() {
		return asremlModel;
	}
	public void setAsremlModel(AsremlModel asremlModel) {
		this.asremlModel = asremlModel;
	}
	public List<Summary> getSummaries() {
		return summaries;
	}
	public void setSummaries(List<Summary> summaries) {
		this.summaries = summaries;
	}
	
	public Analysis(){
		
	}
}
