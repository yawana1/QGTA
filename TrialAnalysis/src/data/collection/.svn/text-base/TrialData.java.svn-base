package data.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class to store the data from a database and also the database type of the columns.
 * 
 * @author Scott Smith
 *
 */
public class TrialData {

	private List<Map<String,Object>> data; //actual column/value rows
	private Map<String,String> types; //database type
	private List<String> booleanColunns; //columns that are only true/false
	private List<String> indexColumns; //columns to index
	
	public List<String> getIndexColumns() {
		return indexColumns;
	}

	public void setIndexColumns(List<String> indexColumns) {
		this.indexColumns = indexColumns;
	}

	public List<Map<String, Object>> getData() {
		return data;
	}

	public void setData(List<Map<String, Object>> data) {
		this.data = data;
	}

	public Map<String, String> getTypes() {
		return types;
	}

	public List<String> getBooleanColunns() {
		return booleanColunns;
	}

	public void setBooleanColunns(List<String> booleanColunns) {
		this.booleanColunns = booleanColunns;
	}

	public void setTypes(Map<String, String> types) {
		this.types = types;
	}

	public TrialData(List<Map<String, Object>> data, Map<String, String> types) {
		super();
		this.data = data;
		this.types = types;
		this.booleanColunns = new ArrayList<>();
		this.indexColumns = new ArrayList<>();
	}

	public boolean isEmpty() {
		boolean result = false;
		
		if(data == null || data.isEmpty()){
			result = true;
		}
		
		return result;
	}	
}
