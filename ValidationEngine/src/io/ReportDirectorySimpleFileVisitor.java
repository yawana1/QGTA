/*
 * 
 */
package io;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import data.Report;

public class ReportDirectorySimpleFileVisitor extends SimpleFileVisitor<Path> {
	static Logger objLogger = Logger.getLogger(ReportDirectorySimpleFileVisitor.class.getName());
	private int intTrialsCapacity;
	private int intTraitsCapacity;
	private String strSeasonName;
	private String strTrialName;
	private Path previousReportsDircetory; // this will be null unless a specific report is given as input
	private Path currentReportsDircetory; // this will be null unless a specific report is given as input
	private boolean bolARCHIVEFlag;
	private List<Report> lstCurrentReports;
	private List<Report> lstPreviousReports;
	private Set<String> setSummaryFileNames;
	private EntrySummaryReader objEntrySummaryReader;
	private ExlSummaryReader objExlSummaryReader;
	private ExperimentSummaryReader objExperimentSummaryReader;
	private LocSummaryReader objLocSummaryReader;
	
	public ReportDirectorySimpleFileVisitor(int intInTrialsCapacity,
											int intInTraitsCapacity,
			                                String strInSeasonName,
			                                String strInTrialName,
			                                Path pathInPreviousReportsDircetory,
			                                Path pathInCurrentReportsDircetory,
			                                boolean bolInARCHIVEFlag) throws Exception {
		this.intTrialsCapacity = intInTrialsCapacity;
		this.intTraitsCapacity = intInTraitsCapacity;
		this.strSeasonName = strInSeasonName;
		this.strTrialName = strInTrialName;
		this.previousReportsDircetory = pathInPreviousReportsDircetory;
		this.currentReportsDircetory = pathInCurrentReportsDircetory;		
		this.bolARCHIVEFlag = bolInARCHIVEFlag;
		this.lstCurrentReports = new ArrayList<Report>(this.intTrialsCapacity);
		this.lstPreviousReports = new ArrayList<Report>(this.intTrialsCapacity);
		this.setSummaryFileNames = new HashSet<String>(4);
		this.setSummaryFileNames.add("entrySummary.txt");
		this.setSummaryFileNames.add("exlSummary.txt");
		this.setSummaryFileNames.add("experimentSummary.txt");
		this.setSummaryFileNames.add("locSummary.txt");		
	}

	@Override public FileVisitResult preVisitDirectory(Path objInDirectoryPath,
			   										   BasicFileAttributes objInBasicFileAttributes)  throws IOException {
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Uses the amount of HashMap space as least as possible to store Report objects avoiding too much memory used  
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
		// If added by u755482 - now we will have the folder work inside archive
		// This will keep the code from looking into work folders
		if (!(objInDirectoryPath.getFileName().toString().contains("work"))) {
			if (this.bolARCHIVEFlag == true) {
				return FileVisitResult.CONTINUE;
			}
			else {
				// changed by U755482
				//if (!(objInDirectoryPath.getFileName().toString().startsWith("ARCHIVE_"))) {
				if (!(objInDirectoryPath.getFileName().toString().contains("ARCHIVE_"))) {
					return FileVisitResult.CONTINUE;
				}
				else {
					return FileVisitResult.SKIP_SUBTREE;
				}
			}
		}else{
			return FileVisitResult.SKIP_SUBTREE;
		}
		
	}
	
