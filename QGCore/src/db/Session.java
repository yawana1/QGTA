/*
 * 
 * 
 * @package 	db.hibernate
 * @class 		Session.java
 * @author 		u409397
 * @modified 	Jul 8, 2010
 * 
 */
package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.transform.AliasToEntityMapResultTransformer;

import db.hibernate.SqlServerDriverTypeExtension;
import error.ErrorMessage;

/**
 * The Class Session is used to manage all connections to databases. To use a
 * database, just call the procSQL method to open a session, execute the sql
 * query, commit the statement, and close the session.
 */
public class Session {
	
	public static final String DRIVER_MS = "com.microsoft.sqlserver.jdbc.SQLServerDriver"; //SQL SERVER
	public static final String PROTOCOL_MSSQL = "jdbc:sqlserver://"; //SQL SERVER
	public static final String PROTOCOL_JTDS = "jdbc:jtds:sqlserver://"; ///JTDS
	public static final String PROTOCOL_IMPALA = "jdbc:impala://";
	public static final String DRIVER_SQL = "net.sourceforge.jtds.jdbc.Driver"; //JTDS
	public static final String DRIVER_IMPALA = "com.cloudera.impala.jdbc41.Driver"; //Impala
	public static final String DRIVER_HIVE2 = "com.cloudera.hive.jdbc41.HS2Driver";
	
	private static Logger log = Logger.getLogger(Session.class.getName());
    private transient SessionFactory sessionFactory;
    private DataSource db;
    private transient boolean connected = false;

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	
	/**
	 * Init a connection to a datasource
	 * <p>Default drive is to Sql Server</p>
	 * 
	 * @param db - DataSource to create a Session on
	 */
	public Session(DataSource db){
    	this.db = db;
    	init();
    }
	
	private void init(){
		try{
	    	if(db.getConnectionString() != null){
	    		setSessionFactoryConnectionString(db.getConnectionString());	
	    	}
	    	else{
	    		setSessionFactory();	    		
	    	}

    		connected = true;
    	}
    	catch(Exception e){
    		log.error(ErrorMessage.INSTANCE.getMessage("cannot_connection") + db.getName(), e);
    	}
	}

	/**
	 * Init connection to a datasource using the specified driver and datasource connection string
	 * 
	 * @param connection - Full connection string
	 * @throws Exception
	 */
    private void setSessionFactoryConnectionString(String connectionString) throws Exception{
        Configuration configuration = new Configuration();
        configuration.setProperty(Environment.URL, connectionString);
        configuration.setProperty(Environment.DRIVER, db.getDriver());
        configuration.setProperty(Environment.FORMAT_SQL, "true");
        configuration.setProperty(Environment.DIALECT, SqlServerDriverTypeExtension.class.getName());
        
        setSessionFactory(configuration);
    }
	
    /**
     * Init connection to a datasource using the specified driver and datasource server, name and login
     * 
     * @param connection - Protocol prefixed to the server and database connection string
     * @throws Exception
     */
    private void setSessionFactory() throws Exception{	
        Configuration configuration = new Configuration();
        configuration.setProperty(Environment.URL, db.getURL());
        configuration.setProperty(Environment.USER, db.getUserName());
        configuration.setProperty(Environment.PASS, db.getPassword());
        configuration.setProperty(Environment.DRIVER, db.getDriver());
        configuration.setProperty(Environment.DIALECT, SqlServerDriverTypeExtension.class.getName()); //No Impala/hive dialect hack but SQLServer works.
		
        setSessionFactory(configuration);
    }
    
