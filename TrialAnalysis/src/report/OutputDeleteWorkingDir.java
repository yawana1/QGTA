package report;

import java.io.File;

import org.apache.log4j.Logger;

import utils.Funcs;
import vat.Vat;
import data.xml.objects.Trial;

/**
 * Delete the trials working folder at the end of analysis.
 * 
 * @author Scott Smith
 * @see Output
 */
public class OutputDeleteWorkingDir implements Output{

	static Logger logger = Logger.getLogger(OutputDeleteWorkingDir.class.getName());

	/**
	 * Delete working dir
	 */
	public void runOutput(Vat vat, Trial trial, ReportOutput reportOutput) {		
		Funcs.deleteDir(new File(trial.getTrialWorkDirectory()));
	}
}
