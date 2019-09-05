package report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import data.xml.objects.Crop;
import data.xml.objects.DBType;
import data.xml.objects.Trial;
import db.Session;
import db.Sessions;
import utils.Globals.SummaryType;
import vat.Vat;

/**
 * Run bulk update queries on the given database.
 * 
 * @author Scott Smith
 * @see Output
 * @see OutputBrazil
 * @see OutputOutlier
 * @see OutputSoybean
 * @see BrazilYieldTransformation
 * 
 */
public abstract class OutputVariety implements Output{

	static Logger log = Logger.getLogger(OutputVariety.class.getName());
	protected String sql;
	protected SummaryType summaryType; 
	
	abstract protected boolean init(Trial trial) throws Exception;
	
	/**
	 * Create sql statement with params to be run on db
	 */
	public void runOutput(Vat vat, Trial trial, ReportOutput reportOutput) {
		try{
			boolean init = init(trial);
			//if(init && !TrialType.MULTI.equals(trial.getType())){
			if (init){
			int paramSize = sql.split("\\?").length; //find all params in sql as defined by char ?.
				Collection<Summary> summaries = reportOutput.get(summaryType);
				Collection<List<Object>> params = new ArrayList<List<Object>>(summaries.size());
				
				for(Summary summary : summaries){
					List<Object> param = createParams(summary, paramSize);
					if(param != null){
						params.add(param);
					}
				}
				
				if(summaries.size() > 0){
					updateDB(DBType.VARIETY, trial.getCrop(), sql, params);
				}
			}
		}
		catch (Exception e) {
			log.warn("", e);
		}
	}

	//implemented in Output specific classes
	protected abstract List<Object> createParams(Summary summary, int size);

	/***
	 * Bulk update variety db with sql statement run for each sets of params.
	 * @param <T>
	 * @param dbType - {@link DBType}
	 * @param crop
	 * @param sql - sql to run.
	 * @param params - List of params needs to match the order of "?" in sql statement.
	 */
	public static <T> void updateDB(DBType dbType, Crop crop, String sql, Collection<List<T>> params){
		try{
			Collection<String> bulkSql = new ArrayList<>();
			for(List<T> paramList : params){
				String valueSql = sql;
				for(T param : paramList){
					//TODO: fix hack
					int index = valueSql.indexOf("?");
					if(param instanceof String &&
							valueSql.charAt(index + 1) != '=' && valueSql.charAt(index + 2) != '='){
						valueSql = valueSql.replaceFirst("\\?", "'" + param + "'");
					}
					else{
						valueSql = valueSql.replaceFirst("\\?", ""+param);
					}
				}
				bulkSql.add(valueSql);
			}
			if(bulkSql.size() > 0){
				Session session = Sessions.INSTANCE.get(crop, dbType);
				session.runBulkSQL(bulkSql);				
			}
		}
		catch(Exception e){
			log.warn("Error in updating to " + dbType, e);
		}
	}
}