package report;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import utils.Globals.SummaryType;
import vat.Vat;
import data.xml.objects.Trial;

/**
 * Call Vat database stored proc to add each summary report file.  Current parameters are the full path file name
 * and crop name.  File path needs to be in Windows format as it's loaded using Sql Server function.
 * 
 * @author Scott Smith
 * @see Output
 */
public class OutputVat implements Output{

	static Logger log = Logger.getLogger(OutputVat.class.getName());
	
	/**
	 * Write each summary report to Vat.  EntrySummary.txt, LocSummary.txt etc.
	 */
	public void runOutput(Vat vat, Trial trial, ReportOutput reportOutput) {
		//put the multimap first
		SummaryType[] outputOrder = SummaryType.values();
		for(int i=0; i<outputOrder.length; i++){
			if(SummaryType.multiMapSummary.equals(outputOrder[i])){
				outputOrder[i] = outputOrder[0];
				outputOrder[0] = SummaryType.multiMapSummary;
				break;
			}
		}
		
		for(SummaryType sumType : outputOrder){
			String key = ReportOutput.getTextFilePath(trial, sumType, true);
			String sp = reportOutput.vatStoredProcs.get(key);
			try{
				if(sp != null){ //no value means don't send to Vat
					String fileName = key;
					String spName = sp;
					log.info("fileName: " + fileName);
					log.info("spName: " + spName);
					
					List<Object> params = new ArrayList<Object>();
					params.add(fileName);
					params.add(trial.getCrop().toString());
					Vat.uploadFileToStoredProc(spName, params);
				}
			}catch(Exception e){
				log.error("",e);
			}
		}
	}
}