	@Override public FileVisitResult visitFile(Path objInFilePath,
                                               BasicFileAttributes objInBasicFileAttributes) throws IOException {
		String[] strArrayLineParts;
		String strFileName;
		
		strArrayLineParts = objInFilePath.toString().split("\\/");
		strFileName = strArrayLineParts[strArrayLineParts.length - 1];
		System.out.println(objInFilePath.toString());
		// it will enter the if only if the user wants to run validation for many trials at the time
		// In this case SeasonName is mandatory
		if(this.strTrialName.equals("")&&(!(this.strSeasonName.equals("")))){
			if ((objInFilePath.toString().contains("/" + this.strSeasonName + "/")) && 
				(this.setSummaryFileNames.contains(strFileName))) {
				this.checkReport(objInFilePath);
			}
		}
		else{
			System.out.println(objInFilePath.toString());
			// if we get here we are already in the right folder. No need for checking
			//if ((objInFilePath.toString().contains("/" + this.strSeasonName + "/")) && 
				//	(objInFilePath.toString().contains("/"+this.strTrialName+"/")) &&
			 //   (this.setSummaryFileNames.contains(strFileName))) {
			//	this.checkReport(objInFilePath);
			//}
			if (this.setSummaryFileNames.contains(strFileName)) {
				this.checkReport(objInFilePath);
			}
		}
		return FileVisitResult.CONTINUE;
	}
	
