package data.collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.Funcs;

/**
 * Represents a single data observation point.  Contains phenoType data and information to tell when and where the data was collected.
 * 
 * @author Scott Smith
 *
 */
public class ExpFBK {

	private List<Double> coeff;
	private Map<String, Object> data;
	
	public ExpFBK(){
		
	}
	public ExpFBK(Map<String,Object> data){
		coeff = new ArrayList<Double>();
		this.data = data;
	}

	public List<Double> getCoeff() {
		return coeff;
	}
	public Double getCoeff(int index){
		return coeff.get(index);
	}
	public void setCoeff(List<Double> coeff) {
		this.coeff = coeff;
	}
	public Map<String, Object> getData() {
		return data;
	}
	public void setData(Map<String, Object> columns) {
		this.data = columns;
	}
	public Set<String> getColNames(){
		return data.keySet();
	}
	
	public Object getValue(String key){
		return data.get(key);
	}
	
	public Integer getInt(String key){
		return Funcs.getInt("" + getValue(key));
	}
	
	public Double getDbl(String key){
		return Funcs.getDbl("" + getValue(key));
	}
	
	public boolean getBool(String key){
		return Funcs.getBool("" + getValue(key));
	}
	
	public List<String> getList(String key){
		return Funcs.getList("" + getValue(key));
	}
	
	public boolean hasValues(){
		return data != null && data.size() > 0;
	}
	
	public boolean hasValues(String genoType){
		boolean hasValues = hasValues();
		if(!data.get(genoType).equals(0)){
			hasValues = true;
		}
		else{
			hasValues = false;
		}
		return hasValues;
	}

	public boolean hasColumn(String columnName){
		boolean result = false;
		if(hasValues()){
			result = data.containsKey(columnName);
		}
		return result;
	}
	
	public boolean isCheck(){
		boolean result = false;
		
		//if one is true returns true
		if(hasValues()){
			result = getBool("coreCheck") | getBool("perfCheck") | getBool("geneticCheck") | getBool("bmrCheck");
		}
		
		return result;
	}
	
	public String toString(){
		String str = "";
		for(Iterator<String> cit = data.keySet().iterator(); cit.hasNext();){
			str += data.get(cit.next())+"\t";
		}
		return str;
	}
	
	public void addData(String col, Object value){
		data.put(col, value);
	}
}