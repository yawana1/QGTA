/*
 * 
 * 
 * @package 	asreml.input
 * @class 		AsremlColumn.java
 * @author 		u409397
 * @modified 	Jul 8, 2010
 * 
 */
package asreml.input;

import asreml.AsremlGlobals.Flag;

/**
 * The Class AsremlColumn is used to define columns in the ASREML command file
 * and the columns information such as flags and factors.
 */
public class AsremlColumn {

	private String name;
	private boolean indexed = false; //to use 
	private boolean coeff = false; //if the factor is a coefficent
	private boolean bool = false; //if the factor is a boolean
	private boolean indexColumn; //if factor has a grm reindex with it
	private boolean noFlag = false; //will not print an factor flags regardless of anything else specified

	public boolean isIndexColumn() {
		return indexColumn;
	}

	public void setIndexColumn(boolean indexColumn) {
		this.indexColumn = indexColumn;
	}

	private Integer level = null;

	public boolean isBool() {
		return bool;
	}

	public void setBool(boolean bool) {
		this.bool = bool;
	}

	public boolean isCoeff() {
		return coeff;
	}

	public void setCoeff(boolean coeff) {
		this.coeff = coeff;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isIndexed() {
		return indexed;
	}

	public void setIndexed(boolean indexed) {
		this.indexed = indexed;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public AsremlColumn(String name) {
		this.name = name;
	}

	public boolean isNoFlag() {
		return noFlag;
	}

	public void setNoFlag(boolean noFlag) {
		this.noFlag = noFlag;
	}

	public AsremlColumn(){
		
	}
	
	public AsremlColumn(String name, boolean indexed, Integer level, boolean bool, boolean coeff, boolean indexColumn, boolean noFlag) {
		this.name = name;
		this.indexed = indexed;
		this.level = level;
		this.bool = bool;
		this.coeff = coeff;
		this.indexColumn = indexColumn;
		this.noFlag = noFlag;
	}


	/**
	 * Example return 
	 * <p> locId !I 13</p>
	 */
	public String toString(){
		StringBuffer result = new StringBuffer();
		result.append(" ");
		result.append(name);
		result.append(" ");
		
		if(level != null){
			if(noFlag){
				result.append(level);
			}
			else if(coeff){
				result.append(Flag.g.value());
				result.append(" ");
				result.append(level);
			}
			else if (indexed) {
				result.append(Flag.i.value());
				result.append(" ");
				result.append(level);
			}
			else if(indexColumn){
				result.append(level);
			}
			else{
				result.append(Flag.a.value());
				result.append(" !PRUNE !LL 70");
				result.append(" ");
				result.append(level);
			}
		}
		return 	result.toString();
	}
	
	/***
	 * If names are the same return true
	 */
	public boolean equals(Object o){
		boolean result = false;
		if(name != null && o !=null){
			if(o instanceof AsremlColumn){
				result = name.equals(((AsremlColumn)o).getName());
			}
			else{
				result = super.equals(o);
			}
		}
		return result;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
