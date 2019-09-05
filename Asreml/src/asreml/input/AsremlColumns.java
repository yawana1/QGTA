/*
 * 
 * @package 	asreml.input
 * @class 		AsremlColumns.java
 * @author 		u409397
 * @modified 	Jul 8, 2010
 * 
 */
package asreml.input;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * The Class AsremlColumns is used to store a collection of {@link AsremlColumn}
 * s.
 */
public class AsremlColumns {

	private static Logger log = Logger.getLogger(AsremlColumns.class.getName());
	private List<AsremlColumn> columns = new ArrayList<AsremlColumn>();
	
	public List<AsremlColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<AsremlColumn> columns) {
		this.columns = columns;
	}
	
	public List<String> getColumnNames(){
		List<String> names = new ArrayList<String>();
		if(null != columns){
			for(int i = 0; i < columns.size(); i++){
				names.add(columns.get(i).getName());
			}
		}
		return names;
	}

	/**
	 * Get all the Asreml Columns that are of type boolean
	 */
	public List<String> getColumnsBoolean(){
		List<String> names = new ArrayList<String>();
		if(null != columns){
			for(AsremlColumn column : columns){
				if(column.isBool()){
					names.add(column.getName());					
				}
			}
		}
		return names;
	}
	
	/**
	 * Adds the.
	 * 
	 * @param column
	 *            the column
	 */
	public void add(AsremlColumn column){
		columns.add(column);
	}
	
	public AsremlColumn get(String name){
		int index = columns.indexOf(new AsremlColumn(name));
		if(index == -1){
			log.error("Trying to find Asreml Column that's not specified " + name);
		}
		return columns.get(index);
	}
	
	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		for(AsremlColumn column : columns){
			str.append(" ");
			str.append(column);
			str.append(System.lineSeparator());
		}
		return str.toString();
	}
	
	public int size(){
		return columns.size();
	}
}
