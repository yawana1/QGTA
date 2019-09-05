/*
 * 
 * 
 * @package 	asreml.input
 * @class 		AsremlTab.java
 * @author 		u409397
 * @modified 	Jul 8, 2010
 * 
 */
package asreml.input;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class AsremlTab is used to define how tabulation should be performed in
 * ASREML.
 */
public class AsremlTab {

	private List<String> factors = new ArrayList<String>();
	
	public List<String> getFactors() {
		return factors;
	}
	
	public void setFactors(ArrayList<String> factors) {
		this.factors = factors;
	}
	
	/**
	 * Instantiates a new asreml tab.
	 * 
	 * @param factors
	 *            the factors
	 */
	public AsremlTab(String[] factors){
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
		String str ="";
		for(String factor : factors){
			str += " "+factor;
		}
		
		return str;
	}
}