    private void setSessionFactory(Configuration configuration) throws Exception{
    	//load driver class
    	if(configuration == null){
    		log.fatal("DB configuration" + ErrorMessage.INSTANCE.getMessage("null_value"));
    	}
    	else{
    		String driver = configuration.getProperty(Environment.DRIVER);
    		
	    	if(driver == null){
	    		log.fatal("DB driver " + ErrorMessage.INSTANCE.getMessage("null_value"));
	    		log.fatal(configuration.getProperties().get(Environment.URL));
	    		throw new Exception("DB Driver null");
	    	}
	    	
	    	try{
				Class.forName(driver).newInstance();
			}
			catch(Exception e){
				log.error(ErrorMessage.INSTANCE.getMessage("class_load") + driver, e);
				throw e;
			}
	    	
	    	ServiceRegistryBuilder builder = new ServiceRegistryBuilder().applySettings(configuration.getProperties());
	        sessionFactory = configuration.buildSessionFactory(builder.buildServiceRegistry());
    	}
    }

    /**
	 * Open session.
	 * 
	 * @return the org.hibernate. session
	 */
    public org.hibernate.Session openSession() {
    	if(connected == false){
    		init();
    	}
        return sessionFactory.openSession();
    }

    /**
	 * Close session.
	 */
    public void closeSession(){
    	if(!sessionFactory.isClosed())
    		sessionFactory.close();
    }

    /**
	 * Proc sql.
	 * 
	 * @param sql
	 *            the sql
     * @return 
	 * 
	 * @return the list<?>
	 */
    @SuppressWarnings("unchecked")
	public <T> T procSQL(final String sql, Map<String,Object> params, AliasToEntityMapResultTransformer aliasToEntityMapResultTransformer){
    	T data = null;
    	org.hibernate.Session session = null;
    	//long startTime = System.currentTimeMillis();
    	try{
    		session = openSession();
	        Transaction tx = session.beginTransaction();
	        Query query = session.createSQLQuery(sql);
	        setParameters(query, params);
			query.setResultTransformer(aliasToEntityMapResultTransformer);
			data = (T)query.list(); //no type safe hibernate query.list method
	        tx.commit();
    	}
    	catch(Exception e){
    		log.warn(sql + db.getName(), e);
    		throw e;
    	}
    	finally{
    		if(session != null){
    			session.close();
    		}
    		//log.warn("Query time = " + (System.currentTimeMillis() - startTime));
    	}
        return data;
    }
    
	private void setParameters(Query query, Map<String,Object> params){
		if(params != null){
			for(Entry<String, Object> parameter : params.entrySet()){
				Object value = null;
				if(parameter.getValue() != null){
					value = parameter.getValue();
				}
				query.setParameter(parameter.getKey(), value);
			}
		}
	}

	public <T> T procSQL(String sql){
		return procSQL(sql, null, null);
	}
	
    public List<Object[]> procSQLList(String sql, Map<String,Object> params){
    	return procSQL(sql, params, null);
    }
	
    public List<Object[]> procSQLList(String sql){
    	return procSQL(sql, null, null);
    }
    
    public List<Map<String,Object>> procSQLMap(String sql, Map<String,Object> params){
    	return procSQL(sql, params, AliasToEntityMapResultTransformer.INSTANCE);
    }

    public List<Map<String,Object>> procSQLMap(String sql){
    	return procSQL(sql, null, AliasToEntityMapResultTransformer.INSTANCE);
    }
    
    public Object procSQLFirst(String sql){
    	Object result = null;
    	List<Map<String,Object>> rs = procSQL(sql, null, AliasToEntityMapResultTransformer.INSTANCE);
    	if(rs != null && rs.size() > 0){
    		Map<String,Object> row = rs.get(0);
    		if(row != null && row.size() > 0){
    			result = row.values().iterator().next();
    		}
    	}
    	
    	return result;
    }
    
    public int updateSQL(String sql){
    	int rows = 0;
    	org.hibernate.Session session = null;
    	//long startTime = System.currentTimeMillis();
    	try{
    		session = openSession();
	        Transaction tx = session.beginTransaction();
	        rows = session.createSQLQuery(sql).executeUpdate();
	        tx.commit();
    	}
    	catch(Exception e){
    		log.warn(sql + db.getName(), e);
    		throw e;
    	}
    	finally{
    		if(session != null){
    			session.close();
    		}
    		//log.warn(" time = " + (System.currentTimeMillis() - startTime));
    	}
    	return rows;
    }
    
