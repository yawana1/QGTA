package db.modules;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import utils.Funcs;
import data.collection.JobDefinition;
import data.xml.objects.DBType;
import data.xml.objects.JobDefinitions;
import db.Session;
import db.Sessions;

/**
 * Build query for a given job def.  Retrive sql stored in a file and insert params.
 */
public class JobDefSQLBuilder{

	static Logger logger = Logger.getLogger(JobDefSQLBuilder.class.getName());

	private String sqlFullPath;
	private StringBuffer sbSQL;
	

	protected JobDefSQLBuilder() {}

	static private JobDefSQLBuilder _instance;

	static public JobDefSQLBuilder getInstance() {
		if (_instance == null) {
			_instance = new JobDefSQLBuilder();
		}
		return _instance;
	}
	
	public List<JobDefinition> execute(JobDefinitions jobDef, List<String> exp) throws Exception{
		List<?> list = null;
		List<JobDefinition> jobs = new ArrayList<JobDefinition>();
		try{
			generateSql(jobDef, exp);
			if(sbSQL!=null && sbSQL.length()>0){
				Session session = Sessions.INSTANCE.get(jobDef.getCrop(), DBType.VARIETY_REP);
				if(session==null){
					logger.error("Session is null ");
				}
				else{
					list = session.procSQLMap(sbSQL.toString());
	
					if(list == null || list.size() == 0){
						logger.warn("No jobs found for jobDef - " + jobDef.getFileName());
						return null;
					}
					
					for(Iterator<?> it = list.iterator(); it.hasNext();){
						@SuppressWarnings("unchecked")
						JobDefinition job = new JobDefinition((Map<String, Object>) it.next());
						jobs.add(job);
					}
				}
			}
		}catch(Exception e){
			String sql = null;
			if(sbSQL != null){
				sql = sbSQL.toString();
			}
			logger.error("sql : " + sql, e);
			throw e;
		}
		return jobs;
	}
	
	private void generateSql(JobDefinitions jobDef, List<String> exp) throws Exception{
		try{
			sqlFullPath = jobDef.getSqlFile();
			boolean exists = (new File(sqlFullPath)).exists();
			sbSQL = new StringBuffer();
			if(exists){
				String sql = Funcs.fileToString(sqlFullPath);
				if(sql.contains("[seasonName]") && jobDef.getSeasonName()!=null) sql = sql.replace("[seasonName]", "'"+jobDef.getSeasonName()+"'");
				if(sql.contains("[classId]") ){
					if(!jobDef.getClassId().isEmpty()){
						sql = sql.replace("[classId]", Funcs.listToSql(jobDef.getClassId(), true));
					}
					else if(jobDef.getClassId().isEmpty()){
						logger.error("File - " + jobDef.getFileName() + " missing classId ");
					}
				}
				if(sql.contains("[groupName]")){
					if(!jobDef.getName().isEmpty()){
						sql = sql.replace("[groupName]", jobDef.getName());
					}
					else {
						logger.error("File - " + jobDef.getFileName() + " missing groupName ");
					}
				}
				if(sql.contains("[analysisStage]")){
					if(!jobDef.getAnalysisStage().isEmpty()){
						sql = sql.replace("[analysisStage]", Funcs.listToSql(jobDef.getAnalysisStage(), true));
					}
					else if(jobDef.getAnalysisStage().isEmpty()){
						logger.error("File - " + jobDef.getFileName() + " missing analysisStage ");
					}
				}
				if(sql.contains("[analysisState]")){
					if(!jobDef.getAnalysisState().isEmpty()){
						sql = sql.replace("[analysisState]", Funcs.listToSql(jobDef.getAnalysisState(), true));
					}
					else if(jobDef.getAnalysisState().isEmpty()){
						logger.error("File - " + jobDef.getFileName() + " missing analysisState ");
					}
				}
				if(sql.contains("[randType]")){
					if(!jobDef.getRandType().isEmpty()){
						sql = sql.replace("[randType]", Funcs.listToSql(jobDef.getRandType(), true));
					}
					else if(jobDef.getRandType().isEmpty()){
						logger.error("File - " + jobDef.getFileName() + " missing randType ");
					}
				}
				sbSQL.append(sql);
				if(jobDef.getRestrictions() != null && !jobDef.getRestrictions().isEmpty()){
					for(String restriction : jobDef.getRestrictions())
						sbSQL.append("\t"+restriction+"\n");
				}
				if(exp != null && !exp.isEmpty()) sbSQL.append("\n and de.doc_name in ("+Funcs.listToSql(exp, true)+")\n");
			}else{
				logger.error("could not find path of sql file : "+sqlFullPath);
				throw new Exception();
			}
		}catch(Exception e){
			logger.error("", e);
			throw e;
		}
	}
}