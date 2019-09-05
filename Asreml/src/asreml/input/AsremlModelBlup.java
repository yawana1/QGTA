package asreml.input;

import java.util.Map;

import asreml.AsremlTrait;

/**
 * The Class AsremlModel is used to define an ASREML command file for multivariate blup jobs.
 */
public class AsremlModelBlup extends AsremlModel{
	
	//The Random Effect, trait variance value used as start values.
	//Ex Block_ID, yield, 159
	private Map<String, Map<AsremlTrait, Double>> varianceStartValues;
	
	public AsremlModelBlup(AsremlModel asremlModel){
		super(asremlModel);
	}
	
	public Map<String, Map<AsremlTrait, Double>> getVarianceStartValues() {
		return varianceStartValues;
	}

	public void setVarianceStartValues(
			Map<String, Map<AsremlTrait, Double>> varianceStartValues) {
		this.varianceStartValues = varianceStartValues;
	}

	/**
	 * Set the RandomEffects and also add in the variance numbers from the univar runs.
	 */
	public void setRandomEffects(AsremlRandomEffects randomEffects) {
		for(AsremlRandomEffect effect : randomEffects.getRandomEffects()){
			effect.setVarianceStartValues(varianceStartValues.get(effect.getName().replace("Trait.", ""))); //Remove Asreml keyword Trait. for multivar effects
		}
		randomEffects.getVarianceStructure().setVarianceStartValues(varianceStartValues.get("Variance"));
		super.setRandomEffects(randomEffects);
	}
	
	public String getTraitNames(){
		String result = "";
		for(AsremlTrait t:traits){
			result += t.getName() + " ";
		}
		return result;
	}
	

	/**
	 * output the ASREML command file content.
	 * 
	 * @return the string
	 */
	public String toString(){
		String nl = System.getProperty("line.separator");
		String str = "!NOGRAPHICS";
		str += nl + getTitle() + nl;
		for(AsremlTrait t : getAsd().getTraits()){
			str += " " + t.getName() + nl;
		}
		str += getColumns().toString();
		str += getGrms();
		
		str += getAsd().getFilename();
		str += " "+getQualifiers().toString();
		getTabs().setTraits(getTraits());
		str += getTabs()==null ? "" : getTabs().toString() + "\n" ;
		for(AsremlTrait t : traits){
			str += " " + t.getName();
		}
		str += " ~ ";
		str += (getFixedEffects() == null ? "" : getFixedEffects().toString());
		str += (getRandomEffects() == null ? "" : getRandomEffects().toString());
		str += nl + getErrorVariances().getErrorVariance().getErrorVariance() + " " +  getResidual();
		str += nl + getErrorVariances().getErrorVariance().getrStructureAdj() + nl;
		str += (getRandomEffects() == null ? "" : getRandomEffects().getStructuresAsString());
		str += (getPredictions() == null ? "" : getPredictions().toString());
		return str;
	}
}