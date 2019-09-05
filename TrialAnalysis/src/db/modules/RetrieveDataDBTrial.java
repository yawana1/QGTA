package db.modules;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import data.collection.TrialData;
import data.xml.objects.DBType;
import data.xml.objects.Experiment;
import data.xml.objects.JobDefinitions;
import data.xml.objects.SqlColumn;
import data.xml.objects.Trait;
import data.xml.objects.Trial;
import db.Session;
import db.Sessions;
import main.trialanalysis.TrialAnalysisJobProcess;
import utils.Funcs;
import utils.Globals.TrialType;

public class RetrieveDataDBTrial implements RetrieveData {

	protected  static Logger log = Logger.getLogger(RetrieveDataDBTrial.class.getName());
	
	private List<String> experiments;
	private JobDefinitions jobDefinitions;
	private Trial trial;
	
	public RetrieveDataDBTrial(JobDefinitions jobDefinitions, List<String> experiments){
		this.jobDefinitions = jobDefinitions;
		this.experiments = experiments;
	}
	
	private String generateSql(){
		return generateSql(jobDefinitions.getSqlFile());
	}
	
	private String generateSql(String sqlFile){
		
		//get sql template from file
		if(sqlFile == null){
			String errorMsg = "Sql file not set";
			log.fatal(errorMsg);
			return null;
		}
		
		Path sqlFilePath = Paths.get(sqlFile);
		String sql = null;
		if(Files.isReadable(sqlFilePath)){
			try {
				sql = new String(Files.readAllBytes(sqlFilePath));
			} catch (IOException e) {
				log.error("", e);
			}
		}
		else{
			String errorMsg = "Sql file not readable";
			log.fatal(errorMsg);
		}
		
		if(sql != null){
			//update sql template
			if(sql.contains("[seasonName]") && jobDefinitions.getSeasonName()!=null){
				//if user defined experiment list don't need to set season.
				if(jobDefinitions.isUserDefined() && jobDefinitions.getExperiments() != null){
					sql = sql.replace("[seasonName]"," LIKE '%' ");					
				}
				else{
					sql = sql.replace("[seasonName]", " = '"+jobDefinitions.getSeasonName()+"'");

				}
			}
			if(sql.contains("[groupName]")){
				String groupName = " de.doc_name ";
				String groupId = " de.doc_id ";
				if(jobDefinitions.getName() != null && !jobDefinitions.getName().isEmpty()){
					if(jobDefinitions.isUserDefined()){
						groupName = String.format("'%s'", jobDefinitions.getName());
						groupId = "'1'";
					}
				}
				
				sql = sql.replace("[groupName]", groupName);
				sql = sql.replace("[groupId]", groupId);
			}
			if(sql.contains("[classId]") ){
				if(!jobDefinitions.getClassId().isEmpty()){
					sql = sql.replace("[classId]", Funcs.listToSql(jobDefinitions.getClassId(), true));
				}
				else if(jobDefinitions.getClassId().isEmpty()){
					log.error("File - " + jobDefinitions.getFileName() + " missing classId ");
				}
			}
			if(sql.contains("[zone]") ){
				if(!jobDefinitions.getZone().isEmpty()){
					sql = sql.replace("[zone]", jobDefinitions.getZone());
				}
				else if(jobDefinitions.getClassId().isEmpty()){
					log.error("File - " + jobDefinitions.getFileName() + " missing classId ");
				}
			}
			if(sql.contains("[analysisStage]")){
				if(!jobDefinitions.getAnalysisStage().isEmpty()){
					sql = sql.replace("[analysisStage]", Funcs.listToSql(jobDefinitions.getAnalysisStage(), true));
				}
				else if(jobDefinitions.getAnalysisStage().isEmpty()){
					log.error("File - " + jobDefinitions.getFileName() + " missing analysisStage ");
				}
			}
			if(sql.contains("[analysisState]")){
				if(!jobDefinitions.getAnalysisState().isEmpty()){
					sql = sql.replace("[analysisState]", Funcs.listToSql(jobDefinitions.getAnalysisState(), true));
				}
				else if(jobDefinitions.getAnalysisState().isEmpty()){
					log.error("File - " + jobDefinitions.getFileName() + " missing analysisState ");
				}
			}
			if(sql.contains("[randType]")){
				if(!jobDefinitions.getRandType().isEmpty()){
					sql = sql.replace("[randType]", Funcs.listToSql(jobDefinitions.getRandType(), true));
				}
				else if(jobDefinitions.getRandType().isEmpty()){
					log.error("File - " + jobDefinitions.getFileName() + " missing randType ");
				}
			}
			if(sql.contains("[experimentLevelZone]")){
				StringBuffer experimentZones = new StringBuffer(); 
				List<String> zones = jobDefinitions.getZones();
				if(zones != null){
					experimentZones.append(" AND zn.list_value IN (");
					experimentZones.append(Funcs.listToSql(zones, true));
					experimentZones.append(")");
				}
				
				sql = sql.replace("[experimentLevelZone]", experimentZones.toString());
			}
			
			if(sql.contains("[restrictions]")){
				StringBuffer sqlRestriction = new StringBuffer();
				if(jobDefinitions.getRestrictions() != null && !jobDefinitions.getRestrictions().isEmpty()){
					for(String restriction : jobDefinitions.getRestrictions()){
						sqlRestriction.append("\t"+restriction+"\n");
					}
					
				}
				
				sql = sql.replace("[restrictions]", sqlRestriction.toString());
			}
			if(sql.contains("[experiments]")){
				String sqlExperiments = "";
				String docName = " de.doc_name ";
				String seasonIdName = " de.season_id ";
				if(jobDefinitions.isUserDefined()){
					Collection<Experiment> listExperiments = Experiment.getExperiments(jobDefinitions.getExperiments());
					
					if(listExperiments!=null && listExperiments.size()>0){
						StringBuffer sbExperiment = new StringBuffer();
						sbExperiment.append(" AND (");
						boolean firstRow = true;
						for(Experiment experiment : listExperiments){
							String seasonId = experiment.getSeason().getSeasonId();
							if(firstRow){
								firstRow = false;
							}
							else{
								sbExperiment.append(" OR ");
							}
							sbExperiment.append("(");
							sbExperiment.append(docName);
							sbExperiment.append(" = ");
							sbExperiment.append("'");
							sbExperiment.append(experiment.getName());
							sbExperiment.append("'");
							sbExperiment.append(" and ");
							sbExperiment.append(seasonIdName);
							sbExperiment.append(" = ");
							sbExperiment.append(seasonId);
							sbExperiment.append(" ");
							sbExperiment.append(")");
						}
						sbExperiment.append(")");

						sqlExperiments = sbExperiment.toString();
					}
				}

				if(experiments != null && !experiments.isEmpty()){
					sqlExperiments += ("\n AND de.doc_name in ("+Funcs.listToSql(experiments, true)+")\n");
				}
				sql = sql.replace("[experiments]", sqlExperiments);
			}
			
			String trialTemplate = jobDefinitions.getTrialFile();
			if(trial == null){
				trial = TrialAnalysisJobProcess.getTrial(trialTemplate);
			}
			
			//trial filters
			if(sql.contains("[trialRestrictions]")){
				StringBuffer sqlRestriction = new StringBuffer();
				if(jobDefinitions.getTrialRestrictions() != null && !jobDefinitions.getTrialRestrictions().isEmpty()){
					sqlRestriction.append("\t"+jobDefinitions.getTrialRestrictions()+"\n");
				}
				
				List<Integer> directions = jobDefinitions.getDirections();
				if(directions != null){
					sqlRestriction.append(" AND dr.list_value IN (");
					sqlRestriction.append(Funcs.listToSql(directions, true));
					sqlRestriction.append(")");
				}
				
				//set locId for Silage
				if(TrialType.MULTI.equals(trial.getType()) && jobDefinitions.getClassId().contains(2) && !jobDefinitions.getRegion().equals("ALL")){
					String locs = Funcs.listToSql(getLocs(), false);
					if(locs != null && !locs.isEmpty()){
						sqlRestriction.append(" AND df.field_id IN (");
						sqlRestriction.append(locs);
						sqlRestriction.append(")");
					}
				}
				
				sql = sql.replace("[trialRestrictions]", sqlRestriction.toString());
			}
			
			//add in traits
			if(sql.contains("[traits]")){
				List<Trait> traits = trial.getTraits();
				if(!traits.isEmpty()){
					sql = sql.replace("[traits]", SQLBuilder.traitsToSql(traits));
				}
				else{
					log.error("File - " + jobDefinitions.getFileName() + " missing traits ");
				}
			}
			
			if(sql.contains("[columns]")){
				List<SqlColumn> columns = trial.getSqlColumns();
				if(columns != null && !columns.isEmpty()){
					StringBuffer buffer = new StringBuffer();
					for(SqlColumn sqlColumn : columns){
						buffer.append(sqlColumn.toSql());
					}
					
					sql = sql.replace("[columns]", buffer.subSequence(0, buffer.length()-1));
				}
				else if(jobDefinitions.getRandType().isEmpty()){
					log.error("File - " + jobDefinitions.getFileName() + " missing randType ");
				}
			}
		}

		return sql;
	}
	
