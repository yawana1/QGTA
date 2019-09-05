package db;

import java.util.Collection;
import java.util.Map;

import utils.Funcs;

/**
 * Helper class to write sql statements.
 * <p>Currently used to create and load the in memory database</p>
 * 
 * @author Scott Smith
 *
 */
public class SQL {

	private String[] columns;
	private String tableName;
	
	public String[] getColumns() {
		return columns;
	}

	public void setColumns(String[] columns) {
		this.columns = columns;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public SQL(String tableName, Collection<String> set){
		this.tableName = tableName;
		this.columns = new String[set.size()];
		this.columns = set.toArray(columns);
	}
	
	/**
	 * Used to create a table that matches the columns in the data with the datatype from types
	 * 
	 * @param tableName
	 * @param data - Example of data row to use to find needed columns 
	 * @param types - Map of columanName/database type
	 * @return - Table create sql
	 */
	public String createTable(Map<String,Object> data, Map<String,String> types){
		String sql = "CREATE TABLE " + tableName + "(columns)";
		StringBuffer columns = new StringBuffer();
		
		//loop through getting columns and data types
		for(String column : this.columns){
			columns.append(",");
			columns.append(Funcs.quoteString(column));
			columns.append(" ");
			columns.append(types.get(column));
			//columns.append(System.lineSeparator());
		}
		
		sql = sql.replace("columns", columns.substring(1)); //skip leading comma
		
		return sql;
	}
	
	/**
	 * Create insert prepared Statement
	 * ex. INSERT INTO EXP_FBK (genoId) values(?)
	 * 
	 * @param tableName - Table to insert into
	 * @param columns - List of columns that with have a value
	 * @return
	 */
	public String insert(){
		StringBuffer sqlColumns = new StringBuffer();
		StringBuffer sqlValues = new StringBuffer();
		String insert = "INSERT INTO " + tableName + " (columns) VALUES (values)";
		
		//create insert prepared statement with columns and ?
		for(String column : this.columns){
			sqlColumns.append(Funcs.quoteString(column));
			sqlColumns.append(",");
			sqlValues.append("?");
			sqlValues.append(",");
		}
 
		insert = insert.replace("columns", sqlColumns.subSequence(0, sqlColumns.length() -1 )); //remove last trailing comma
		insert = insert.replace("values", sqlValues.subSequence(0, sqlValues.length() -1 )); //remove last trailing comma
		
		return insert;
	}
}