    public void queryWithTypes(String sql, Map<String, Object> params, List<Map<String, Object>> data, Map<String, String> types){
        final org.hibernate.Session session = openSession();
        try{
	        Transaction tx = session.beginTransaction();
			session.doReturningWork(new ReturnWork(sql, params, types, data));
	        tx.commit();
        }
        catch(Exception e){
    		log.warn(db.getName() + sql, e);
    		throw e;
    	}
    	finally{
    		if(session != null){
    			session.close();
    		}
    	}
    }
    
    public void runBulkSQL(final String sql, final String[] columns, final List<?> data, final String... sqlPre ){
        final org.hibernate.Session session = openSession();
        try{
	        //long start = System.currentTimeMillis();
	        Transaction tx = session.beginTransaction();
	        session.doWork(
					new Work(){
						@SuppressWarnings("unchecked")
						public void execute(Connection conn) throws SQLException{
							try(Statement stmt = conn.createStatement()){
								if(sqlPre != null){
									for(String s : sqlPre){
										stmt.executeUpdate(s);
									}
								}
							}
							catch (Exception e) {
								throw e;
							}
							try(PreparedStatement stmt = conn.prepareStatement(sql)){
								conn.setAutoCommit(false);
								
								//add inserts as batch
								for(Object row : data){
									if(row instanceof Map){
										bulk((Map<String, Object>) row, stmt, columns);
									}
									else if(row instanceof Object[]){
										bulk((Object[]) row, stmt);
									}
									
									stmt.addBatch();
								}
								stmt.executeBatch();
							}
							catch (Exception e) {
								throw e;
							}
						}
					});
	        tx.commit();
	        //log.info(System.currentTimeMillis() - start);
        }
        catch(Exception e){
    		log.warn(db.getName() + sql, e);
    		throw e;
    	}
    	finally{
    		if(session != null){
    			session.close();
    		}
    	}
    }
    
    private void bulk(Map<String, Object> row, PreparedStatement stmt, String[] columns) throws SQLException{
		int i = 1;
		for(String column : columns){
			stmt.setObject(i++, row.get(column));
		}
    }
    
    private void bulk(Object[] row, PreparedStatement stmt) throws SQLException{
		int i = 1;
    	for(Object column : row){
			stmt.setObject(i++, column);
		}
    }
    
    public void runBulkSQL(final Collection<String> sql){
        final org.hibernate.Session session = openSession();
        try{
	        //long start = System.currentTimeMillis();
	        Transaction tx = session.beginTransaction();
	        session.doWork(
					new Work(){
						public void execute(Connection conn) throws SQLException{
							try(Statement stmt = conn.createStatement()){
								conn.setAutoCommit(false);
								for(String insert : sql){
									stmt.addBatch(insert);
								}
								stmt.executeBatch();
							}
							catch (Exception e) {
								throw e;
							}
						}
					});
	        tx.commit();
	        //log.info(System.currentTimeMillis() - start);
        }
        catch(Exception e){
    		log.warn(db.getName() + sql, e);
    		throw e;
    	}
    	finally{
    		if(session != null){
    			session.close();
    		}
    	}
    }
    
    /**
     * Save or Update an object of Type T
     * @param <T>
     * @param o
     */
    public <T> void saveOrUpdate(T o){
        org.hibernate.Session session = openSession();
        Transaction tx = session.beginTransaction();
        session.saveOrUpdate(o);
        tx.commit();
        session.close();
    }
    
    /**
     * Save full Collection before committing.
     * @param <T> - Object Type
     * @param o - Data to be saved
     */
    public <T> void saveBulk(Collection<T> o){
        org.hibernate.Session session = openSession();
        Transaction tx = session.beginTransaction();
        for(T element:o){
        	session.save(element);
        }
        tx.commit();
        session.close();
    }
    