	public TrialData execute(Trial trial){
		this.trial = trial;
		return execute();
	}
	
	public List<String> getLocs(){
		List<String> result = new ArrayList<>();
		
		try{
			//get seasonId's
			
			
			//get sql template from file
			String sqlFile = jobDefinitions.getSqlFile();
			
			Path sqlFilePath = Paths.get(sqlFile).resolveSibling("vat.sql");
			String sql = "";
			if(Files.isReadable(sqlFilePath)){
				try {
					sql = new String(Files.readAllBytes(sqlFilePath));
				} catch (IOException e) {
					log.error("", e);
				}
			}
			else{
				String errorMsg = "Sql file not readable";
				log.fatal(errorMsg);
			}

			
			
			Map<String,Object> params = new HashMap<>();
			params.put("name", jobDefinitions.getRegion());
			sql = sql.replace(":seasonId", Funcs.listToSql(getSeasonIds(DBType.VARIETY_REP, "seasonId.sql"), false));
			
			Session session = Sessions.INSTANCE.get(jobDefinitions.getCrop(), DBType.VAT);
			
			if(session==null){
				String error = "Cannot open session for " + DBType.VAT;
				log.error(error);
				throw new Exception(error);
			}
			List<Map<String, Object>> data = new ArrayList<>();
			session.queryWithTypes(sql, params, data, new HashMap<String,String>());
			
			if(data != null){
				List<String> buffer = new ArrayList<>();
				for( Map<String, Object> row : data){
					buffer.add(row.get("locId").toString());
				}
				result = buffer;
			}
		}
		catch(Exception e){
			log.error("", e);
		}
		
		return result;
	}
	