	private void checkReport(Path objInFilePath) {
		String[] strArrayLineParts;
		String strTrialName;
		Report objCurrentReport;
		int intReportIndex;
		String strTimeStamp;
		Report objPreviousReport;
		
		
		objLogger.info("Checking the file: " + objInFilePath.toString() + "...");
		strArrayLineParts = objInFilePath.toString().split("\\/");
		///////////////////////
		// Current run report - for the case where no specifics paths are given
		// or to deal with the case where the report is not in the archive
		// regardless if it is current or previous
		///////////////////////
		// Changed by U755482 to reflect the new structure
		//if (!(strArrayLineParts[strArrayLineParts.length - 2].startsWith("ARCHIVE_"))) {
		if (!(strArrayLineParts[strArrayLineParts.length - 3].startsWith("ARCHIVE_"))) {			
			// Changed by U755482 to reflect the new structure
			strTrialName = strArrayLineParts[strArrayLineParts.length - 3];
			//strTrialName = strArrayLineParts[strArrayLineParts.length - 2];
			
			if((this.currentReportsDircetory == null)||(objInFilePath.toString().contains(this.currentReportsDircetory.toString()))){
			
				objCurrentReport = new Report(strTrialName,
    					  				  objInFilePath.getParent());
				/////////////////////////////////////////////
				// It is not in the list of current reports    
				/////////////////////////////////////////////
				if (!(this.lstCurrentReports.contains(objCurrentReport))) {
					this.lstCurrentReports.add(objCurrentReport);
				}
				//////////////////////////////////////////////////////////
				// The current report is filled with (more) summary data  
				//////////////////////////////////////////////////////////
				this.fillSummaryData(this.lstCurrentReports,
			                     this.lstCurrentReports.indexOf(objCurrentReport),
						         objInFilePath);
			}else{
				// it is previous but not in archive
			
				objPreviousReport = new Report(strTrialName,
		  				  objInFilePath.getParent());
				/////////////////////////////////////////////
				// It is not in the list of Previous reports    
				/////////////////////////////////////////////
				if (!(this.lstPreviousReports.contains(objPreviousReport))) {
					this.lstPreviousReports.add(objPreviousReport);
				}
				//////////////////////////////////////////////////////////
				// The Previous report is filled with (more) summary data  
				//////////////////////////////////////////////////////////
				this.fillSummaryData(this.lstPreviousReports,
						this.lstPreviousReports.indexOf(objPreviousReport),
						objInFilePath);
				
			}
			
		}
		//////////////////////////////////////////////////////////////////////////////
		// Previous run report for the case where no paths are given as input
		// It must be time stamped since there can be more than one previous report;  
		// only the most recent previous report is chosen
		// *** it also deals with the case where paths are given as input and they are in the archive 
		//////////////////////////////////////////////////////////////////////////////
		else {
			// Changed by U755482 to reflect the new structure
			strTrialName = strArrayLineParts[strArrayLineParts.length - 5];
			//strTrialName = strArrayLineParts[strArrayLineParts.length - 3];
			
			if(this.previousReportsDircetory != null){
				// we will NOT need to check timestamp
				if(objInFilePath.toString().contains(this.currentReportsDircetory.toString())){
					objCurrentReport = new Report(strTrialName,
			  				  objInFilePath.getParent());
					/////////////////////////////////////////////	
					// It is not in the list of current reports    
					/////////////////////////////////////////////
					if (!(this.lstCurrentReports.contains(objCurrentReport))) {
						this.lstCurrentReports.add(objCurrentReport);
					}
					//////////////////////////////////////////////////////////
					// The current report is filled with (more) summary data  
					//////////////////////////////////////////////////////////
					this.fillSummaryData(this.lstCurrentReports,
					               this.lstCurrentReports.indexOf(objCurrentReport),
							         objInFilePath);
				}else{
				// it is previous in archive but with given paths
				
					objPreviousReport = new Report(strTrialName,
							  objInFilePath.getParent());
					/////////////////////////////////////////////
					// It is not in the list of Previous reports    
					/////////////////////////////////////////////
					if (!(this.lstPreviousReports.contains(objPreviousReport))) {
						this.lstPreviousReports.add(objPreviousReport);
					}
					//////////////////////////////////////////////////////////
					// The Previous report is filled with (more) summary data  
					//////////////////////////////////////////////////////////
					this.fillSummaryData(this.lstPreviousReports,
							this.lstPreviousReports.indexOf(objPreviousReport),
							objInFilePath);
				
				}
				
			}else{
				

				objPreviousReport = new Report(strTrialName,
						                       objInFilePath.getParent());
				//////////////////////////////////////////////
				// It is not in the list of previous reports    
				//////////////////////////////////////////////
				if (!(this.lstPreviousReports.contains(objPreviousReport))) {
					// Changed by U755482 to reflect the new structure
					objPreviousReport.setTimeStamp(this.getTimeStamp(objInFilePath,
	                        strArrayLineParts[strArrayLineParts.length - 3]));
					//objPreviousReport.setTimeStamp(this.getTimeStamp(objInFilePath,
					//		                                         strArrayLineParts[strArrayLineParts.length - 2]));
					this.lstPreviousReports.add(objPreviousReport);
					this.fillSummaryData(this.lstPreviousReports,
	                                     this.lstPreviousReports.indexOf(objPreviousReport),
	                                     objInFilePath);
				}
				//////////////////////////////////////////
				// It is in the list of previous reports      
				//////////////////////////////////////////
				else {
					intReportIndex = this.lstPreviousReports.indexOf(objPreviousReport);
					// Changed by U755482 to reflect the new structure
					strTimeStamp = this.getTimeStamp(objInFilePath,
	                                                 strArrayLineParts[strArrayLineParts.length - 3]);
					//strTimeStamp = this.getTimeStamp(objInFilePath,
	                 //       strArrayLineParts[strArrayLineParts.length - 2]);
					////////////////////////////////////////////////////////////////////////////////
					// The new summary data is coherent with the time stamp of the previous report  
					////////////////////////////////////////////////////////////////////////////////
					if (this.lstPreviousReports.get(intReportIndex).getTimeStamp().equals(strTimeStamp)) {
						objLogger.info("The report is updated with data from " + objInFilePath.toString());
						this.fillSummaryData(this.lstPreviousReports,
								             intReportIndex,
	                                         objInFilePath);
					}
					//////////////////////////////////////////////////////////////////////////////
					// The previous report is newer than the one in the list of previous reports    
					//////////////////////////////////////////////////////////////////////////////
					else {
						if (this.lstPreviousReports.get(intReportIndex).isOlderThan(strTimeStamp)) {
							objLogger.info(objInFilePath.toString() + " is accepted for being newer");
							this.lstPreviousReports.remove(intReportIndex);
							objPreviousReport = new Report(strTrialName,
									                       objInFilePath.getParent());
							// Changed by U755482 to reflect the new structure
							objPreviousReport.setTimeStamp(this.getTimeStamp(objInFilePath,
			                        strArrayLineParts[strArrayLineParts.length - 3]));
						
							//objPreviousReport.setTimeStamp(this.getTimeStamp(objInFilePath,
	                          //                                               strArrayLineParts[strArrayLineParts.length - 2]));
							this.lstPreviousReports.add(objPreviousReport);
							this.fillSummaryData(this.lstPreviousReports,
	                                             this.lstPreviousReports.indexOf(objPreviousReport),
	                                             objInFilePath);
						}
						else {
							objLogger.info(objInFilePath.toString() + " is rejected for being older");
						}
					}
				}
			}
		}
	}

