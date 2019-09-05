/*
 * 
 * 
 * @package 	asreml.output.asr
 * @class 		AsrAnalysisOfVariance.java
 * @author 		u409397
 * @modified 	Jul 8, 2010
 * 
 */
package asreml.output;

import java.util.regex.Pattern;

import org.apache.commons.validator.routines.IntegerValidator;


/**
 * The Class AsrAnalysisOfVariance.  Found in Asreml .asr file
 */
public class AsrAnalysisOfVariance {

	private int model;
	private String effect;
	private String msg;
	private Object numDF;
	private Double fInc;
	private Double fCon;
	private boolean valid;
	private Double denDf;
	private String p;
	
	/**
	 * Checks if is valid.
	 * 
	 * @return true, if is valid
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Sets the valid.
	 * 
	 * @param valid
	 *            the new valid
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * Gets the model.
	 * 
	 * @return the model
	 */
	public int getModel() {
		return model;
	}

	/**
	 * Sets the model.
	 * 
	 * @param model
	 *            the new model
	 */
	public void setModel(int model) {
		this.model = model;
	}

	/**
	 * Gets the effect.
	 * 
	 * @return the effect
	 */
	public String getEffect() {
		return effect;
	}

	/**
	 * Sets the effect.
	 * 
	 * @param effect
	 *            the new effect
	 */
	public void setEffect(String effect) {
		this.effect = effect;
	}

	/**
	 * Gets the msg.
	 * 
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}

	/**
	 * Sets the msg.
	 * 
	 * @param msg
	 *            the new msg
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}

	/**
	 * Gets the num df.
	 * 
	 * @return the num df
	 */
	public Object getNumDF() {
		return numDF;
	}

	/**
	 * Sets the num df.
	 * 
	 * @param numDF
	 *            the new num df
	 */
	public void setNumDF(Object numDF) {
		this.numDF = numDF;
	}
	
	public Double getfInc() {
		return fInc;
	}

	public void setfInc(Double fInc) {
		this.fInc = fInc;
	}

	public Double getfCon() {
		return fCon;
	}

	public void setfCon(Double fCon) {
		this.fCon = fCon;
	}

	public Double getDenDf() {
		return denDf;
	}

	public void setDenDf(Double denDf) {
		this.denDf = denDf;
	}

	public String getP() {
		return p;
	}
	
	public Double getPDouble() {
		Double result = null;
		if(p != null){
			String p = this.p;
			if(p.startsWith("<")){
				p = p.substring(1);
			}
			//catch NA values
			try{
				result = Double.parseDouble(p);
			}
			catch(NumberFormatException e){
				result = null;
			}
		}
		return result;
	}

	public void setP(String p) {
		this.p = p;
	}

	/**
	 * Instantiates a new asr analysis of variance.
	 * 
	 * @param str
	 *            the str
	 */
	public AsrAnalysisOfVariance(String str){
		String[] arr = str.trim().split("\\s+");
		IntegerValidator iv = new IntegerValidator();
		if(!iv.isValid(arr[0])){
			valid = false;
			return;
		}
		model = Integer.parseInt(arr[0]);
		effect = arr[1];
		if(arr.length == 4){
			numDF = arr[2];
			fInc = Double.parseDouble(arr[3]);
		}
		else if(arr.length == 6){
			numDF = arr[2];
			denDf = Double.parseDouble(arr[3]);
			fInc = Double.parseDouble(arr[4]);
			p = arr[5];
		}
		else if(arr.length == 8){
			numDF = arr[2];
			String decimalPattern = "([0-9]*)\\.([0-9]*)";
			if(Pattern.matches(decimalPattern, arr[3])){
				denDf = Double.parseDouble(arr[3]);
			}
			fInc = Double.parseDouble(arr[4]);
			if(Pattern.matches(decimalPattern, arr[5])){
				fCon = Double.parseDouble(arr[5]);
			}
			p = arr[7];
		}
		else{
			msg = "";
			for(int i=2; i<arr.length; i++)
				msg += arr[i]+" ";
		}
		valid = true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return model +" "+ effect +" "+
			   (msg != null ? msg : numDF+" "+fInc);
	}
}