	private List<String> getSeasonIds(DBType dbType, String sqlFile) throws Exception{
		List<String> result = new ArrayList<>();

		Path sqlFilePath = Paths.get(jobDefinitions.getSqlFile()).resolveSibling(sqlFile);
		String sql = generateSql(sqlFilePath.toString());
		
		Session session = Sessions.INSTANCE.get(jobDefinitions.getCrop(), dbType);
		
		if(session==null){
			String error = "Cannot open session for " + dbType;
			log.error(error);
			throw new Exception(error);
		}
		List<Map<String, Object>> data = new ArrayList<>();
		session.queryWithTypes(sql, null, data, new HashMap<String,String>());
		
		if(data != null){
			List<String> buffer = new ArrayList<>();
			for( Map<String, Object> row : data){
				buffer.add(row.get("seasonId").toString());
			}
			result = buffer;
		}
		
		return result;
	}
	
	public TrialData execute(){
		final String sql = generateSql();

		List<Map<String,Object>> data = new ArrayList<>();
		Map<String,String> types = new HashMap<>();
		
		try{
		
			if(sql != null){
				//metrics
				Calendar startDate = null;
				Calendar endDate = null;
				int numberOfRecords = 0;
				
				Session session = Sessions.INSTANCE.get(jobDefinitions.getCrop(), DBType.VARIETY_REP);
				
				if(session==null){
					String error = "Cannot open session for " + DBType.VARIETY_REP;
					log.error(error);
					throw new Exception(error);
				}
				
				startDate = Calendar.getInstance();
	
				//write sql file for debugging
				//Path path = Paths.get(trial.getTrialWorkDirectory(), "data.sql");
				//Files.write(Funcs.createWithPermissions(path, false), sql.getBytes());
				
				session.queryWithTypes(sql, null, data, types);
				if(data != null){
					numberOfRecords = data.size();
				}
				
				//send information about the query to a daily file to record total time and number of records
				endDate = Calendar.getInstance();
				SQLBuilder.createSqlLogFile(jobDefinitions.getCrop(),jobDefinitions.getFileName(), startDate, endDate, numberOfRecords, sql);
			}
		}
		catch(Exception e){
			log.error("", e);
		}

		return new TrialData(data, types);
	}
}