	private String getTimeStamp(Path objInFilePath,
			                    String strInArchiveDirectoryName) {
		Report objVoidReport;
		String strTimeStamp;
		String[] strArrayLineParts;
		
		objVoidReport = new Report("",
				                   null);
		strTimeStamp = "";
		try {
			strArrayLineParts = strInArchiveDirectoryName.split("_");
			if (strArrayLineParts.length == 2) {
				strTimeStamp = strArrayLineParts[1];
				try {
					if (!(objVoidReport.checkTimeStampFormat(strTimeStamp))) {
						throw new Exception("The format of the timestamp given is incorrect when treating with " + 
								            objInFilePath.toString());
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					objLogger.error("ReportDirectorySimpleFileVisitor.getTimeStamp", 
					                e);
					throw new RuntimeException();
				}
			}
			else {
				throw new Exception("There are more than two parts from splitting " + objInFilePath.toString());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			objLogger.error("ReportDirectorySimpleFileVisitor.getTimeStamp", 
			                e);
			throw new RuntimeException();
		}
		
		return strTimeStamp;
	}
		
	private void fillSummaryData(List<Report> lstInReports,
			                     int intInReportIndex,
			                     Path objInFilePath) {
		String[] strArrayLineParts;
		String strFileName;
		
		objLogger.info("Getting the summary data from the file: " + objInFilePath + "...");
		strArrayLineParts = objInFilePath.toString().split("\\/");
		strFileName = strArrayLineParts[strArrayLineParts.length - 1];
		switch (strFileName) {
			/////////////////////
			// entrySummary.txt    
			/////////////////////				
			case "entrySummary.txt":
				try {
					if (lstInReports.get(intInReportIndex).getEntrySummaryFilePath() == null) {
						lstInReports.get(intInReportIndex).setEntrySummaryFilePath(objInFilePath);
						this.objEntrySummaryReader = new EntrySummaryReader(this.intTraitsCapacity,
								                                            objInFilePath);
						objLogger.info("Setting the entry creation time...");
						lstInReports.get(intInReportIndex).setCreationTime(this.objEntrySummaryReader.getCreationTime());
						objLogger.info("Setting the entry genotypes numbers...");
						lstInReports.get(intInReportIndex).setEntryGenotypesNumbers(this.objEntrySummaryReader.getGenotypesNumbers());
						objLogger.info("Setting the entry estimates...");
						lstInReports.get(intInReportIndex).setEntryEstimates(this.objEntrySummaryReader.getEstimates());
						objLogger.info("Setting the entry corechecks...");
						lstInReports.get(intInReportIndex).setEntryCoreChecks(this.objEntrySummaryReader.getCoreChecks());
						objLogger.info("Setting the entry ranks...");
						lstInReports.get(intInReportIndex).setEntryRanks(this.objEntrySummaryReader.getRanks());
						objLogger.info("Setting the entry raw counts...");
						lstInReports.get(intInReportIndex).setEntryRawCounts(this.objEntrySummaryReader.getRawCounts());
					}
					else {
						throw new Exception("A possible repetition of entrySummary detected between " +
								            lstInReports.get(intInReportIndex).getEntrySummaryFilePath().toString() + 
					                        " and " + objInFilePath.toString()); 
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					objLogger.error("ReportDirectorySimpleFileVisitor.FileVisitResult", 
			                        e);	
					throw new RuntimeException();
				}
				break;

			///////////////////
			// exlSummary.txt    
			///////////////////				
			case "exlSummary.txt":
				try {
					if (lstInReports.get(intInReportIndex).getExlSummaryFilePath() == null)  {
						lstInReports.get(intInReportIndex).setExlSummaryFilePath(objInFilePath);
						this.objExlSummaryReader = new ExlSummaryReader(this.intTraitsCapacity,
                                                                        objInFilePath);
						objLogger.info("Setting the exl raw means...");
						lstInReports.get(intInReportIndex).setExlRawMeans(this.objExlSummaryReader.getRawMeans());
						objLogger.info("Setting the exl core checks...");
						lstInReports.get(intInReportIndex).setExlCoreChecks(this.objExlSummaryReader.getCoreChecks());
						objLogger.info("Setting the exl CAVs...");
						lstInReports.get(intInReportIndex).setExlCavs(this.objExlSummaryReader.getCavs());
					}
					else {
						throw new Exception("A possible repetition of exlSummary detected between " +
								            lstInReports.get(intInReportIndex).getExlSummaryFilePath().toString() + 
								            " and " + objInFilePath.toString()); 
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					objLogger.error("ReportDirectorySimpleFileVisitor.FileVisitResult", 
			                        e);
					throw new RuntimeException();
				}
				break;

			//////////////////////////
			// experimentSummary.txt      
			//////////////////////////				
			case "experimentSummary.txt":
				try {
					if (lstInReports.get(intInReportIndex).getExperimentSummaryFilePath() == null)  {
						lstInReports.get(intInReportIndex).setExperimentSummaryFilePath(objInFilePath);
						this.objExperimentSummaryReader = new ExperimentSummaryReader(this.intTraitsCapacity,
                                                                                      objInFilePath);
						objLogger.info("Setting the experiment locations numbers...");
						lstInReports.get(intInReportIndex).setExperimentLocationsNumbers(this.objExperimentSummaryReader.getLocationsNumbers()); 
					}
					else {
						throw new Exception("A possible repetition of experimentSummary detected between " +
								            lstInReports.get(intInReportIndex).getExperimentSummaryFilePath().toString() + 
					                        " and " + objInFilePath.toString()); 
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					objLogger.error("ReportDirectorySimpleFileVisitor.FileVisitResult", 
	                        		e);
					throw new RuntimeException();
				}				
				break;

			///////////////////
			// locSummary.txt  
			///////////////////				
			case "locSummary.txt":
				try {
					if (lstInReports.get(intInReportIndex).getLocSummaryFilePath() == null)  {
						lstInReports.get(intInReportIndex).setLocSummaryFilePath(objInFilePath);
						this.objLocSummaryReader = new LocSummaryReader(this.intTraitsCapacity,
                                                                        objInFilePath);
						objLogger.info("Setting the loc estimates...");
						lstInReports.get(intInReportIndex).setLocEstimates(this.objLocSummaryReader.getEstimates());
						objLogger.info("Setting the loc CVs...");
						lstInReports.get(intInReportIndex).setLocCvs(this.objLocSummaryReader.getCvs());
						objLogger.info("Setting the loc Check CVs...");
						lstInReports.get(intInReportIndex).setLocCheckCvs(this.objLocSummaryReader.getCheckCvs());
						objLogger.info("Setting the loc Raw Core Check Means...");
						lstInReports.get(intInReportIndex).setLocRawCoreCheckMeans(this.objLocSummaryReader.getRawCoreCheckMeans());
					}
					else {
						throw new Exception("A possible repetition of locSummary detected between " +
								            lstInReports.get(intInReportIndex).getLocSummaryFilePath().toString() + 
					                        " and " + objInFilePath.toString()); 
					}
				}	
				catch (Exception e) {
					e.printStackTrace();
					objLogger.error("ReportDirectorySimpleFileVisitor.FileVisitResult", 
	                       		    e);	
					throw new RuntimeException();
				}				
				break;
		}
	}
	
	public List<Report> getCurrentReports() {
		return this.lstCurrentReports;
	}

	public List<Report> getPreviousReports() {
		return this.lstPreviousReports;
	}
}
