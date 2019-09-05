package db;

import java.sql.Connection;
import java.sql.DriverManager;

public class Connect {
	
	private static final String CONNECTION = "jdbc:jtds:sqlserver:"; ///JTDS

	public static Connection getConnection(DataSource ds) throws Exception {
		Connection conn = null;
		
		try{
			Class.forName(ds.getDriver()).newInstance();
		}
		catch(Exception e){
			throw e;
		}
		
		String connectionString = CONNECTION+ds.getServer()+";databaseName="+ds.getName();
		conn = DriverManager.getConnection(connectionString, ds.getUserName(), ds.getPassword());
		
		return conn;
	}
}
