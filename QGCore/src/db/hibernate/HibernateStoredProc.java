package db.hibernate;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.hibernate.jdbc.Work;

/**
 * Helper class to call a stored proc through hibernate
 * @author Scott Smith
 *
 */
public class HibernateStoredProc {

	static Logger logger = Logger.getLogger(HibernateStoredProc.class.getName());
	
	/***
     * Call when no return value expected.
     * @param procName
     * @param params
     */
	public static ResultSet rs = null;
	
    public static ResultSet callStoredProc(org.hibernate.Session session, final String procName, final List<Object> params){
		try{
			Transaction tx = session.beginTransaction();
			session.doWork(
					new Work(){
						public void execute(Connection conn){
							String sql = " {call "+ procName+"(";
							for(int i = 0; i < params.size(); i++){
								if(i!=0){
									sql += ",";
								}
								sql += " ?";
							}
							sql += ")} ";
							try(CallableStatement stmt = conn.prepareCall(sql);){
								
								
								for(int i = 0; i < params.size(); i++){
									stmt.setObject(i+1, params.get(i));											
								}

								stmt.execute();
							} catch (Exception e) {
								logger.error("Error in store proc call" + sql,e);
							}
						}
					});
			tx.commit();
		}
		catch(Exception e){
			logger.error("",e);
		}
		
		return rs;
    }
}
