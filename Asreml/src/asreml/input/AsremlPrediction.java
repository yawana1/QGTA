package asreml.input;

import java.util.ArrayList;

public class AsremlPrediction {

	private ArrayList<String> factors = new ArrayList<String>();
	private String tag;
	
	
	/**
	 * Gets the tag.
	 * 
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * Sets the tag.
	 * 
	 * @param tag 
	 * 			the tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}

	/**
	 * Gets the factors.
	 * 
	 * @return the factors
	 */
	public ArrayList<String> getFactors() {
		return factors;
	}
	
	/**
	 * Sets the factors.
	 * 
	 * @param factors
	 *            the new factors
	 */
	public void setFactors(ArrayList<String> factors) {
		this.factors = factors;
	}
	
	/**
	 * Instantiates a new asreml prediction.
	 * 
	 * @param factors
	 *            the factors
	 */
	public AsremlPrediction(String[] factors){
		add(factors);
	}
	
	/**
	 * Adds the.
	 * 
	 * @param factor
	 *            the factor
	 */
	public void add(String factor){
		factors.add(factor);
	}	
	
	/**
	 * Adds the.
	 * 
	 * @param factors
	 *            the factors
	 */
	public void add(String[] factors){
		for(String factor : factors)
			this.factors.add(factor);
	}
	
	@Override
	public String toString() {
		String str = "";
		
		for(String factor : factors){
			str += factor+" ";
		}
		
		return str;
	}
}
