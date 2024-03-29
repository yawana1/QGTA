/*
 * 
 */
package validation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.log4j.Logger;

import asreml.output.AsrVariance;
import data.Model;
import data.Report;
import data.Trait;
import data.Trial;
import data.xml.objects.App;
//import java.util.Arrays;
import highLevelSummary.ReportReader;
import io.ReportDirectorySimpleFileVisitor;
import io.ReportWriter;
import io.ReportWriterPerTrait;
import io.WorkDirectorySimpleFileVisitor;
import io.XMLFileParser;

public class Validator {
	
	static Logger objLogger = Logger.getLogger(Validator.class.getName());
	private int intTrialsCapacity;
	private int intTraitsCapacity;	
	private String strSeasonName;
	private String strTrialName;
	private String strCrop;
	private String strProjectName;
	private boolean forceMatch;
	private Path objPreviousReportsDirectoryPath;
	private Path objCurrentReportsDirectoryPath;
	private List<Trial> lstTrials;
	private List<Report> lstCurrentReports;
	private List<Report> lstPreviousReports;
	private Map<String, Integer> objMapCurrentReportIndexes;
	private Map<String, Integer> objMapPreviousReportIndexes;	
	private TreeMap<String, List<Trial>> objTreeMapTrialsByTrait;
	private ReportWriter objReportWriter;
	private ReportWriterPerTrait objReportWriterPerTrait;
	private DateFormat objDataFormat;
	private String classType;
	private Path objOutputReportPath;
	private String objOutputFormat;
	
	public Validator(String strInSeasonName,
			         Path objInPreviousReportsDirectoryPath,Path objInCurrentReportsDirectoryPath, 
			         String classType,String strInTrialName, String strInProjectName, String strInCrop, 
			         boolean inForceMatch, Path objInOutputReportPath, String objInOutputFormat) {
		this.intTrialsCapacity = 4000;
		this.intTraitsCapacity = 30;
		this.objPreviousReportsDirectoryPath = objInPreviousReportsDirectoryPath;
		this.objCurrentReportsDirectoryPath = objInCurrentReportsDirectoryPath;
		this.objOutputReportPath = objInOutputReportPath;
		this.objOutputFormat =  objInOutputFormat;
		
		if(this.objCurrentReportsDirectoryPath==null){
		
			this.strSeasonName = strInSeasonName;
			this.strTrialName = strInTrialName;
			this.strProjectName = strInProjectName;
			this.classType = classType;
			this.strCrop = strInCrop;
			this.forceMatch = false; // forceMatch will never be allowed in this scenario
			// we will have a first check in main.java though so that we never get to this point
			// where we change the value to guarantee the correctness of the run.
			// we will just not run.
		}else{ // if we have current report we do not need all this information here
			this.strSeasonName = "";
			this.strTrialName = "";
			this.strProjectName = "";
			this.classType = "";
			this.strCrop = "";
			this.forceMatch = inForceMatch;
	
			
		}
	}
	
	public void run() {
		try {
			
			Path baseDir;
			
			if(this.objCurrentReportsDirectoryPath == null){
			
				baseDir = Paths.get(App.INSTANCE.getWorkDirectory());
				//baseDir = baseDir.resolve("corn/");
				baseDir = baseDir.resolve(this.strCrop+"/"+this.classType+"/");
				// season name is required. we do not check if it is given 
				baseDir = baseDir.resolve(this.strSeasonName+"/");
				if(!(this.strTrialName.equals(""))){
					baseDir = baseDir.resolve(this.strProjectName+"/"+this.strTrialName+"/");
				}
			}else{
				
				baseDir = this.objCurrentReportsDirectoryPath.getParent();
				baseDir = baseDir.resolve("work/");
				// This means that we are not using the default work directory and it might be one in the archive
			}
			System.out.println(baseDir.toString());
			this.coverWorkDirectory(baseDir);

			if(this.objCurrentReportsDirectoryPath == null){
			
			
				baseDir = Paths.get(App.INSTANCE.getReportDirectory());
				baseDir = baseDir.resolve(this.strCrop+"/"+this.classType+"/");
				// season name is required. we do not check if it is given 
				baseDir = baseDir.resolve(this.strSeasonName+"/");
				if(!(this.strTrialName.equals(""))){
					baseDir = baseDir.resolve(this.strProjectName+"/"+this.strTrialName+"/");
					// this will be used for both current and previous report
					// so we cannot go any more deeper than TrialName
				}
			}else{
				
				baseDir = this.objCurrentReportsDirectoryPath;
			}
			//System.out.println(baseDir.toString());
			this.coverReportDirectory(baseDir);
			
			this.connectReportWithTrial();
			this.getTrialsByTrait();
			String strInFileName = this.processTraits();
			if(this.objOutputFormat.equals("PerRequest")){
				this.createHighLevelSummary(strInFileName);
			}
		}
		catch (IOException e) {
			objLogger.error("Validator.run", 
                            e);
		}
	}

	private void coverWorkDirectory(Path baseDir) throws IOException {
		WorkDirectorySimpleFileVisitor objWorkDirectorySimpleFileVisitor;
		
		objLogger.info("Processing the work directory...");
		objWorkDirectorySimpleFileVisitor = null;
		try {
			objWorkDirectorySimpleFileVisitor = new WorkDirectorySimpleFileVisitor(this.intTrialsCapacity,
					                                                               this.strSeasonName,
					                                                               this.strTrialName);
		}
		catch (Exception e) {
			objLogger.error("Validator.coverWorkDirectory", 
                            e);
		}
		Files.walkFileTree(baseDir, 
				           objWorkDirectorySimpleFileVisitor);
		System.out.print("walked");
		this.lstTrials = objWorkDirectorySimpleFileVisitor.getTrials();
		this.getAsrData();
	}

