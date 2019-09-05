package asreml.input;

import asreml.AsremlGlobals.Flag;
import asreml.AsremlGlobals.SourceMatrix;

/**
 * The Class AsremlRandomEffectStructure is used to define a random effect variance structure in
 * the ASREML command file.
 */
public class AsremlRandomEffectStructure {

	private Integer sourceNum;
	private String name;
	private Double startValue;
	private String positiveDefinite;
	
	public String getPositiveDefinite() {
		return positiveDefinite;
	}

	public void setPositiveDefinite(String positiveDefinate) {
		this.positiveDefinite = positiveDefinate;
	}

	/**
	 * @return the startValue
	 */
	public Double getStartValue() {
		return startValue;
	}

	/**
	 * @param startValue the startValue to set
	 */
	public void setStartValue(Double startValue) {
		this.startValue = startValue;
	}

	public Integer getSourceNum() {
		return sourceNum;
	}

	public void setSourceNum(Integer sourceNum) {
		this.sourceNum = sourceNum;
	}

	/**
	 * Gets the source.
	 * 
	 * @return the source
	 */
	public SourceMatrix getSource() {
		return sourceNum==0?SourceMatrix.IDV:SourceMatrix.GIV;
	}
	
	
	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	
	public AsremlRandomEffectStructure(String name) {
		super();
		this.name = name;
	}
	

	public AsremlRandomEffectStructure(String name, int sourceNum) {
		this.name = name;
		this.sourceNum = sourceNum;
	}

	
	public AsremlRandomEffectStructure(String name, Integer source_num, Double startValue, String positiveDefinite) {
		this.name = name;
		this.sourceNum = source_num;
		this.startValue = startValue;
		this.positiveDefinite = positiveDefinite;
	}

	
	public String toString(){
		String str = name+" 0 "+getSource();
		str += sourceNum==null || sourceNum==0? "" : sourceNum;
		if(startValue != null && startValue!=0)
			str +=" "+ startValue +" "+Flag.gp.value();
		return str;
	}

}