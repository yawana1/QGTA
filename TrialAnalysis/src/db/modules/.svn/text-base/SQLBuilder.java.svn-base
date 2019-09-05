package db.modules;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.hibernate.jdbc.ReturningWork;

import utils.Funcs;
import utils.Globals.TrialType;
import data.collection.TrialData;
import data.xml.objects.App;
import data.xml.objects.Crop;
import data.xml.objects.DBType;
import data.xml.objects.Experiment;
import data.xml.objects.SqlColumn;
import data.xml.objects.Trait;
import data.xml.objects.Trial;
import db.Session;
import db.Sessions;

public class SQLBuilder implements RetrieveData {

	static Logger log = Logger.getLogger(SQLBuilder.class.getName());
	
	protected String sqlFileName;
	protected String sqlFullPath;
	protected List<SqlColumn> colList = new ArrayList<SqlColumn>();
	protected List<Trait> traitList = new ArrayList<Trait>();
	protected StringBuffer sbSQL = new StringBuffer();
	

	public SQLBuilder() {}	
	
	private StringBuffer getsbSQL(){
		return sbSQL;
	}
	
	private void setsbSQL(StringBuffer sb){
		this.sbSQL = sb;
	}

	public TrialData execute(){
		throw new NotImplementedException("");
	}
	
	/***
	 * Return the result from running the sql file specified in the trial
	 * @param - trial
	 */
	@SuppressWarnings("unused")
	public TrialData execute(Trial trial){

		List<Map<String,Object>> data = new ArrayList<>();
		Map<String,String> types = new HashMap<>();
		
		try{
			
			generateSql(trial);
			sbSQL= getsbSQL();
			//if the sql has more than just the select in it, execute it and populate the list
			if(sbSQL!=null && sbSQL.length()>7){
				Calendar startDate = null;
				Calendar endDate = null;
				int numberOfRecords = 0;
				
				org.hibernate.Session session = Sessions.INSTANCE.get(trial.getCrop(), DBType.VARIETY_REP).openSession();
				
				if(session==null){
					String error = "Cannot open session for " + DBType.VARIETY_REP;
					log.error(error);
					throw new Exception(error);
				}
				
				startDate = Calendar.getInstance();
				org.hibernate.Transaction tx = session.beginTransaction();

				final String sql = sbSQL.toString();
				
				//write sql file for debugging
				if(false){
					Path path = Paths.get(trial.getTrialWorkDirectory(), "data.sql");
					Files.write(path, sql.getBytes());
				}
				
				Sessions.INSTANCE.get(trial.getCrop(), DBType.VARIETY_REP).queryWithTypes(sql, null, data, types);
				
				types = session.doReturningWork(
						new ReturningWork <Map<String,String>>(){
							public Map<String,String> execute(Connection conn) throws SQLException{
								Map<String,String> types = new HashMap<>();
								try(PreparedStatement stmt = conn.prepareStatement(sql);){
									ResultSetMetaData metaData = stmt.getMetaData();
	
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
								}
								catch(Exception e){
									log.error("Sql = " + sql, e);
									throw e;
								}
								return types;
							}
						});

				endDate = Calendar.getInstance();
				tx.commit();
				
				data = Sessions.INSTANCE.get(trial.getCrop(), DBType.VARIETY_REP).procSQLMap(sql);
				if(data != null){
					numberOfRecords = data.size();
				}
				
				//send information about the query to a daily file to record total time and number of records
				createSqlLogFile(trial.getCrop(),trial.getTrialName(),startDate,endDate,numberOfRecords,sbSQL.toString());
				
				session.close();

			}
		}catch(Exception e){
			log.error("", e);
		}

		return new TrialData(data, types);
	}
	
	public static void postProcess(Trial trial){
		String sql = "" +
				" UPDATE " +
				"	wks_reps " +
				" SET " +
				"	 analysis_peopleid = " + trial.getAnalysisPeopleId() +
				"	,analysis_date = getdate() " +
				" FROM " +
				"	wks_reps " +
				"		INNER JOIN " +
				"	v_ExpBlock vEB " +
				"		ON WKS_REPS.BLOCK_ID = vEB.Block_Id " +
				" WHERE " +
				"		C_SET_SEASON = 9521 " +
				"	and vEB.C_SET_NAME IN ("+Funcs.listToSql(trial.getExperimentNames(), true)+")"
				;
		try{
			Session session = Sessions.INSTANCE.get(trial.getCrop(), DBType.VARIETY);
			session.updateSQL(sql);
		}catch(Exception e){
			log.error(sql, e);
		}
	}

