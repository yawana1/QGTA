/*
 * 
 */
package data;

import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

///////////////////////////////////////////////////////////////////////////
// The identifier attributes of the report object are:                       
// 1) the trial name. Example: DC591L                                      
// 2) the relative path. Example: Yield_Trial/10E/L_Elite_Genetics/DC591L  
///////////////////////////////////////////////////////////////////////////
public class Report {

	static Logger objLogger = Logger.getLogger(Report.class.getName());
	public static final String NEWLINE = System.getProperty("line.separator");
	private String strTrialName;
	private Path objAbsolutePath;
	private String strRelativePath;
	private String strTimeStamp;
	private String strRegularExpression;
	private Pattern objPattern;
	private Path objEntrySummaryFilePath;
	private String strCreationTime;
	private Map<String, Integer> mapEntryGenotypesNumbers;
	private Map<String, Map<String, Double>> mapEntryEstimates;
	private Map<String, Map<String, Double>> mapEntryCoreChecks;	
	private Map<String, Map<String, Double>> mapEntryRanks;	
	private Map<String, Integer> mapEntryRawCounts;
	private Path objExlSummaryFilePath;
	private Map<String, Map<String, Double>> mapExlRawMeans;
	private Map<String, Map<String, Double>> mapExlCoreChecks;	
	private Map<String, Map<String, Double>> mapExlCavs;		
	private Path objExperimentSummaryFilePath;
	private Map<String, Integer> mapExperimentLocationsNumbers;
	private Path objLocSummaryFilePath;
	private Map<String, Map<String, Double>> mapLocEstimates;
	private Map<String, Map<String, Double>> mapLocCvs;
	private Map<String, Map<String, Double>> mapLocCheckCvs;
	private Map<String, Map<String, Double>> mapLocRawCoreCheckMeans;	
	
	public Report(String strInTrialName,
			      Path objInAbsolutePath) {
		String[] strArrayLineParts;
		
		this.strTrialName = strInTrialName;
		this.objAbsolutePath = objInAbsolutePath;
		if (this.objAbsolutePath != null) {
			try {
				if (!(this.objAbsolutePath.isAbsolute())) {
					throw new Exception("The path " + 
			                        	this.objAbsolutePath.toString() + 
						            	" is not absolute");
				}
				else {
					if (!(this.objAbsolutePath.toString().contains("reports")))
					{
						throw new Exception("The absolute path " + 
				                        	this.objAbsolutePath.toString() + 
				                        	" does not contain the directory: \"reports\"");
					}
					else {
						// changed from reports to run
						//strArrayLineParts = this.objAbsolutePath.toString().split("reports\\/");
						strArrayLineParts = this.objAbsolutePath.toString().split("runs\\/");
						// it splits based on the word reports
						// Changed by U755482
						// Now it does not make sense anymore, because before reports was in the middle of the path
						// now it is at the end.
						
						// removed by u755482 if (!(1 < strArrayLineParts.length)) 
						// removed by u755482{
						// removed by u755482	throw new Exception("The absolute path " + 
						// removed by u755482						this.objAbsolutePath.toString() + 
						// removed by u755482                    	" is too short to give any trial information");
						// removed by u755482}
						// removed by u755482else 
						// removed by u755482{
							/* Change done by u587000, Oct 4, 2013.
							 * Remove the "ARCHIVE_" folder from the relative path */
							// this.strRelativePath = strArrayLineParts[strArrayLineParts.length - 1];
						
							
						
							String curRelativePath = strArrayLineParts[strArrayLineParts.length - 1];
							if (curRelativePath.contains("ARCHIVE_")){
								// This is a previous report.
								// u755482 had to change to be according to the current organization
								//this.strRelativePath = curRelativePath.split("\\/ARCHIVE_")[0];
								
								// relative path will be used to identify if work and report have the same
								// relative path before calculating correlation in validator - connect report with trial
								
								this.strRelativePath = curRelativePath.split("\\/archive")[0];
							}
							else{
								// This is a current report.
								
								// changed by U755482 - we need both work and report to have the same relative path
								// this.strRelativePath = curRelativePath
								this.strRelativePath = curRelativePath.split("\\/reports")[0];
							}
							if (this.strRelativePath.equals("")) {
								throw new Exception("There is no relative path to be captured from the absolute path " + 
						                        	this.objAbsolutePath.toString());
							}
						// removed by u755482}
					}
				}
			}
			catch (Exception e) {
				objLogger.error("Report.Report", 
	                        	e);
				throw new RuntimeException();
			}
		}
		else {
			this.strRelativePath = "";
		}
		this.strTimeStamp = "";
		///////////////////  
		// YYYYMMDDHHMMSS  
        ///////////////////
		this.strRegularExpression = "^[0-9]{14}$";
		this.objPattern = Pattern.compile(this.strRegularExpression);
		this.objEntrySummaryFilePath = null;
		this.strCreationTime = null;
		this.mapEntryGenotypesNumbers = null;	
		this.mapEntryEstimates = null;
		this.mapEntryCoreChecks = null;
		this.mapEntryRanks = null;		
		this.mapEntryRawCounts = null;
		this.objExlSummaryFilePath = null;
		this.mapExlRawMeans = null;
		this.mapExlCoreChecks = null;
		this.mapExlCavs = null;		
		this.objExperimentSummaryFilePath = null;
		this.mapExperimentLocationsNumbers = null;
		this.objLocSummaryFilePath = null;
		this.mapLocEstimates = null;
		this.mapLocCvs = null;
		this.mapLocCheckCvs = null;
		this.mapLocRawCoreCheckMeans = null;	
	}
	
