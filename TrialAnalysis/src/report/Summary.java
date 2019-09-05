package report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import utils.Globals.StatGroup;
import utils.Globals.SummaryType;
import utils.ObjectQuery;
import data.xml.objects.Trait;

/***
 * Object used to store all data needed to write the summary reports.
 * 
 * @author Scott Smith
 *
 */
public class Summary implements Cloneable {

	static Logger logger = Logger.getLogger(Summary.class.getName());
	
	private Trait trait;
	private SummaryType type;
	private LinkedHashMap<String, Integer> filters;
	private Map<String, Object> values;
	private List<StatGroup> statGroups;
	
	public Summary(SummaryType type, Trait trait){
		this.type = type;
		this.trait = trait;
		values = new HashMap<String, Object>();
	}
	
	public Summary(SummaryType type){
		this.type = type;
		values = new HashMap<String, Object>();
	}
	
	public Summary(){
		
	}

	public Trait getTrait() {
		return trait;
	}

	public void setTrait(Trait trait) {
		this.trait = trait;
	}

	public SummaryType getType() {
		return type;
	}

	public void setType(SummaryType type) {
		this.type = type;
	}
	
	public Map<String, Object> getValues() {
		return values;
	}

	public void setValues(Map<String, Object> values) {
		this.values = values;
	}

	public LinkedHashMap<String,Integer> getFilters() {
		return filters;
	}

	public void setFilters(LinkedHashMap<String,Integer> filters) {
		this.filters = filters;
	}
	

	public List<StatGroup> getStatGroups() {
		return statGroups;
	}

	public void setStatGroups(List<StatGroup> statGroups) {
		this.statGroups = statGroups;
	}

	public Double getEstimate(){	
		return (Double)values.get("estimate");
	}
	
	/**
	 * If no reps don't show this summary.
	 * 
	 * @return
	 */
	public boolean show(){
		boolean result = false;
		if(!values.containsKey("rawCount") || values.get("rawCount") != null){ //don't show rows with no reps
			result = true;
		}
		return result;
	}

	public List<Integer> getIds() {
		return new ArrayList<Integer>(filters.values());
	}
	
	/**
	 * Deep copy the this summary
	 */
	public Object clone(){
		Summary result = null;
		try {
			result = (Summary) super.clone();
			result.setValues(new HashMap<String, Object>());
			for(Entry<String, Object> entry:values.entrySet()){
				result.getValues().put(entry.getKey(), entry.getValue());
			}
		} catch (CloneNotSupportedException e) {
			logger.error(e);
		}
		return result;
	}

	@Override
	public String toString() {
		return "Summary [trait=" + trait + ", type=" + type + ", filters="
				+ filters + ", values=" + values + ", statGroups=" + statGroups
				+ "]";
	}
	
	/**
	 * Search the Collection of Summaries and return the first result found for the filters.
	 * Uses JoSql to query the collection using the filters as a where clause
	 * 
	 * @param data - Collection of Summaries
	 * @param filters - Map of columnName/index to search the collection by
	 * @return
	 */
	public static Summary search(Collection<Summary> data, LinkedHashMap<String, Integer> filters){
		
		Summary result = null;
		
		try{
			if(data != null){
				//select
				String[] select = {"*"};
				
				//where
				List<String> entryFilter = new ArrayList<String>();
				int j = 0;
				for(String filter : filters.keySet()){
					entryFilter.add(" get(ids, " + j++ + ") = '" + filters.get(filter).toString() + "' ");
				}
				
				ObjectQuery query = new ObjectQuery( Arrays.asList(select), Summary.class.getName(), entryFilter, data);
				List<Summary> results = query.execute();
				
				if(results.size() > 0){
					result = results.get(0);
				}
			}
		}
		catch (Exception e) {
			logger.warn("", e);
		}
		return result;
	}
	
	/**
	 * Try to find value of colum from the given summary.
	 * @param summary -
	 * @param column - Column on the Summary
	 * @return - Return the value of the column or 0.
	 */
	public static Double getSummaryValue(Summary summary, String column){
		Double result = 0d;
		if(summary != null){
			if(summary.getValues().get(column) != null){
				String value = summary.getValues().get(column).toString();
				result = Double.parseDouble(value);
			}
		}
		return result;
	}
}