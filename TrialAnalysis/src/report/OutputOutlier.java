package report;

import java.util.ArrayList;
import java.util.List;

import utils.Funcs;
import utils.Globals.SummaryType;
import data.collection.ExpFBK;
import data.collection.ExpFBKs;
import data.xml.objects.App;
import data.xml.objects.DBType;
import data.xml.objects.Trial;
import db.Session;
import db.Sessions;

/**
 * Write outlier values back to Variety.
 * Outlier_Flag is set to true or false and then the outlier_trait_s is the comma seperated list of traits that are outliers.
 * 
 * @author Scott Smith
 *
 */
public class OutputOutlier extends OutputVariety{
	private ExpFBKs expFBKs;
	private int outlierYes;
	private int outlierNo;
	
	/**
	 * Define update sql to be run and get YES and NO id's from db.
	 */
	protected boolean init(Trial trial) throws Exception{
		expFBKs = trial.getFbks();
		String sqlFile = App.INSTANCE.getSqlDirectory()+"/outlier.sql";
		sql = " UPDATE exp_fbk SET C_OUTLIER_FLAG = ?, C_OUTLIER_TRAIT_S = ? WHERE p_rowid = ? ;";
		summaryType = SummaryType.outlierSummary;
		
		Session session = Sessions.INSTANCE.get(trial.getCrop(), DBType.VARIETY_REP);
		//get Yes and NO id values from Database
		String sqlYesNo = Funcs.fileToString(sqlFile);
		List<Object[]> result = session.procSQLList(sqlYesNo);
	
		for(Object[] row : result){
			if(row[0].equals("Yes")){
				outlierYes =  Funcs.getInt(row[1].toString());
			}
			else if(row[0].equals("No")){
				outlierNo =  Funcs.getInt(row[1].toString());
			}
		}
		
		return true;
	}

	/**
	 * Set the query params.  Outlier flag as yes or no and outlier traits as the list of trait that have outliers
	 */
	protected List<Object> createParams(Summary summary, int size) {
		//check if database needs updates
		List<Object> param = null;
		ExpFBK expFBK = expFBKs.getFirstFBK("rowId", Integer.parseInt(summary.getValues().get("rowId").toString()));
		if(expFBKs.getColMap().containsKey("outlierTraits")){
			if(		(expFBK.getValue("outlierTraits") == null && summary.getValues().get("outlierTraits") != null) 
				||	(!expFBK.getValue("outlierTraits").equals(summary.getValues().get("outlierTraits")))
			  ){
				param = new ArrayList<Object>(size);
		
				param.add((summary.getValues().get("outlier").equals("YES") ? outlierYes : outlierNo));
				param.add(summary.getValues().get("outlierTraits"));
				param.add(summary.getValues().get("rowId"));
			}
		}
		return param;
	}
}