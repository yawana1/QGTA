package asreml.input;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The Class AsremlRandomEffects is used to store a collection of
 * {@link AsremlRandomEffect} s.
 */
public class AsremlRandomEffects {

	/** The randomEffects. */
	private List<AsremlRandomEffect> randomEffects = new ArrayList<AsremlRandomEffect>();
	private AsremlRandomEffect varianceStructure;  //used for multivar blup as the first element that doesn't have a RandomEffectStructure.

	public AsremlRandomEffect getVarianceStructure() {
		return varianceStructure;
	}

	public void setVarianceStructure(AsremlRandomEffect varianceStructure) {
		this.varianceStructure = varianceStructure;
	}
	
	public List<AsremlRandomEffect> getRandomEffects() {
		return randomEffects;
	}

	public void setRandomEffects(List<AsremlRandomEffect> randomEffects) {
		this.randomEffects = randomEffects;
	}

	public void add(AsremlRandomEffect randomEffect) {
		randomEffects.add(randomEffect);
	}

	public AsremlRandomEffect getRandomEffect(String name) {
		AsremlRandomEffect randomEffect = null;
		int index = this.randomEffects.indexOf(new AsremlRandomEffect(name));
		if(index != -1){
			randomEffect = this.randomEffects.get(index);
		}
		return randomEffect;
	}
	
	@Override
	public String toString() {
		String str = "";

		if (randomEffects.size() > 0) {
			str += " !r";
			for (Iterator<AsremlRandomEffect> cit = randomEffects.iterator(); cit.hasNext();) {
				str += " " + cit.next().getName();
			}
		}

		return str;
	}
	
	public String getStructuresAsString(){
		String str = "";
		
		if(varianceStructure != null && varianceStructure.getVarianceStartValues() != null){
			//add variance from uni runs for multiVar AS file
			str += varianceStructure.getMultivarVarianceStructure() + "\n";
			String defaultStartingValue = "";
			for(double d: varianceStructure.getVarianceStartValues().values()){
				str += defaultStartingValue + d + "\n";
				defaultStartingValue += varianceStructure.getStartValue() + " ";
			}
		}
		
		for (AsremlRandomEffect effect : randomEffects) {
			str += effect;
		}
		return str;
	}

	public int size() {
		return randomEffects.size();
	}
}
