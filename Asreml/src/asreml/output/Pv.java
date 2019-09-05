package asreml.output;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import utils.Validator;

public class Pv {

	private Integer primary_id;
	private Integer secondary_id;
	private List<Integer> keys;
	private BigDecimal effect;
	private BigDecimal error;
	private String errorCode;
	
	public Integer getPrimary_id() {
		return primary_id;
	}
	public void setPrimary_id(Integer primary_id) {
		this.primary_id = primary_id;
	}
	public Integer getSecondary_id() {
		return secondary_id;
	}
	public void setSecondary_id(Integer secondary_id) {
		this.secondary_id = secondary_id;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public BigDecimal getEffect() {
		return effect;
	}
	public void setEffect(BigDecimal effect) {
		this.effect = effect;
	}
	public BigDecimal getError() {
		return error;
	}
	public void setError(BigDecimal error) {
		this.error = error;
	}	
	public List<Integer> getKeys() {
		return keys;
	}
	public void setKeys(List<Integer> keys) {
		this.keys = keys;
	}
	
	public Pv(String[] data){
		keys = new ArrayList<Integer>();
		int index = data.length;
		this.errorCode = data[--index].trim();
		Double val = Validator.getDbl(data[--index].trim());
		if(val.isInfinite() || val.isNaN()){
			val = 0d;
		}
		this.error = new BigDecimal(val);

		val = Validator.getDbl(data[--index].trim());
		if(val.isInfinite() || val.isNaN()){
			val = 0d;
		}
		this.effect = new BigDecimal(val);

		for(int i  = 0; i < index; i++){
			keys.add(Integer.parseInt(data[i]));
		}
	}
	
	public boolean isEstimable(){
		return errorCode.equals("E");
	}
	
	public Pv(){}
}
