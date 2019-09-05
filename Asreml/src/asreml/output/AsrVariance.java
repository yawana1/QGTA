/*
 * 
 * 
 * @package 	asreml.output.asr
 * @class 		AsrVariance.java
 * @author 		u409397
 * @modified 	Jul 8, 2010
 * 
 */
package asreml.output;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class AsrVariance.
 */
public class AsrVariance {

	/** The terms. */
	private List<Integer> terms;
	
	/** The gamma. */
	private double gamma;
	
	/** The component. */
	private double component;
	
	/** The comp se. */
	private double compSE;
	
	/** The percent. */
	private double percent;
	
	/** The source. */
	private String source;
	
	/** The model. */
	private String model;
	
	/** The c. */
	private String c;
	
	/**
	 * Gets the terms.
	 * 
	 * @return the terms
	 */
	public List<Integer> getTerms() {
		return terms;
	}

	/**
	 * Sets the terms.
	 * 
	 * @param terms
	 *            the new terms
	 */
	public void setTerms(List<Integer> terms) {
		this.terms = terms;
	}

	/**
	 * Gets the gamma.
	 * 
	 * @return the gamma
	 */
	public double getGamma() {
		return gamma;
	}

	/**
	 * Sets the gamma.
	 * 
	 * @param gamma
	 *            the new gamma
	 */
	public void setGamma(double gamma) {
		this.gamma = gamma;
	}

	/**
	 * Gets the component.
	 * 
	 * @return the component
	 */
	public double getComponent() {
		return component;
	}

	/**
	 * Sets the component.
	 * 
	 * @param component
	 *            the new component
	 */
	public void setComponent(double component) {
		this.component = component;
	}

	/**
	 * Gets the comp se.
	 * 
	 * @return the comp se
	 */
	public double getCompSE() {
		return compSE;
	}

	/**
	 * Sets the comp se.
	 * 
	 * @param compSE
	 *            the new comp se
	 */
	public void setCompSE(double compSE) {
		this.compSE = compSE;
	}

	/**
	 * Gets the percent.
	 * 
	 * @return the percent
	 */
	public double getPercent() {
		return percent;
	}

	/**
	 * Sets the percent.
	 * 
	 * @param percent
	 *            the new percent
	 */
	public void setPercent(double percent) {
		this.percent = percent;
	}

	/**
	 * Gets the source.
	 * 
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Sets the source.
	 * 
	 * @param source
	 *            the new source
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * Gets the model.
	 * 
	 * @return the model
	 */
	public String getModel() {
		return model;
	}

	/**
	 * Sets the model.
	 * 
	 * @param model
	 *            the new model
	 */
	public void setModel(String model) {
		this.model = model;
	}

	/**
	 * Gets the c.
	 * 
	 * @return the c
	 */
	public String getC() {
		return c;
	}

	/**
	 * Sets the c.
	 * 
	 * @param c
	 *            the new c
	 */
	public void setC(String c) {
		this.c = c;
	}

	/**
	 * Instantiates a new asr variance.
	 */
	public AsrVariance(){}
	
	/**
	 * Instantiates a new asr variance.
	 * 
	 * @param arr
	 *            the arr
	 */
	public AsrVariance(String[] arr){
		int index = 0;
		terms = new ArrayList<Integer>();
		this.source = arr[index++];
		this.model = arr[index++];
		terms.add(Integer.valueOf(arr[index++]));
		if(arr.length == 9){ // extra term for multivaariant
			terms.add(Integer.valueOf(arr[index++]));
		}
		this.gamma = Double.parseDouble(arr[index++]);
		this.component = Double.parseDouble(arr[index++]);
		this.compSE = Double.parseDouble(arr[index++]);
		this.percent = Double.parseDouble(arr[index++]);
		this.c = arr[index++];
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return	source +" "+ model +" "+ terms +" "+ gamma +" "+ component +" "+
				compSE +" "+ percent +" "+ c;
				
	}
}