	/**
	 * Generate Query from Sql template file specified in the trial
	 * @param trial
	 */
	private void generateSql(Trial trial){
		try{
			
			sbSQL.append("Select ");
			
			String analysisVarName = null;
			String experimentVarName = null;
			String seasonVarName = null;
			String seasonVarId = null;
			colList =  trial.getSqlColumns();
			if(colList!=null && colList.size()>0){
				for(int x=0; x<colList.size(); x++){
					String varName = colList.get(x).getVarietyName();
					if(varName.contains("ud."))continue;
					String name = colList.get(x).getName();
					if(name.equals("analysisState")){
						analysisVarName = varName;
					}
					if(name.equals("expName")){
						experimentVarName = varName;
					}
					if(name.equals("seasonName")){
						seasonVarName = varName;
					}
					if(name.equals("seasonId")){
						seasonVarId = varName;
					}
					if(name.equals("projectName")){
						seasonVarId = varName;
					}
					if(varName!=null && name!=null){
						if(x<colList.size()-1){
							sbSQL.append(varName+" as "+name+",");
						}else{
							sbSQL.append(varName+" as "+name+" ");
						}
					}
					
				}
				
			}else{
				log.error("trial.getSqlColumns is null");
			}
			
			//get list of traits (then look up actual variety trait name)
			traitList = trial.getTraits();
			if(traitList!=null && traitList.size()>0){
				for(int x=0; x<traitList.size(); x++){
					Trait t = traitList.get(x);
					

					String varietyTraitName = t.getVarName(); //t.getVar_name(); 
					String traitName = t.getName();


					//System.out.println("varietyTraitName_varname: " + varietyTraitName);
					//System.out.println("varietyTraitName_name: " + t.getName());
					//System.out.println("varietyTraitName_vatname: " + t.getVat_name());
					//System.out.println("--");
					if(varietyTraitName!=null && traitName!=null){
						sbSQL.append(",");
						//sbSQL.append(varietyTraitName);
						sbSQL.append(varietyTraitName+" as "+traitName+" ");
					}
					
				}
				
			}
			
			//read trial xml to get template sql file 		
			sqlFullPath = trial.getSqlTemplateFile();
			

			//check that file exists
			boolean exists = (new File(sqlFullPath)).exists();
			if(exists){
				//read file to get from/where of sql and append each line to the buffer
				BufferedReader reader = null;
				File inFile = null;
				String line = null;
				Scanner in = new Scanner(sqlFullPath);
				inFile = new File(in.next());
				
				reader = new BufferedReader(new FileReader(inFile));
				while ((line=reader.readLine()) != null){
					if(line.startsWith("where") || line.startsWith("and") || line.startsWith("from")){
						sbSQL.append(" ");
					}
					sbSQL.append(line.toString());
				}
				reader.close();
				in.close();
			}else{
				log.error("could not find path of sql file - " + sqlFileName);
				throw new Exception();
			}
			
			
			//get analysis state
			List<Integer> listAnalysisState = trial.getAnalysisState();
			if(listAnalysisState!=null && listAnalysisState.size()>0 && analysisVarName!=null){
				boolean orNull = false;
				StringBuffer sbAnalysisState = new StringBuffer();
				sbAnalysisState.append(" (");
				for(int x=0; x<listAnalysisState.size(); x++){
					if(listAnalysisState.get(x).equals(-1)){
						orNull = true;
					}
					else{
						if(x>0){
							sbAnalysisState.append(",");
						}
						sbAnalysisState.append(listAnalysisState.get(x).toString());
					}
				}
				sbAnalysisState.append(")");
				sbSQL.append(" and ( ");
				sbSQL.append(analysisVarName);
				sbSQL.append(" IN ");
				sbSQL.append(sbAnalysisState);
				
				if(orNull){
					sbSQL.append(" OR ");
					sbSQL.append(analysisVarName);
					sbSQL.append(" IS NULL ");
				}
				
				sbSQL.append(" ) ");
			}
			
			//get docname/expName
			if(TrialType.MULTI.equals(trial.getType())){
				Collection<Experiment> listExperiments = trial.getExperiments();
				
				//MultiYear
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
						sbExperiment.append(experimentVarName);
						sbExperiment.append(" = ");
						sbExperiment.append("'");
						sbExperiment.append(experiment.getName());
						sbExperiment.append("'");
						sbExperiment.append(" and ");
						sbExperiment.append(seasonVarId);
						sbExperiment.append(" = ");
						sbExperiment.append(seasonId);
						sbExperiment.append(" ");
						sbExperiment.append(")");
					}
					sbExperiment.append(")");
					sbSQL.append(sbExperiment);
					
					List<String> zones = trial.getZones();
					if(zones != null){
						sbSQL.append(" AND zn.list_value IN (");
						sbSQL.append(Funcs.listToSql(zones, true));
						sbSQL.append(")");
					}
					
					List<Integer> directions = trial.getDirections();
					if(directions != null){
						sbSQL.append(" AND dr.list_value IN (");
						sbSQL.append(Funcs.listToSql(directions, true));
						sbSQL.append(")");
					}
				}
				//Giant Experiments
				else if(trial.getExperimentNames() != null && trial.getExperimentNames().size() > 0 ){
					StringBuffer sbExperiment = addExperimentNames(trial.getExperimentNames());
					sbSQL.append(" and ");
					sbSQL.append(experimentVarName);
					sbSQL.append(" IN ");
					sbSQL.append(sbExperiment);
					
					//get seasonName
					String seasonName = trial.getSeasonName();
					addAndFilter(seasonVarName, seasonName, sbSQL);
				}
				else if(trial.getProjectName() != null && trial.getAnalysisStage() != null){
					//analysis Stage
					String analysisStage = trial.getAnalysisStage();
					String analysisStageVar = "stage.list_value";
					addAndFilter(analysisStageVar, analysisStage, sbSQL);
					
					//project name
					String projectName = trial.getProjectName();
					String projectNameVar = "project_name";
					addAndFilter(projectNameVar, projectName, sbSQL);
					
					//get seasonName
					String seasonName = trial.getSeasonName();
					addAndFilter(seasonVarName, seasonName, sbSQL);
				}
			}
			//Single Trials
			else{
				List<String> listExperiments = trial.getExperimentNames();
				if(listExperiments!=null && listExperiments.size()>0 && experimentVarName!=null){
					StringBuffer sbExperiment = addExperimentNames(listExperiments);
					sbSQL.append(" and ");
					sbSQL.append(experimentVarName);
					sbSQL.append(" IN ");
					sbSQL.append(sbExperiment);
				}
						
				//get seasonName
				String seasonName = trial.getSeasonName();
				addAndFilter(seasonVarName, seasonName, sbSQL);
			}
			