	@Override
	public boolean equals(Object objIn) {
		if ((objIn != null) && 
			(objIn instanceof Report)) {
			//return ((this.strTrialName.equals(((Report)objIn).getTrialName())) && 
			//		(this.strRelativePath.equals(((Report)objIn).getRelativePath())));
			// Now it matches only by trial name and not by (relative path and trial name) anymore - changed by U755482
			return ((this.strTrialName.equals(((Report)objIn).getTrialName())));

		}
		else {
			return false;
		}
	}

	public void setTimeStamp(String strInTimeStamp) {
		try {
			if (!(this.checkTimeStampFormat(strInTimeStamp))) {
				throw new Exception("Timestamp format incorrect: " + strInTimeStamp + 
				                    " when treating the report: " + this.strTrialName + 
				                    " in the path " + this.objAbsolutePath.toString());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			objLogger.error("Report.setTimeStamp", 
	                        e);
			throw new RuntimeException();
		}
		this.strTimeStamp = strInTimeStamp;
	}

	public void setEntrySummaryFilePath(Path objInEntrySummaryFilePath) {
		this.objEntrySummaryFilePath = objInEntrySummaryFilePath;
	}
	
	public void setCreationTime(String strInCreationTime) {
		this.strCreationTime = strInCreationTime;
	}
	
	public void setEntryGenotypesNumbers(Map<String, Integer> mapInRecordsNumbers) {
		this.mapEntryGenotypesNumbers = mapInRecordsNumbers;
	}
	
	public void setEntryEstimates(Map<String, Map<String, Double>> mapInEntryEstimatesByTrait) {
		this.mapEntryEstimates = mapInEntryEstimatesByTrait;
	}

	public void setEntryCoreChecks(Map<String, Map<String, Double>> mapInEntryCoreChecksByTrait) {
		this.mapEntryCoreChecks = mapInEntryCoreChecksByTrait;
	}

	public void setEntryRanks(Map<String, Map<String, Double>> mapInEntryRanksByTrait) {
		this.mapEntryRanks = mapInEntryRanksByTrait;
	}
	
	public void setEntryRawCounts(Map<String, Integer> mapInEntryRawCountsByTrait) {
		this.mapEntryRawCounts = mapInEntryRawCountsByTrait; 
	}
	
	public void setExlSummaryFilePath(Path objInExlSummaryFilePath) {
		this.objExlSummaryFilePath = objInExlSummaryFilePath;
	}
	
	public void setExlRawMeans(Map<String, Map<String, Double>> mapInExlRawMeansByTrait) {
		this.mapExlRawMeans = mapInExlRawMeansByTrait;
	}

	public void setExlCoreChecks(Map<String, Map<String, Double>> mapInExlCoreChecksByTrait) {
		this.mapExlCoreChecks = mapInExlCoreChecksByTrait;
	}
	
	public void setExlCavs(Map<String, Map<String, Double>> mapInExlCavsByTrait) {
		this.mapExlCavs = mapInExlCavsByTrait;
	}

	public void setExperimentSummaryFilePath(Path objInExperimentSummaryFilePath) {
		this.objExperimentSummaryFilePath = objInExperimentSummaryFilePath; 
	}
	
	public void setExperimentLocationsNumbers(Map<String, Integer> mapInExperimentLocationsNumbers) {
		this.mapExperimentLocationsNumbers = mapInExperimentLocationsNumbers;
	}
	
	public void setLocSummaryFilePath(Path objInLocSummaryFilePath) {
		this.objLocSummaryFilePath = objInLocSummaryFilePath; 
	}

	public void setLocEstimates(Map<String, Map<String, Double>> mapInLocEstimatesByTrait) {
		this.mapLocEstimates = mapInLocEstimatesByTrait;
	}

	public void setLocCvs(Map<String, Map<String, Double>> mapInLocCvsByTrait) {
		this.mapLocCvs = mapInLocCvsByTrait;
	}

	public void setLocCheckCvs(Map<String, Map<String, Double>> mapInLocCheckCvsByTrait) {
		this.mapLocCheckCvs = mapInLocCheckCvsByTrait;
	}
	
	public void setLocRawCoreCheckMeans(Map<String, Map<String, Double>> mapInLocRawCoreCheckMeansByTrait) {
		this.mapLocRawCoreCheckMeans = mapInLocRawCoreCheckMeansByTrait;
	}
	
	public String getTrialName() {
		return this.strTrialName;
	}
	
	public String getAbsolutePath() {
		return this.objAbsolutePath.toString(); 
	}
	
	public String getRelativePath() {
		return this.strRelativePath;
	}
	
	public String getTimeStamp() {
		return this.strTimeStamp;
	}

	public Path getEntrySummaryFilePath() {
		return this.objEntrySummaryFilePath;
	}
	
	public String getCreationTime() {
		return this.strCreationTime;
	}
	
	public Map<String, Integer> getEntryGenotypesNumbers() {
		return this.mapEntryGenotypesNumbers;
	}

	public Map<String, Map<String, Double>> getEntryEstimates() {
		return this.mapEntryEstimates;
	}
	
	public Map<String, Map<String, Double>> getEntryCoreChecks() {
		return this.mapEntryCoreChecks;
	}
	
	public Map<String, Map<String, Double>> getEntryRanks() {
		return this.mapEntryRanks;
	}
	
	public Map<String, Integer> getEntryRawCounts() {
		return this.mapEntryRawCounts;
	}
	
	public Path getExlSummaryFilePath() {
		return this.objExlSummaryFilePath;
	}
	
	
	public Path getAboslutePath(){
		return this.objAbsolutePath;
	}
	
	public Map<String, Map<String, Double>> getExlRawMeans() {
		return this.mapExlRawMeans;
	}

	public Map<String, Map<String, Double>> getExlCoreChecks() {
		return this.mapExlCoreChecks;
	}
	
	public Map<String, Map<String, Double>> getExlCavs() {
		return this.mapExlCavs;
	}

	public Path getExperimentSummaryFilePath() {
		return this.objExperimentSummaryFilePath; 
	}
	
	public Map<String, Integer> getExperimentLocationsNumbers() {
		return this.mapExperimentLocationsNumbers;	
	}
	
	public Path getLocSummaryFilePath() {
		return this.objLocSummaryFilePath; 
	}
	
	public Map<String, Map<String, Double>> getLocEstimates() {
		return this.mapLocEstimates;
	}
	
	public Map<String, Map<String, Double>> getLocCvs() {
		return this.mapLocCvs;
	}

	public Map<String, Map<String, Double>> getLocCheckCvs() {
		return this.mapLocCheckCvs;
	}
	
	public Map<String, Map<String, Double>> getLocRawCoreCheckMeans() {
		return this.mapLocRawCoreCheckMeans;
	}
	
	public boolean checkTimeStampFormat(String strInTimeStamp) {
		if (this.objPattern.matcher(strInTimeStamp).find()) {
			return true;
		}
		else {
			return false;
		}
	}

	public boolean isOlderThan(String strInTimeStamp) {
		try {
			if (this.strTimeStamp.equals("")) {
				throw new Exception("The timestamp attribute of this report is empty");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			objLogger.error("Report.isOlderThan", 
	                        e);
			throw new RuntimeException();
		}
		
		return (this.strTimeStamp.compareTo(strInTimeStamp) < 0);
	}

	@Override 
	public String toString() {
		String strOutput;
		
		strOutput = "Name Trial: " + this.strTrialName + NEWLINE;		
		strOutput = strOutput + "Absolute Path: " + this.objAbsolutePath.toString() + NEWLINE;
		strOutput = strOutput + "Relative Path: " + this.strRelativePath + NEWLINE;		
		strOutput = strOutput + "Timestamp: " + this.strTimeStamp + NEWLINE;
		strOutput = strOutput + "entrySummary File Path: " + this.objEntrySummaryFilePath.toString() + NEWLINE;
		strOutput = strOutput + "Creation Time: " + this.strCreationTime + NEWLINE;
		strOutput = strOutput + "Entry Genotypes Numbers: " + NEWLINE;
		for (Map.Entry<String, Integer> objEntry : this.mapEntryGenotypesNumbers.entrySet()) {
			strOutput = strOutput + "\t" + objEntry.getKey() + ": " + objEntry.getValue() + NEWLINE;
		}
		strOutput = strOutput + "Entry Estimates: " + NEWLINE;
		for (Map.Entry<String, Map<String, Double>> objEntry : this.mapEntryEstimates.entrySet()) {
			strOutput = strOutput + "\t" + objEntry.getKey() + ": " + objEntry.getValue() + NEWLINE;
		}
		strOutput = strOutput + "Entry Core Checks: " + NEWLINE;
		for (Map.Entry<String, Map<String, Double>> objEntry : this.mapEntryCoreChecks.entrySet()) {
			strOutput = strOutput + "\t" + objEntry.getKey() + ": " + objEntry.getValue() + NEWLINE;
		}
		strOutput = strOutput + "Entry Ranks: " + NEWLINE;
		for (Map.Entry<String, Map<String, Double>> objEntry : this.mapEntryRanks.entrySet()) {
			strOutput = strOutput + "\t" + objEntry.getKey() + ": " + objEntry.getValue() + NEWLINE;
		}
		strOutput = strOutput + "Entry Raw Counts: " + NEWLINE;
		for (Map.Entry<String, Integer> objEntry : this.mapEntryRawCounts.entrySet()) {
			strOutput = strOutput + "\t" + objEntry.getKey() + ": " + objEntry.getValue() + NEWLINE;
		}
		strOutput = strOutput + "exlSummary File Path: " + this.objExlSummaryFilePath.toString() + NEWLINE;
		strOutput = strOutput + "Exl Raw Means: " + NEWLINE;
		for (Map.Entry<String, Map<String, Double>> objEntry : this.mapExlRawMeans.entrySet()) {
			strOutput = strOutput + "\t" + objEntry.getKey() + ": " + objEntry.getValue() + NEWLINE;
		}
		strOutput = strOutput + "Exl Core Checks: " + NEWLINE;
		for (Map.Entry<String, Map<String, Double>> objEntry : this.mapExlCoreChecks.entrySet()) {
			strOutput = strOutput + "\t" + objEntry.getKey() + ": " + objEntry.getValue() + NEWLINE;
		}
		strOutput = strOutput + "Exl CAVs: " + NEWLINE;
		for (Map.Entry<String, Map<String, Double>> objEntry : this.mapExlCavs.entrySet()) {
			strOutput = strOutput + "\t" + objEntry.getKey() + ": " + objEntry.getValue() + NEWLINE;
		}
		strOutput = strOutput + "experimentSummary File Path: " + this.objExperimentSummaryFilePath.toString() + NEWLINE;
		strOutput = strOutput + "Experiment Locations Numbers: " + NEWLINE;
		for (Map.Entry<String, Integer> objEntry : this.mapExperimentLocationsNumbers.entrySet()) {
			strOutput = strOutput + "\t" + objEntry.getKey() + ": " + objEntry.getValue() + NEWLINE;
		}
		strOutput = strOutput + "locSummary File Path: " + this.objLocSummaryFilePath.toString() + NEWLINE;
		strOutput = strOutput + "Loc Estimates: " + NEWLINE;
		for (Map.Entry<String, Map<String, Double>> objEntry : this.mapLocEstimates.entrySet()) {
			strOutput = strOutput + "\t" + objEntry.getKey() + ": " + objEntry.getValue() + NEWLINE;
		}
		strOutput = strOutput + "Loc CVs: " + NEWLINE;
		for (Map.Entry<String, Map<String, Double>> objEntry : this.mapLocCvs.entrySet()) {
			strOutput = strOutput + "\t" + objEntry.getKey() + ": " + objEntry.getValue() + NEWLINE;
		}
		strOutput = strOutput + "Loc Check CVs: " + NEWLINE;
		for (Map.Entry<String, Map<String, Double>> objEntry : this.mapLocCheckCvs.entrySet()) {
			strOutput = strOutput + "\t" + objEntry.getKey() + ": " + objEntry.getValue() + NEWLINE;
		}
		strOutput = strOutput + "Loc Raw Core Check Means: " + NEWLINE;
		for (Map.Entry<String, Map<String, Double>> objEntry : this.mapLocRawCoreCheckMeans.entrySet()) {
			strOutput = strOutput + "\t" + objEntry.getKey() + ": " + objEntry.getValue() + NEWLINE;
		}
		
		strOutput = strOutput + NEWLINE;
				
		return strOutput;
	} 
}