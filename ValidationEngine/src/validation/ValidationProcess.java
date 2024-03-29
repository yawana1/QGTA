/*
 * 
 */
package validation;

import java.nio.file.Path;

public class ValidationProcess {
	private Validator objValidator;
	
	public ValidationProcess(String strInSeasonName,
			                 Path objInPreviousReportsDirectoryPath,Path objInCurrentReportsDirectoryPath,
			                 String classType, String strInTrialName, String strInProjectName, 
			                 String strCrop, boolean forceMatch, Path objOutputReportPath, String objOutputFormat) {
		this.objValidator = new Validator(strInSeasonName
				                          ,objInPreviousReportsDirectoryPath,objInCurrentReportsDirectoryPath,
				                          classType,strInTrialName,strInProjectName,strCrop,
				                          forceMatch,objOutputReportPath,objOutputFormat);
	}
	
	public void start() {
		this.objValidator.run();
	}
}