	private void getAsrData() {
		int intTrialIndex;
		int intModelIndex;
		int intTraitIndex;
		Path objAsrFilePath;
		asreml.AsremlTrait objAsremlTrait;
		List<asreml.AsremlTrait> lstArsemlTraits;
		asreml.output.AsrData objAsrData;
		Map<String, AsrVariance> objMapAsrVariances;
		boolean bolBoundedVCs;
		
		objLogger.info("Processing the capture of the Asr data...");
		for (Trial objTrial : this.lstTrials) {
			intTrialIndex = this.lstTrials.indexOf(objTrial);
			for (Model objModel : objTrial.getModels()) {
				intModelIndex = this.lstTrials.get(intTrialIndex).getModels().indexOf(objModel);
				for (Trait objTrait : objModel.getTraits()) {
					intTraitIndex = this.lstTrials.get(intTrialIndex).getModels().get(intModelIndex).getTraits().indexOf(objTrait);
					///////////////////////////////////////////////////////////////////////////////////////////////
					// The AsrData class in the Trial Analysis project adds the extension ".asr" to any filename,   
					// even having the same extension!
					///////////////////////////////////////////////////////////////////////////////////////////////					
					objAsrFilePath = FileSystems.getDefault().getPath(objTrait.getPath().toString(), "asreml");
					objAsremlTrait = new asreml.AsremlTrait(objTrait.getName(), 
							                          0);
					lstArsemlTraits = new ArrayList<asreml.AsremlTrait>();
                    lstArsemlTraits.add(objAsremlTrait);
                    objLogger.info("Reading the Asr data in " + objAsrFilePath.toString() + ".asr...");
                    objAsrData = new asreml.output.AsrData(objAsrFilePath,
					                                       0,
					                                       0,
					                                       0,
					                                       lstArsemlTraits,
											               0.0);
					this.lstTrials.get(intTrialIndex).getModels().get(intModelIndex).getTraits().get(intTraitIndex).setConverged(objAsrData.isConverged());
					objMapAsrVariances = objAsrData.getVariance(objAsremlTrait);
					if (objMapAsrVariances.get("Variance") != null) {
						bolBoundedVCs = this.getBoundedC(objMapAsrVariances.get("Variance").getC());
					}
					else {
						bolBoundedVCs = false;
					}
					this.lstTrials.get(intTrialIndex).getModels().get(intModelIndex).getTraits().get(intTraitIndex).setBoundedVCs(bolBoundedVCs);
				}
			}
		}
	}
	
	private void coverReportDirectory(Path baseDir) throws IOException,
    										   FileNotFoundException {
		boolean bolARCHIVEFlag;
		ReportDirectorySimpleFileVisitor objReportDirectorySimpleFileVisitor;

		objLogger.info("Processing the report directory(ies)...");
		objReportDirectorySimpleFileVisitor = null;
		// ////////////////////////////////////////////////////////////
		// If the ARCHIVE subdirectories are preferred to be targeted  
		///////////////////////////////////////////////////////////////
		if (this.objPreviousReportsDirectoryPath == null) {
			bolARCHIVEFlag = true;
			try {
				objReportDirectorySimpleFileVisitor = new ReportDirectorySimpleFileVisitor(this.intTrialsCapacity,
						                                                                   this.intTraitsCapacity,
                                                                                           this.strSeasonName,
                                                                                           this.strTrialName,
                                                                                           null, //previousReportsDircetoryPath
                                                                                           null, //currentReportsDircetoryPath
						                                                                   bolARCHIVEFlag);
			}
			catch (Exception e) {
				objLogger.error("Validator.coverReportDirectory", 
								e);
			}
			Files.walkFileTree(baseDir, 
				           	   objReportDirectorySimpleFileVisitor);
			this.lstCurrentReports = objReportDirectorySimpleFileVisitor.getCurrentReports();
			this.lstPreviousReports = objReportDirectorySimpleFileVisitor.getPreviousReports();
		}
		////////////////////////////////////////////////////////////
		// If a previous reports directory is given to be targeted  
        ////////////////////////////////////////////////////////////
		else {
			//changed by u755482
			// we will now allow to look into archive for the case where we have a previous report specified
			//bolARCHIVEFlag = false;
			bolARCHIVEFlag = true;
			try {
				objReportDirectorySimpleFileVisitor = new ReportDirectorySimpleFileVisitor(this.intTrialsCapacity,
						                                                                   this.intTraitsCapacity,
                                                                                           this.strSeasonName,
                                                                                           this.strTrialName,
                                                                                           this.objPreviousReportsDirectoryPath,
                                                                                           this.objCurrentReportsDirectoryPath,
						                                                                   bolARCHIVEFlag);
			}
			catch (Exception e) {
				objLogger.error("Validator.coverReportDirectory", 
								e);
			}
			// baseDir has the same value of this.objCurrentReportsDirectoryPath
			Files.walkFileTree(baseDir, 
	           	   		       objReportDirectorySimpleFileVisitor);
			this.lstCurrentReports = objReportDirectorySimpleFileVisitor.getCurrentReports();
			//////////////////////////////////////////////////////////
			// Reassigning objReportDirectorySimpleFileVisitor again  
			// to avoid having two big, big objects at the same time  
			////////////////////////////////////////////////////////// 
			try {
				objReportDirectorySimpleFileVisitor = new ReportDirectorySimpleFileVisitor(this.intTrialsCapacity,
                                                                                           this.intTraitsCapacity,
                                                                                           this.strSeasonName,
                                                                                           this.strTrialName,
                                                                                           this.objPreviousReportsDirectoryPath,
                                                                                           this.objCurrentReportsDirectoryPath,
						                                                                   bolARCHIVEFlag);
			}
			catch (Exception e) {
				objLogger.error("Validator.coverReportDirectory", 
								e);
			}
			Files.walkFileTree(this.objPreviousReportsDirectoryPath,
                        	   objReportDirectorySimpleFileVisitor);
			this.lstPreviousReports = objReportDirectorySimpleFileVisitor.getPreviousReports();		
		}
	}
	
