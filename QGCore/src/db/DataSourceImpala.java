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

/**
 * The Class DB is used to define a database.
 */
public class DataSourceImpala {

	private String name;
	private String server;
	private String userName;
	private String password;
	private String connectionString;
	private String driver;

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

	public DataSourceImpala(String name){
		this.name = name;
	}
	
	public DataSourceImpala(String name, String connectionString, String driver){
		this.name = name;
		this.connectionString = connectionString;
		this.driver = driver;
	}
	
	public String connection(){
		return server + ","+userName+","+password;
	}
	
	public boolean equals(DataSourceImpala db){
		return name.equals(db.getName());
	}
}
