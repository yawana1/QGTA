/*
 * 
 * 
 * @package 	asreml.input
 * @class 		AsremlRandomEffect.java
 * @author 		u409397
 * @modified 	Jul 8, 2010
 * 
 */
package asreml.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import asreml.AsremlGlobals.Flag;
import asreml.AsremlGlobals.SourceMatrix;
import asreml.AsremlTrait;

/**
 * The Class AsremlRandomEffect is used to store a collection of
 * {@link AsremlRandomEffectStructure}s.
 */
public class AsremlRandomEffect extends AsremlEffect{

	private String asdName;  //from names where random effect and fbk are different for the same column. ie geno_id = at(core, 1).geno_id
	
	/** The start value. */
	private double startValue;
	private Map<AsremlTrait, Double> varianceStartValues;
	
	/** The flag. */
	private Flag flag;
	
	/** The source number. */
	private int sourceNumber;
	
	/** The source. */
	private SourceMatrix source;
	
	private String multivarVarianceStructure;
	
	List<AsremlRandomEffectStructure> structures = new ArrayList<AsremlRandomEffectStructure>();
	
	public String getMultivarVarianceStructure() {
		return multivarVarianceStructure;
	}

	public void setMultivarVarianceStructure(String multivarVarianceStructure) {
		this.multivarVarianceStructure = multivarVarianceStructure;
	}

	/**
	 * @return the structures
	 */
	public List<AsremlRandomEffectStructure> getStructures() {
		return structures;
	}


	/**
	 * @param structures the structures to set
	 */
	public void setStructures(List<AsremlRandomEffectStructure> structures) {
		this.structures = structures;
	}


	/**
	 * Instantiates a new asreml random effect.
	 * 
	 * @param name
	 *            the name
	 */
	public AsremlRandomEffect(String name){
		this.name = name;
		this.asdName = name;
	}
	
	public AsremlRandomEffect(String name, String asdName){
		this.name = name;
		this.asdName = asdName;
	}

	public AsremlRandomEffect(String name, double startValue, Flag flag,
			int sourceNumber, SourceMatrix source) {
		super();
		this.name = name;
		this.startValue = startValue;
		this.flag = flag;
		this.sourceNumber = sourceNumber;
		this.source = source;
	}

	/**
	 * @return the startValue
	 */
	public double getStartValue() {
		return startValue;
	}

	public String getAsdName() {
		return null == asdName? name:asdName;
	}

	public void setAsdName(String asdName) {
		this.asdName = asdName;
	}

	/**
	 * @param startValue the startValue to set
	 */
	public void setStartValue(double startValue) {
		this.startValue = startValue;
	}



	/**
	 * @return the flag
	 */
	public Flag getFlag() {
		return flag;
	}



	/**
	 * @param flag the flag to set
	 */
	public void setFlag(Flag flag) {
		this.flag = flag;
	}



	/**
	 * @return the sourceNumber
	 */
	public int getSourceNumber() {
		return sourceNumber;
	}



	/**
	 * @param sourceNumber the sourceNumber to set
	 */
	public void setSourceNumber(int sourceNumber) {
		this.sourceNumber = sourceNumber;
	}



	/**
	 * @return the source
	 */
	public SourceMatrix getSource() {
		return source;
	}



	/**
	 * @param source the source to set
	 */
	public void setSource(SourceMatrix source) {
		this.source = source;
	}

	public Map<AsremlTrait, Double> getVarianceStartValues() {
		return varianceStartValues;
	}

	public void setVarianceStartValues(
			Map<AsremlTrait, Double> varianceStartValues) {
		this.varianceStartValues = varianceStartValues;
	}

	public boolean equals(Object o){
		boolean  result = false;
		if(o != null && o.toString() != null && name != null){
			result = name.equals(((AsremlRandomEffect)o).name);
		}
		return result;
	}
	
	public String toString(){
		String str = name + " ";
		int size = getStructures() != null ? getStructures().size() : 0;
		if(multivarVarianceStructure != null && !multivarVarianceStructure.isEmpty()){
			size ++;
		}
		
		str += (size > 0 ? size : " ")+"\n";

		if(multivarVarianceStructure != null && !multivarVarianceStructure.isEmpty()){
			str += multivarVarianceStructure + "\n";
			String defaultStartingValue = "";
			for(Double d: varianceStartValues.values()){
				str += defaultStartingValue + d + "\n";
				defaultStartingValue += startValue + " ";
			}
		}
		
		for (AsremlRandomEffectStructure structure : getStructures()) {
			str += structure + "\n";
		}

		return str;
	}
}