			if(trial.getTrialRestriction() != null){
				sbSQL.append(trial.getTrialRestriction());
			}
			
			setsbSQL(sbSQL);
		}catch(Exception e){
			log.error("error in SQLBuilder.generateSQL:", e);
		}
	}
	
	private void addAndFilter(String varName, String value, StringBuffer buffer){
		if(value != null && varName != null){
			buffer.append(" and ");
			buffer.append(varName);
			buffer.append(" = '");
			buffer.append(value);
			buffer.append("'");
		}
	}
	
	private StringBuffer addExperimentNames(List<String> listExperiments){
		StringBuffer sbExperiment = new StringBuffer();
		sbExperiment.append("(");
		for(int x=0; x<listExperiments.size(); x++){
			if(x>0){
				sbExperiment.append(",");
			}
			sbExperiment.append("'");
			sbExperiment.append(listExperiments.get(x).toString());
			sbExperiment.append("'");
		}
		sbExperiment.append(")");
		return sbExperiment;
	}

	/**
	 * 
	 * @param trial
	 * @param startTime
	 * @param endTime
	 * @param records
	 * @param sql
	 */
	public static void createSqlLogFile(Crop crop, String name,Calendar startTime, Calendar endTime, int records, String sql){
		
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
		SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMdd");
		
		Calendar cal = Calendar.getInstance();

		String fullFilePath = null;
		try {
			//create a new file for each Summary
			fullFilePath = App.INSTANCE.getLogDir()+ "/SQL_Log/SqlLog_"+df2.format(cal.getTime())+".txt";
			Path file = Paths.get(fullFilePath);

			//create dir if not there
			if(!Files.exists(file.getParent())){
				Funcs.createWithPermissions(file.getParent(),true);
			}

			Long x = endTime.getTimeInMillis()-startTime.getTimeInMillis();
		
			int seconds = (int)(x/1000)/60;
			int minutes = (int)(x/(1000*60))/60;
			int hours = (int)(x/(1000*60*60))/24;
			
			StringBuffer output = new StringBuffer();
			
			output.append("Crop: "+crop.name() + " Trial: "+name);
			output.append("\r\n");
			output.append(("Start Time: " + df.format(startTime.getTime())));
			output.append("\r\n");
			output.append(("End Time  : " + df.format(endTime.getTime())));
			output.append("\r\n");
			output.append(("Total Time : " + hours+ " hours, "+ minutes+" minutes, "+ seconds + " seconds, "+ x+" milliseconds")); 

			output.append("\r\n");
			output.append(("Number of records: " + records));
			output.append("\r\n");
			output.append(sql.toString());
			output.append("\r\n");
			output.append("--------------------------------------------------");
			output.append("\r\n");
			
			Funcs.createWithPermissions(file,false);
			Files.write(file, output.toString().getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);

		} catch (Exception e) {
			log.error("File creatation failed for: " + fullFilePath, e);
		}
	}
	
	/**
	 * Take a list of traits and process sql.
	 * @return 
	 */
	public static String traitsToSql(List<Trait> traits){
		StringBuffer buffer = new StringBuffer();
		if(traits!=null && traits.size()>0){
			for(Trait trait : traits){
				String varietyTraitName = trait.getVarName();
				String traitName = trait.getName();

				if(varietyTraitName!=null && traitName!=null){
					buffer.append(",");
					buffer.append(varietyTraitName+" as "+traitName+" ");
				}
			}
		}
		return buffer.toString();
	}
}