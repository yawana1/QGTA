/*
 * Using JRE 1.6.0_02
 * 
 * @package 	asreml.output.sln
 * @class 		Sln.java
 * @author 		u409397
 * @modified 	Jul 8, 2010
 * 
 */
package asreml.output;

import org.apache.commons.validator.routines.IntegerValidator;

import utils.Validator;

/**
 * The Class Sln.
 */
public class Sln {

	private double effect;
	private double error;
	private String column;
	private int[] ids;
	
	public double getEffect() {
		return effect;
	}

	public void setEffect(double effect) {
		this.effect = effect;
	}

	public double getError() {
		return error;
	}

	public void setError(double error) {
		this.error = error;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public int[] getIds() {
		return ids;
	}

	public void setIds(int[] ids) {
		this.ids = ids;
	}

	public Sln(String[] data){
		this.column = data[0].trim();
		String[] lst = data[1].split("\\.");
		int[] tmp = new int[lst.length];
		IntegerValidator iv = new IntegerValidator();
		for(int i=0; i<lst.length; i++){
			if(iv.isValid(lst[i].trim())){
				tmp[i] = Validator.getInt(lst[i].trim());
			}
		}
		ids = tmp;
		this.effect = Validator.getDbl(data[2].trim());
		this.error = Validator.getDbl(data[3].trim());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return column+" "+ids.toString()+" "+effect+" "+error;
	}
	
	public boolean isValid(){
		boolean valid = false;
		if(ids != null){
			boolean negativeFound = false;
			for(Integer id : ids){
				if(null != id && id < 0 ){
					negativeFound = true;
				}
			}
			valid = !negativeFound;
		}
		return valid;
	}
	
	public Sln(){}
}
