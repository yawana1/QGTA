/*
 * Using JRE 1.6.0_02
 * 
 * @package 	utils
 * @class 		DB.java
 * @author 		u409397
 * @modified 	Jul 8, 2010
 * 
 */
package db;

import java.util.Map;

/**
 * The Class DB is used to define a database.
 */
public class DataSource {

	private String name;
	private String server;
	private String userName;
	private String password;
	private String connectionString;
	private String driver;
	private String type;

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getConnectionString() {
		return connectionString;
	}
	public void setConnectionString(String connectionString) {
		this.connectionString = connectionString;
	}
	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public DataSource(String name){
		this.name = name;
	}
	
	public DataSource(String name, String connectionString, String driver){
		this.name = name;
		this.connectionString = connectionString;
		this.driver = driver;
	}
	
	public String getURL(){

		//set default as sqlserver
		if(type == null){
			type = "sqlserver";
		}
		
		//set datasource url for this data source
		Map<String, DataSourceType> types = Sessions.INSTANCE.getTypes();
		if(types == null){
			driver = Session.DRIVER_SQL;
			return Session.PROTOCOL_JTDS+server+";databaseName="+name+";allowMultiQueries=true";
		}
		DataSourceType dsType = types.get(type);
		String protocol = dsType.getProtocol();
		String url = protocol+server+";databaseName="+name;
		return url;
	}
	
	public boolean equals(DataSource db){
		return name.equals(db.getName());
	}
}