    /**
     * Save or Update full Collection before committing.
     * @param <T> - Object Type
     * @param o - Data to be saved
     */
    public <T> void updateBulk(Collection<T> o){
        org.hibernate.Session session = openSession();
        try{
	        Transaction tx = session.beginTransaction();
	        for(T element:o){
	        	session.update(element);
	        }
	        tx.commit();
        }
        catch(Exception e){
        	log.error("",e);
        	throw e;
        }
        finally{
        	session.close();
        }
    }
    
    public class ReturnWork implements ReturningWork<List<Map<String,Object>>>{
		private static final String VARIABLE_DELIMITER = ":";
		private Map<String, String> types;
		private List<Map<String,Object>> data;
		private String sql;
		private Map<String, Object> params;
		
		public ReturnWork(String sql, Map<String,Object> params, Map<String, String> types, List<Map<String,Object>> data){
			this.sql = sql;
			this.types = types;
			this.data = data;
			this.params = params;
		}

		public List<Map<String,Object>> execute(Connection conn) throws SQLException{
			try(PreparedStatement stmt = namedParameterStatement(conn, sql, params);
				ResultSet rs = stmt.executeQuery();){
				
				ResultSetMetaData metaData = rs.getMetaData();

				for(int i=0; i<metaData.getColumnCount(); i++){
					String label = metaData.getColumnLabel(i+1); //1 not 0 based index
					String type = metaData.getColumnTypeName(i+1);
							
					if(type.equals("varchar") || type.equals("decimal") || type.equals("numeric") || type.equals("nvarchar")){
						int precision = metaData.getPrecision(i+1);
						int scale = metaData.getScale(i+1);
						
						String scaleString = "";
						if(scale != 0){
							scaleString += "," + scale;
						}
						
						type += "(" + precision + scaleString + ")";
					}
					types.put(label, type);
				}

				while(rs.next()){
					Map<String,Object> row = new HashMap<String, Object>();
					for(String label : types.keySet()){
						row.put(label, rs.getObject(label));										
					}
					data.add(row);
				}
			}
			catch(Exception e){
				log.error(sql, e);
				throw e;
			}
			return null;
		}
		
		private PreparedStatement namedParameterStatement(Connection conn, String sql, Map<String,Object> params) throws SQLException{
			PreparedStatement stmt;
			if(params != null && params.size() > 0){
				
				//set named parames into preparedStatement
				//set order of ?
				List<Object> paramValues = new ArrayList<>(); //final list of values to use in prepared statement
				Map<Integer,Object> paramOrder = new HashMap<>(); //index of in the sql of the variable and it's value
				String sqlPreparedStatement = sql;
				
				for(Entry<String, Object> param : params.entrySet()){
					int fromIndex = 0; //index in the sql string to start from
					int fromIndexPreparedStatement = 0; //index in the sql with variable replaced with ?
					
					String variableName = VARIABLE_DELIMITER+param.getKey();
					fromIndex = sql.indexOf(variableName, fromIndex);
					while((fromIndexPreparedStatement=sqlPreparedStatement.indexOf(variableName, fromIndexPreparedStatement)) != -1){
						paramOrder.put(fromIndex, param.getValue());
						sqlPreparedStatement = sqlPreparedStatement.replaceFirst(variableName, "?");
					}
				}
	
				//sort list to get order of parameters
				List<Integer> order = new ArrayList<>(paramOrder.keySet());
				Collections.sort(order);
				for(Integer index : order){
					paramValues.add(paramOrder.get(index));
				}
				
				stmt = conn.prepareStatement(sqlPreparedStatement);
				
				//add params
				if(params != null){
					for(int i=0; i < paramValues.size(); i++){
						stmt.setObject(i+1, paramValues.get(i));
					}
				}
			}
			else{
				stmt = conn.prepareStatement(sql);
			}
			
			return stmt;
		}
	}
}