	private void connectReportWithTrial() {
		int intListIndex;
		
		objLogger.info("Connecting reports with trials...");
		////////////////////////////////////////////////////////////////////////////////////////
		// "Connects" two objects from different classes, "Report" and "Trial", by trial name.  
		////////////////////////////////////////////////////////////////////////////////////////
		this.objMapCurrentReportIndexes = new HashMap<String, Integer>(this.intTrialsCapacity);
		for (intListIndex = 0;
			 intListIndex < this.lstCurrentReports.size();
			 intListIndex = intListIndex + 1) {
			if (!(this.objMapCurrentReportIndexes.containsKey(this.lstCurrentReports.get(intListIndex).getTrialName()))) {
				this.objMapCurrentReportIndexes.put(this.lstCurrentReports.get(intListIndex).getTrialName(), 
						                            intListIndex);
			}
			else {
				objLogger.error("The current report " +  this.lstCurrentReports.get(intListIndex).getTrialName() + 
				                " is repeated");
		        throw new RuntimeException("The current report " +  this.lstCurrentReports.get(intListIndex).getTrialName() + 
		                                   " is repeated");
			}
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		// If there is a previous report with the same trial name and relative path with the current report,  
		// then it is accepted to save HashMap space.                                                                               
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		this.objMapPreviousReportIndexes = new HashMap<String, Integer>(this.intTrialsCapacity);		
		for (intListIndex = 0;
			 intListIndex < this.lstPreviousReports.size();
			 intListIndex = intListIndex + 1) {
			
			// mapping current and previous
			
			if(this.forceMatch){
				
				// Forcing match
				// we do not check if the trial in previous is present in current
				// we will just match them. This will be needed when comparing experiments with different trial names
				// such as giant analysis with single analysis
				//if ((this.lstCurrentReports.indexOf(this.lstPreviousReports.get(intListIndex))) != -1) {
					if (!(this.objMapPreviousReportIndexes.containsKey(this.lstPreviousReports.get(intListIndex).getTrialName()))) {
						this.objMapPreviousReportIndexes.put(this.lstPreviousReports.get(intListIndex).getTrialName(), 
								                         	 intListIndex);
					}
					else {
						objLogger.error("The previous report " + this.lstPreviousReports.get(intListIndex).getTrialName() + 
					                	" is repeated");
						throw new RuntimeException("The previous report " + this.lstPreviousReports.get(intListIndex).getTrialName() + 
				                               	   " is repeated");
					}
				//}
			}else{
				// Do not force match
				if ((this.lstCurrentReports.indexOf(this.lstPreviousReports.get(intListIndex))) != -1) {
					if (!(this.objMapPreviousReportIndexes.containsKey(this.lstPreviousReports.get(intListIndex).getTrialName()))) {
						this.objMapPreviousReportIndexes.put(this.lstPreviousReports.get(intListIndex).getTrialName(), 
								                         	 intListIndex);
					}
					else {
						objLogger.error("The previous report " + this.lstPreviousReports.get(intListIndex).getTrialName() + 
					                	" is repeated");
						throw new RuntimeException("The previous report " + this.lstPreviousReports.get(intListIndex).getTrialName() + 
				                               	   " is repeated");
					}
				}
				
				
			}
			
			
		}
	}
	
	private void getTrialsByTrait() {
		List<Trial> lstTrials;
		
		objLogger.info("Processing the inverse relationship of trials by trait...");
		this.objTreeMapTrialsByTrait = new TreeMap<String, List<Trial>>();
		for (Trial objTrial : this.lstTrials) {
			for (Model objModel : objTrial.getModels()) {
				for (Trait objTrait : objModel.getTraits()) { 
					if (!(this.objTreeMapTrialsByTrait.containsKey(objTrait.getName()))) {
						lstTrials = new ArrayList<Trial>(this.intTrialsCapacity);
						lstTrials.add(objTrial);
						this.objTreeMapTrialsByTrait.put(objTrait.getName(), 
								                         lstTrials);
					}
					else {
						if (!(this.objTreeMapTrialsByTrait.get(objTrait.getName()).contains(objTrial))) {
							this.objTreeMapTrialsByTrait.get(objTrait.getName()).add(objTrial);
						}
					}
				}
			}
		}
		try {
			if (this.objTreeMapTrialsByTrait.isEmpty()) {
				objLogger.error("No useful trial data for the traits");
				throw new Exception("No useful trial data for the traits");
			}
		}
		catch (Exception e) {
			objLogger.error("Validator.getTrialsByTrait", 
							e);
			throw new RuntimeException();
		}
	}
	
	private String processTraits() {
		String[] strArrayHeaders;
		Iterator<Map.Entry<String, List<Trial>>> iteTrialsByTrait;
		Map.Entry<String, List<Trial>> objMapEntry;
		String strTraitName;
		int intPreviousReportIndex;
		int intCurrentReportIndex;
		Object[] objArrayRowData;
		String strCropName;
		String strTypeName;
		String strStageName;
		String strSeasonName;
		String strFirstModelName;
		String strSecondModelName;
		String strThirdModelName;
		String strLastModelToConverge = "";
		Path objPathXMLDirectory;
		DirectoryStream<Path> objDirectoryStream;
		int intXMLFilesNumber;
		Path objPathXMLFile;
		XMLFileParser objXMLFileParser;
		int intPreviousLocationsNumber;
		int intCurrentLocationsNumber;
		int intPreviousGenotypesNumber;
		int intCurrentGenotypessNumber;		
		int intTrialIndex;
		int intFirstModelIndex;
		int intSecondModelIndex;
		int intThirdModelIndex;
		double douEntryCorrelationEstimate = 0;
		double douEntryCorrelationCoreCheck = 0;	
		double douEntryCorrelationRank = 0;	
		double douExlCorrelationRawMean = 0;
		double douExlCorrelationCoreCheck = 0;	
		double douExlCorrelationCav = 0;	
		double douLocCorrelationEstimate = 0;	
		double douLocCorrelationCv = 0;	
		double douLocCorrelationCheckCv = 0;	
		double douLocCorrelationRawCoreCheckMean = 0;
		int locationDeviation = 0;
		String outputFileName;
		String outputFileNameIssues;
		
		
		

		objLogger.info("Processing the traits ...");
		strArrayHeaders = new String[33];
		strArrayHeaders[ 0] = "Crop";
		strArrayHeaders[ 1] = "Type";
		strArrayHeaders[ 2] = "Stage";
		strArrayHeaders[ 3] = "Country";
		strArrayHeaders[ 4] = "Yield";
		strArrayHeaders[ 5] = "Season";
		strArrayHeaders[ 6] = "Trial";
		strArrayHeaders[ 7] = "Locations";
		strArrayHeaders[ 8] = "Locations Deviation";
		strArrayHeaders[ 9]	= "Genotypes"; 	
		strArrayHeaders[10] = "Genotypes Deviation";
		strArrayHeaders[11] = "Total rawCounts";
		strArrayHeaders[12] = "First Model";		
		strArrayHeaders[13] = "FM Converged";
		strArrayHeaders[14] = "Bounded VCs";
		strArrayHeaders[15] = "Second Model";		
		strArrayHeaders[16] = "SM Converged";
		strArrayHeaders[17] = "Third Model";		
		strArrayHeaders[18] = "TM Converged";
		strArrayHeaders[19] = "Last model to converge";
		strArrayHeaders[20] = "Correlation Of estimate (entrySummary)";
		strArrayHeaders[21] = "Correlation Of coreCheck (entrySummary)";	
		strArrayHeaders[22] = "Correlation Of rank (entrySummary)";	
		strArrayHeaders[23] = "Correlation Of rawMean (exlSummary)";
		strArrayHeaders[24] = "Correlation Of coreCheck (exlSummary)";	
		strArrayHeaders[25] = "Correlation Of CAV (exlSummary)";	
		strArrayHeaders[26] = "Correlation Of estimate (locSummary)";	
		strArrayHeaders[27] = "Correlation Of cv (locSummary)";	
		strArrayHeaders[28] = "Correlation Of checkCV (locSummary)";	
		strArrayHeaders[29] = "Correlation Of rawCoreCheckMean (locSummary)";	
		strArrayHeaders[30] = "Creation Time";
		strArrayHeaders[31] = "Current Report";
		strArrayHeaders[32] = "Previous Report";
		
		if(this.objOutputFormat.equals("PerRequest")){
			this.objReportWriter = new ReportWriter();
		}
		this.objDataFormat = new SimpleDateFormat("MM_dd_yyyy");
		iteTrialsByTrait = this.objTreeMapTrialsByTrait.entrySet().iterator();
		while (iteTrialsByTrait.hasNext()) {
		
			boolean writeIssues = false;
			
			objMapEntry = (Map.Entry<String, List<Trial>>)iteTrialsByTrait.next();
			strTraitName = objMapEntry.getKey();
			// Pathology traits have the prefix pathology_
			// we want to get rid of that when searching for the values
			strTraitName = strTraitName.replace("pathology_", "");
					
			objLogger.info("Generating trial data for the trait: " + strTraitName);
			
			if(this.objOutputFormat.equals("PerRequest")){

				this.objReportWriter.createSheet(strTraitName,
					                         strArrayHeaders);
			}
			
			
			
			for (Trial objTrial : objMapEntry.getValue()) {
				objArrayRowData = new Object[strArrayHeaders.length];
				objPathXMLDirectory = objTrial.getPath();
				objPathXMLFile = null;
				objDirectoryStream = null;
				try {
					objDirectoryStream = Files.newDirectoryStream(objPathXMLDirectory);
					intXMLFilesNumber = 0;
					for (Path objPath: objDirectoryStream) {
						if (objPath.getFileName().toString().toLowerCase().endsWith(".xml")) {
							objPathXMLFile = objPath; 
							intXMLFilesNumber = intXMLFilesNumber + 1;
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					objLogger.error("Validator.processTraits", 
									e);
					throw new RuntimeException();
				}
				finally {
					if (objDirectoryStream != null)
					{
						try {
							objDirectoryStream.close();
						}
						catch (Exception e) {
					        e.printStackTrace();
					        objLogger.error("Validator.processTraits", 
							                e);
					        throw new RuntimeException();
						}
					}
				}
				if (intXMLFilesNumber == 1) {
					objXMLFileParser = new XMLFileParser(objPathXMLFile);
					strCropName = objXMLFileParser.getCropName();
					strTypeName = objXMLFileParser.getClassName();
					strStageName = objXMLFileParser.getStageName();
					strSeasonName = objXMLFileParser.getSeasonName();
					strFirstModelName = objXMLFileParser.getFirstModelName();
					strSecondModelName = objXMLFileParser.getSecondModelName();
					strThirdModelName = objXMLFileParser.getThirdModelName();
				}
				else {
					strCropName = "";
					strTypeName = "";
					strStageName = "";
					strSeasonName = "";
					strFirstModelName = "";
					strSecondModelName = "";
					strThirdModelName = "";
				}
				
				////////////////////////////
				// The column "Crop"  
				////////////////////////////
				objArrayRowData[ 0] = strCropName;
				
				////////////////////////////
				// The column "Type"  
				////////////////////////////
				objArrayRowData[ 1] = strTypeName;
				
				////////////////////////////
				// The column "Stage"  
				////////////////////////////
				objArrayRowData[ 2] = strStageName;
				
				////////////////////////////
				// The column "Country"  
				////////////////////////////
				objArrayRowData[ 3] = "";

				////////////////////////////
				// The column "Yield"  
				////////////////////////////
				objArrayRowData[ 4] = "";
				
				/////////////////////////////
				// The column "Season"  
				/////////////////////////////
				objArrayRowData[ 5] = strSeasonName;
				
				////////////////////////////
				// The column "Trial"  
				////////////////////////////
				objArrayRowData[ 6] = objTrial.getTrialName();
				
				if (this.objMapCurrentReportIndexes.containsKey(objTrial.getTrialName())) {
					intCurrentReportIndex = this.objMapCurrentReportIndexes.get(objTrial.getTrialName());
				}
				else {
					intCurrentReportIndex = -1;
				}
				
				if(this.forceMatch){
				// if we force match, we will have to look for different trial names
				// so we need to look for the trial name in the previous directory
					String trialNPrevious;
					String[] strArrayLineParts;
					
					if(this.objPreviousReportsDirectoryPath == null){
						trialNPrevious = this.strTrialName;
					}else{
						strArrayLineParts = this.objPreviousReportsDirectoryPath.toString().split("\\/");
						if (!(strArrayLineParts[strArrayLineParts.length - 2].startsWith("ARCHIVE_"))) {			
							trialNPrevious = strArrayLineParts[strArrayLineParts.length - 2];
						}else{
							trialNPrevious = strArrayLineParts[strArrayLineParts.length - 4];
						}
					}
				
					if (this.objMapPreviousReportIndexes.containsKey(trialNPrevious)) {
						intPreviousReportIndex = this.objMapPreviousReportIndexes.get(trialNPrevious);
						
						if(!(objArrayRowData[6].equals(trialNPrevious))){
							objArrayRowData[6] = objArrayRowData[6] + "_vs_" + trialNPrevious;
						}
						
					}
					else {
						intPreviousReportIndex = -1;
					}
					
				}else{
					
					if (this.objMapPreviousReportIndexes.containsKey(objTrial.getTrialName())) {
						intPreviousReportIndex = this.objMapPreviousReportIndexes.get(objTrial.getTrialName());
					}
					else {
						intPreviousReportIndex = -1;
					}
					
				}
				
				intCurrentLocationsNumber = 0;
				if (intCurrentReportIndex != -1) {
					intCurrentGenotypessNumber = 0;
					if (this.lstCurrentReports.get(intCurrentReportIndex).getExperimentLocationsNumbers() != null) {
						if (this.lstCurrentReports.get(intCurrentReportIndex).getExperimentLocationsNumbers().containsKey(strTraitName)) {
							intCurrentLocationsNumber = this.lstCurrentReports.get(intCurrentReportIndex).getExperimentLocationsNumbers().get(strTraitName); 
							///////////////////////////
							// The column "Locations"    
							///////////////////////////
							objArrayRowData[ 7] = intCurrentLocationsNumber;
						}
					}
					if (this.lstCurrentReports.get(intCurrentReportIndex).getEntryGenotypesNumbers() != null) {
						if (this.lstCurrentReports.get(intCurrentReportIndex).getEntryGenotypesNumbers().containsKey(strTraitName)) {
							intCurrentGenotypessNumber = this.lstCurrentReports.get(intCurrentReportIndex).getEntryGenotypesNumbers().get(strTraitName);
							///////////////////////////
							// The column "Genotypes"    
							///////////////////////////
							objArrayRowData[ 9] = intCurrentGenotypessNumber;
						}
					}
					if (intPreviousReportIndex != -1) {
						if (this.lstPreviousReports.get(intPreviousReportIndex).getExperimentLocationsNumbers() != null) {
							if (this.lstPreviousReports.get(intPreviousReportIndex).getExperimentLocationsNumbers().containsKey(strTraitName)) {
								intPreviousLocationsNumber = this.lstPreviousReports.get(intPreviousReportIndex).getExperimentLocationsNumbers().get(strTraitName);
								/////////////////////////////////////  
								// The column "Locations Deviation"   
								/////////////////////////////////////
								locationDeviation = intCurrentLocationsNumber - intPreviousLocationsNumber;
								objArrayRowData[ 8] = locationDeviation;
							}
						}
						if (this.lstPreviousReports.get(intPreviousReportIndex).getEntryGenotypesNumbers() != null) {
							if (this.lstPreviousReports.get(intPreviousReportIndex).getEntryGenotypesNumbers().containsKey(strTraitName)) {
								intPreviousGenotypesNumber = this.lstPreviousReports.get(intPreviousReportIndex).getEntryGenotypesNumbers().get(strTraitName);
								/////////////////////////////////////  
								// The column "Genotypes Deviation"    
								/////////////////////////////////////
								objArrayRowData[10] = intCurrentGenotypessNumber - intPreviousGenotypesNumber;
							}
						}
					}
					if (this.lstCurrentReports.get(intCurrentReportIndex).getEntryRawCounts() != null) {
						if (this.lstCurrentReports.get(intCurrentReportIndex).getEntryRawCounts().containsKey(strTraitName)) {
							/////////////////////////////////
							// The column "Total rawCounts"  
							/////////////////////////////////
							objArrayRowData[11] = this.lstCurrentReports.get(intCurrentReportIndex).getEntryRawCounts().get(strTraitName);
						}
					}
				}
			
				intTrialIndex = this.lstTrials.indexOf(objTrial);

				///////////////////////////////////////
				// The First, Second, and Third Model Indexes  
				///////////////////////////////////////
				intFirstModelIndex = -1;
				intSecondModelIndex = -1;
				intThirdModelIndex = -1;
				for (Model objModel : this.lstTrials.get(intTrialIndex).getModels()) {
					if ((!(strFirstModelName.equals(""))) &&
						(objModel.getName().equals(strFirstModelName))) {
						intFirstModelIndex = this.lstTrials.get(intTrialIndex).getModels().indexOf(objModel);
					}
					if ((!(strSecondModelName.equals(""))) &&
						(objModel.getName().equals(strSecondModelName))) {
						intSecondModelIndex = this.lstTrials.get(intTrialIndex).getModels().indexOf(objModel);
					}
					if ((!(strThirdModelName.equals(""))) &&
							(objModel.getName().equals(strThirdModelName))) {
							intThirdModelIndex = this.lstTrials.get(intTrialIndex).getModels().indexOf(objModel);
					}
				}

				////////////////////
				// The First Model   
				////////////////////
				if (intFirstModelIndex != -1) {
					for (Trait objTrait : this.lstTrials.get(intTrialIndex).getModels().get(intFirstModelIndex).getTraits()) {
						if (objTrait.getName().equals(objMapEntry.getKey())) {
							/////////////////////////////
							// The column "First Model"    
							/////////////////////////////
							objArrayRowData[12] = this.lstTrials.get(intTrialIndex).getModels().get(intFirstModelIndex).getName();
							//////////////////////////////
							// The column "FM Converged"    
							//////////////////////////////
							objArrayRowData[13] = this.translateBooleanValue(objTrait.getConverged());
							
							if(objArrayRowData[13]=="Yes"){
								strLastModelToConverge = this.lstTrials.get(intTrialIndex).getModels().get(intFirstModelIndex).getName();
							}
							
							/////////////////////////////
							// The column "Bounded VCs"    
							/////////////////////////////
							objArrayRowData[14] = this.translateBooleanValue(objTrait.getBoundedVCs());
							break;
						}
					}
				}

				/////////////////////
				// The Second Model      
				/////////////////////
				if (intSecondModelIndex != -1) {
					for (Trait objTrait : this.lstTrials.get(intTrialIndex).getModels().get(intSecondModelIndex).getTraits()) {
						if (objTrait.getName().equals(objMapEntry.getKey())) {
							//////////////////////////////
							// The column "Second Model"    
							//////////////////////////////
							objArrayRowData[15] = this.lstTrials.get(intTrialIndex).getModels().get(intSecondModelIndex).getName();
							//////////////////////////////
							// The column "SM Converged"    
							//////////////////////////////
							objArrayRowData[16] = this.translateBooleanValue(objTrait.getConverged());
							
							if(objArrayRowData[16]=="Yes"){
								strLastModelToConverge = this.lstTrials.get(intTrialIndex).getModels().get(intSecondModelIndex).getName();
							}
							
							
							break;
						}
					}
				}
				
				
				/////////////////////
				// The Third Model      
				/////////////////////
				if (intThirdModelIndex != -1) {
					for (Trait objTrait : this.lstTrials.get(intTrialIndex).getModels().get(intThirdModelIndex).getTraits()) {
						if (objTrait.getName().equals(objMapEntry.getKey())) {
							//////////////////////////////
							// The column "Third Model"    
							//////////////////////////////
							objArrayRowData[17] = this.lstTrials.get(intTrialIndex).getModels().get(intThirdModelIndex).getName();
							//////////////////////////////
							// The column "TM Converged"    
							//////////////////////////////
							objArrayRowData[18] = this.translateBooleanValue(objTrait.getConverged());
							
							if(objArrayRowData[18]=="Yes"){
								strLastModelToConverge = this.lstTrials.get(intTrialIndex).getModels().get(intThirdModelIndex).getName();
							}
							
							break;
						}
					}
				}
				
				
				objArrayRowData[19] = strLastModelToConverge;
				
				
				if ((intCurrentReportIndex != -1) && 
					(intPreviousReportIndex != -1)) {
					////////////////////////////////////////////////////////
					// The column "Correlation Of estimate (entrySummary)"   
					////////////////////////////////////////////////////////
					if ((this.lstCurrentReports.get(intCurrentReportIndex).getEntryEstimates() != null) && 
                        (this.lstPreviousReports.get(intPreviousReportIndex).getEntryEstimates() != null)) {
						douEntryCorrelationEstimate = this.getDoublePearsonProductMomentCorrelationCoefficient(this.lstCurrentReports.get(intCurrentReportIndex).getEntryEstimates().get(strTraitName), 
							                                                                                   this.lstPreviousReports.get(intPreviousReportIndex).getEntryEstimates().get(strTraitName));
						objArrayRowData[20] = douEntryCorrelationEstimate;
						
						
						
						
					}
					
					/////////////////////////////////////////////////////////
					// The column "Correlation Of coreCheck (entrySummary)"   
					/////////////////////////////////////////////////////////
					if ((this.lstCurrentReports.get(intCurrentReportIndex).getEntryCoreChecks() != null) &&
                        (this.lstPreviousReports.get(intPreviousReportIndex).getEntryCoreChecks() != null)) {
						douEntryCorrelationCoreCheck = this.getDoublePearsonProductMomentCorrelationCoefficient(this.lstCurrentReports.get(intCurrentReportIndex).getEntryCoreChecks().get(strTraitName),
					                                                                                            this.lstPreviousReports.get(intPreviousReportIndex).getEntryCoreChecks().get(strTraitName));
						objArrayRowData[21] = douEntryCorrelationCoreCheck;
					}
										
					////////////////////////////////////////////////////
					// The column "Correlation Of rank (entrySummary)"     
					////////////////////////////////////////////////////
					if ((this.lstCurrentReports.get(intCurrentReportIndex).getEntryRanks() != null) &&
                        (this.lstPreviousReports.get(intPreviousReportIndex).getEntryRanks() != null)) {
						douEntryCorrelationRank = this.getDoublePearsonProductMomentCorrelationCoefficient(this.lstCurrentReports.get(intCurrentReportIndex).getEntryRanks().get(strTraitName),
					                                                                                       this.lstPreviousReports.get(intPreviousReportIndex).getEntryRanks().get(strTraitName));
						objArrayRowData[22] = douEntryCorrelationRank;
					}

					/////////////////////////////////////////////////////
					// The column "Correlation Of rawMean (exlSummary)"   
					/////////////////////////////////////////////////////
					if ((this.lstCurrentReports.get(intCurrentReportIndex).getExlRawMeans() != null) &&
						(this.lstPreviousReports.get(intPreviousReportIndex).getExlRawMeans() != null)) {
						douExlCorrelationRawMean = this.getDoublePearsonProductMomentCorrelationCoefficient(this.lstCurrentReports.get(intCurrentReportIndex).getExlRawMeans().get(strTraitName),
							                                                                                this.lstPreviousReports.get(intPreviousReportIndex).getExlRawMeans().get(strTraitName));
						objArrayRowData[23] = douExlCorrelationRawMean;
					}
					
					///////////////////////////////////////////////////////
					// The column "Correlation Of coreCheck (exlSummary)"   
					///////////////////////////////////////////////////////
					if ((this.lstCurrentReports.get(intCurrentReportIndex).getExlCoreChecks() != null) && 
                        (this.lstPreviousReports.get(intPreviousReportIndex).getExlCoreChecks() != null)) {
						douExlCorrelationCoreCheck = this.getDoublePearsonProductMomentCorrelationCoefficient(this.lstCurrentReports.get(intCurrentReportIndex).getExlCoreChecks().get(strTraitName),
					                                                                                          this.lstPreviousReports.get(intPreviousReportIndex).getExlCoreChecks().get(strTraitName));
						objArrayRowData[24] = douExlCorrelationCoreCheck;
					}
					
					/////////////////////////////////////////////////
					// The column "Correlation Of CAV (exlSummary)"   
					/////////////////////////////////////////////////
					if ((this.lstCurrentReports.get(intCurrentReportIndex).getExlCavs() != null) &&
                        (this.lstPreviousReports.get(intPreviousReportIndex).getExlCavs() != null)) {
						douExlCorrelationCav = this.getDoublePearsonProductMomentCorrelationCoefficient(this.lstCurrentReports.get(intCurrentReportIndex).getExlCavs().get(strTraitName),
					                                                                                    this.lstPreviousReports.get(intPreviousReportIndex).getExlCavs().get(strTraitName));
						objArrayRowData[25] = douExlCorrelationCav;
					}

					///////////////////////////////////////////////////////////
					// The column "Correlation Of estimate (locationSummary)"   
					///////////////////////////////////////////////////////////
					if(intCurrentLocationsNumber==1){
						objArrayRowData[26] = "";
					
					}else{
						if ((this.lstCurrentReports.get(intCurrentReportIndex).getLocEstimates() != null) &&
					
                        (this.lstPreviousReports.get(intPreviousReportIndex).getLocEstimates() != null)) {
						douLocCorrelationEstimate = this.getDoublePearsonProductMomentCorrelationCoefficient(this.lstCurrentReports.get(intCurrentReportIndex).getLocEstimates().get(strTraitName),
					                                                                                         this.lstPreviousReports.get(intPreviousReportIndex).getLocEstimates().get(strTraitName));
						objArrayRowData[26] = douLocCorrelationEstimate;
					
						}
					}

					/////////////////////////////////////////////////////
					// The column "Correlation Of CV (locationSummary)"     
					/////////////////////////////////////////////////////
					if(intCurrentLocationsNumber==1){
						objArrayRowData[27] = "";
						
					
					}else{
					if ((this.lstCurrentReports.get(intCurrentReportIndex).getLocCvs() != null) &&
                        (this.lstPreviousReports.get(intPreviousReportIndex).getLocCvs() != null)) {
						douLocCorrelationCv = this.getDoublePearsonProductMomentCorrelationCoefficient(this.lstCurrentReports.get(intCurrentReportIndex).getLocCvs().get(strTraitName),
					                                                                                   this.lstPreviousReports.get(intPreviousReportIndex).getLocCvs().get(strTraitName));
						objArrayRowData[27] = douLocCorrelationCv;
					}
					}

					//////////////////////////////////////////////////////////
					// The column "Correlation Of checkCV (locationSummary)"     
					//////////////////////////////////////////////////////////
					if(intCurrentLocationsNumber==1){
						objArrayRowData[28] = "";
					
					}else{
					if ((this.lstCurrentReports.get(intCurrentReportIndex).getLocCheckCvs() != null) && 
                        (this.lstPreviousReports.get(intPreviousReportIndex).getLocCheckCvs() != null)) {
						douLocCorrelationCheckCv = this.getDoublePearsonProductMomentCorrelationCoefficient(this.lstCurrentReports.get(intCurrentReportIndex).getLocCheckCvs().get(strTraitName),
					                                                                                        this.lstPreviousReports.get(intPreviousReportIndex).getLocCheckCvs().get(strTraitName));
						objArrayRowData[28] = douLocCorrelationCheckCv;
					}
					}
					
					///////////////////////////////////////////////////////////////////
					// The column "Correlation Of rawCoreCheckMean (locationSummary)"       
					///////////////////////////////////////////////////////////////////
					if(intCurrentLocationsNumber==1){
						objArrayRowData[29] = "";
					
					}else{
					if ((this.lstCurrentReports.get(intCurrentReportIndex).getLocRawCoreCheckMeans() != null) && 
                        (this.lstPreviousReports.get(intPreviousReportIndex).getLocRawCoreCheckMeans() != null)) {
						douLocCorrelationRawCoreCheckMean = this.getDoublePearsonProductMomentCorrelationCoefficient(this.lstCurrentReports.get(intCurrentReportIndex).getLocRawCoreCheckMeans().get(strTraitName),
					                                                                                                 this.lstPreviousReports.get(intPreviousReportIndex).getLocRawCoreCheckMeans().get(strTraitName));
						objArrayRowData[29] = douLocCorrelationRawCoreCheckMean;
					}
					}
				}
				
				///////////////////////////////
				// The column "Creation Time"  
				///////////////////////////////
				if (intCurrentReportIndex != -1) {
					if (this.lstCurrentReports.get(intCurrentReportIndex) != null) {
						objArrayRowData[30] = this.lstCurrentReports.get(intCurrentReportIndex).getCreationTime();
					}
				}
				
				if (intCurrentReportIndex != -1){
					
						objArrayRowData[31] = this.lstCurrentReports.get(intCurrentReportIndex).getAboslutePath().toString();
					
						if((intPreviousReportIndex != -1)) {
							objArrayRowData[32] = this.lstPreviousReports.get(intPreviousReportIndex).getAboslutePath().toString();
						}
					
				}
				
				
				// we do not consider cases where there is no data for the trait
				if(objArrayRowData[8] != null){
					
					// Condition 1
					if((locationDeviation == 0)&&((douEntryCorrelationEstimate<0.97)||(douExlCorrelationRawMean<0.97)||(douLocCorrelationEstimate<0.97&&intCurrentLocationsNumber>1)||(douLocCorrelationCv<0.97&&intCurrentLocationsNumber>1)||(douLocCorrelationCheckCv<0.97&&intCurrentLocationsNumber>1))){
						writeIssues = true;
			
					}
					
					// Condition 2 
					// if locations is not blank, and (FMConverged =no, or SM Converged=No)
					if((objArrayRowData[7] != null)&&((objArrayRowData[16]=="No")||(objArrayRowData[13]=="No")||(objArrayRowData[18]=="No"))){
						
						writeIssues = true;
						
					}
					
					
				}
				
				
				//////////////////////////////////////////
				// Writing the row data in the worksheet    
				//////////////////////////////////////////
				if(this.objOutputFormat.equals("PerRequest")){
					
						this.objReportWriter.writeRowData(objArrayRowData);
				}else{
					
					
					outputFileName = this.objOutputReportPath.toString() +  
						System.getProperty("file.separator") +
	       				strTraitName +
	       				"_" +
	       				this.objDataFormat.format(new Date()) +
	       				".csv";
					this.objReportWriterPerTrait = new ReportWriterPerTrait(outputFileName,strArrayHeaders);
					this.objReportWriterPerTrait.writeRowData(objArrayRowData);
	
					if(writeIssues){
						File dir = new File(this.objOutputReportPath.toString() +  
								System.getProperty("file.separator") +
								"Issues");
						dir.mkdir();
						
						outputFileNameIssues = this.objOutputReportPath.toString() +  
							System.getProperty("file.separator") +
							"Issues" +
							System.getProperty("file.separator") +
		       				strTraitName +
		       				"_" +
		       				this.objDataFormat.format(new Date()) +
		       				"--ISSUES.csv";
						this.objReportWriterPerTrait = new ReportWriterPerTrait(outputFileNameIssues,strArrayHeaders);
						this.objReportWriterPerTrait.writeRowData(objArrayRowData);
						
					}
				}
			}
			if(this.objOutputFormat.equals("PerRequest")){

				this.objReportWriter.closeSheet();
			}
		}

		////////////////////////////////////////////////////////////////////////
		// Checking any trial with no asr data but still with a current report    
		////////////////////////////////////////////////////////////////////////
		// This will not be taken into account for the reports per trait
		///////////////////////////////////////////////////////////////////////
		
		if(this.objOutputFormat.equals("PerRequest")){

			objLogger.info("Generating a list of trials with no asr data and a report...");
			this.objReportWriter.createSheet("No Asr File Created",
                                         strArrayHeaders);
			for (Report objCurrentReport : this.lstCurrentReports) {
			
				if (this.lstTrials.indexOf(new Trial(objCurrentReport.getTrialName(), 
					                             null)) == -1) {
					objArrayRowData = new Object[strArrayHeaders.length];
					objArrayRowData[ 6] = objCurrentReport.getTrialName();
					this.objReportWriter.writeRowData(objArrayRowData);
				}
			}
			this.objReportWriter.closeSheet();
		
			return this.saveAllProcessedSheetsData();
		}else{
			return "<separate file for every trait>";
		}
	}

	private boolean getBoundedC(String strInC) {
		if (strInC == null) { 
			return false; }
		
		return (!(strInC.equals("P")));
	}
	
	private String translateBooleanValue(boolean bolInValue) {
		if (bolInValue == true) {
			return "Yes";
		}
		else {
			return "No";
		}
	}

	private double getDoublePearsonProductMomentCorrelationCoefficient(Map<String, Double> mapInStringKeyDoubleX,
                                                                       Map<String, Double> mapInStringKeyDoubleY)
	{
		double douPearsonProductMomentCorrelationCoefficient;
		List<Double> lstDoubleX;
		List<Double> lstDoubleY;
		double[] douArrayX;
	    double[] douArrayY;

		douPearsonProductMomentCorrelationCoefficient = 0.0;
		if ((mapInStringKeyDoubleX != null) && 
		    (mapInStringKeyDoubleY != null))
		{
			lstDoubleX = new ArrayList<Double>();
			lstDoubleY = new ArrayList<Double>();
			for (String strKey : mapInStringKeyDoubleX.keySet()) {
				if (mapInStringKeyDoubleY.containsKey(strKey)) {
					lstDoubleX.add(mapInStringKeyDoubleX.get(strKey));
					lstDoubleY.add(mapInStringKeyDoubleY.get(strKey));
				}
			}
			if (1 < lstDoubleX.size()) {
				douArrayX = ArrayUtils.toPrimitive(lstDoubleX.toArray(new Double[lstDoubleX.size()]));
				douArrayY = ArrayUtils.toPrimitive(lstDoubleY.toArray(new Double[lstDoubleY.size()]));
				try {
					douPearsonProductMomentCorrelationCoefficient = new PearsonsCorrelation().correlation(douArrayX, 
							                                                                              douArrayY);
				}
				catch (IllegalArgumentException e) {
					this.objReportWriter.closeSheet();
					this.saveAllProcessedSheetsData();
					System.err.println("Exception in trying to make the Pearson's correlation coefficient");
					e.printStackTrace();
					objLogger.error("Validator.getPearsonProductMomentCorrelationCoefficient", 
					                e);
					throw new RuntimeException();
				}
			}
		}
		
		return douPearsonProductMomentCorrelationCoefficient;
	}	

	private String saveAllProcessedSheetsData() {
		//////////////////////////////////////////////
		// Writing all the the data in the .xls file    
		//////////////////////////////////////////////
		String trialN;
		String trialNPrevious;
		String[] strArrayLineParts;
		String outputFileName;
		
		if(this.objCurrentReportsDirectoryPath == null){
			trialN = this.strTrialName;
			trialNPrevious=trialN;
		}else{
			strArrayLineParts = this.objCurrentReportsDirectoryPath.toString().split("\\/");
			if (!(strArrayLineParts[strArrayLineParts.length - 2].startsWith("ARCHIVE_"))) {			
				trialN = strArrayLineParts[strArrayLineParts.length - 2];
			}else{
				trialN = strArrayLineParts[strArrayLineParts.length - 4];
			}
			strArrayLineParts = this.objPreviousReportsDirectoryPath.toString().split("\\/");
			if (!(strArrayLineParts[strArrayLineParts.length - 2].startsWith("ARCHIVE_"))) {			
				trialNPrevious = strArrayLineParts[strArrayLineParts.length - 2];
			}else{
				trialNPrevious = strArrayLineParts[strArrayLineParts.length - 4];
			}
		}
		// it will enter here if trial names are the same or if you did not force match
		// if names are different and we do not force match, there is no match
		if((trialN.equals(trialNPrevious))||(!this.forceMatch)){
			outputFileName = this.objOutputReportPath.toString()  + 
		           				System.getProperty("file.separator") +
		           				"report_trial_" +
		           				trialN +
		           				//"_" +
		           				//this.objDataFormat.format(new Date()) + 
		           				".xls";
		}else{

			outputFileName = this.objOutputReportPath.toString() +
		           				System.getProperty("file.separator") +
		           				"report_trial_" +
		           				trialN +
		           				"_vs_trial_"+
		           				trialNPrevious +
		           				//"_" +
		           				//this.objDataFormat.format(new Date()) + 
		           				".xls";
			
		}
		this.objReportWriter.close(outputFileName);
		return outputFileName;
	}
	
	private void createHighLevelSummary(String strInFileName){
		String strOutputFileName = strInFileName.substring(0, strInFileName.lastIndexOf(".")+1) + "HighLevelSummary" + ".xls";
		
		ReportReader reader = new ReportReader(strInFileName);
		highLevelSummary.ReportWriter writer = new highLevelSummary.ReportWriter(strOutputFileName, reader.getData());
	}
}
