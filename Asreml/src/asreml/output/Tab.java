/*
 * Using JRE 1.6.0_02
 * 
 * @package 	asreml.output.tab
 * @class 		Tab.java
 * @author 		u409397
 * @modified 	Jul 8, 2010
 * 
 */
package asreml.output;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import utils.Validator;

// TODO: Auto-generated Javadoc
/**
 * The Class Tab.
 */
public class Tab {
	
	/** The logger. */
	static Logger logger = Logger.getLogger(Tab.class.getName());
	private Integer primary_id;
	private Integer secondary_id;
	private Integer count;
	private Double mean,stDev,min,max;
	private boolean isValid = true;
	private List<String> keys;
	
	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public List<String> getKeys() {
		return keys;
	}

	public void setKeys(List<String> keys) {
		this.keys = keys;
	}

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

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Double getMean() {
		return mean;
	}

	public void setMean(Double mean) {
		this.mean = mean;
	}

	public Double getStDev() {
		return stDev;
	}

	public void setStDev(Double stDev) {
		this.stDev = stDev;
	}

	public Double getMin() {
		return min;
	}

	public void setMin(Double min) {
		this.min = min;
	}

	public Double getMax() {
		return max;
	}

	public void setMax(Double max) {
		this.max = max;
	}

	public Tab(String[] data){
		int index = 0;
		keys = new ArrayList<String>();
		try{
			for(;index<data.length-6;index++){
				keys.add(data[index].trim());
			}
			primary_id = keys.size() > 0 ? Validator.getInt(keys.get(0)):null; //backward compt
			secondary_id = keys.size() > 1 ? Validator.getInt(keys.get(1)):null; //backward compt
			mean =Validator.getDbl(data[index++].trim());
			stDev =Validator.getDbl(data[index++].trim());
			min =Validator.getDbl(data[index++].trim());
			max =Validator.getDbl(data[index++].trim());
			count = Validator.getInt(data[index++].trim());
			
			if(mean == null || stDev == null || min == null || max == null || count == null){
				isValid = false;
			}
			
			if(isValid){
				if(stDev == null){
					stDev = 0d;
				}
			}
		}catch(Exception e){
			logger.warn("Tab", e);
			isValid = false;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return primary_id+" "+(secondary_id==null ? "" : secondary_id+" ")+mean+" "+stDev+" "+min+" "+max+" "+count;
	}
}
