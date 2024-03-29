/*
 * 
 */
package validation;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import main.Main;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import utils.ArgumentParser;
import data.XML;
import data.xml.objects.App;

public class ValidationEngine {
	
	private static Logger objLogger;
	private final static String APPLICATION_CONFIGURATION = "App.xml";
	
	private ArgumentParser objArgumentParser;
	private String strApplicationConfigurationFile;
	private URL objURLApplicationConfigurationFile;
	private String trialName;
	private String crop;
	private String projectName;
	private boolean forceMatch;
	private Path objPreviousReportsDirectoryPath;
	private Path objCurrentReportsDirectoryPath;
	private String strSeasonName;
	private URL objURLLogFile;
	private ValidationProcess objValidationProcess;
	private long lngStartTime;
	private Path objOutputReportPath;
	private String objOutputFormat;
	private String classType;
	private boolean loggerFlag;

	
	
	public ValidationEngine(String[] args, boolean loggerFlag) {

		this.loggerFlag = loggerFlag;
		String strOutputReportArgument;
		String strOutputFormatArgument;
		String strSeasonNameArgument;
		String strProjectNameArgument;
		String strForceMatchArgument;		
		String cropArgument;
		String strPreviousReportsDirectoryArgument;
		String strCurrentReportsDirectoryArgument;
		String strTrialNameArgument;
		String strApplicationConfigurationArgument;
		String classTypeArgument;

		if(this.loggerFlag){
			objURLLogFile = Thread.currentThread().getContextClassLoader()
					.getResource("log4j.properties");
			System.setProperty("log.file", "main");
			System.setProperty("log.dir", App.INSTANCE.getLogDir());
			PropertyConfigurator.configure(objURLLogFile);
		}
		objLogger = Logger.getLogger(Main.class.getName());
				
		String programUsage = "\n\n\n\n"
				+ "*** Please enter the arguments in the command line as follows! ***"
				+ "\n\nExamples: "
				+ "\n"
				+ "\n\nSCENARIO 1: Validate all trials for a crop, class type and season:\n"  
				+ "-> Usage: use arguments -crop, -classType, -seasonName\n"
				+ "-> Example: -crop=corn -classType=Yield_Trial -seasonName=14E\n"
				+ "\n\nSCENARIO 2: Validate a specific trial:\n"
				+ "-> Usage: use arguments -crop, -classType, -seasonName, -trialName, -projectName\n"
				+ "-> Example: -crop=corn -classType=Yield_Trial -seasonName=14E -trialName=DA505WW -projectName=W_Fowler__IN\n"
				+ "\n\nSCENARIO 3: Validate a specific trial given previous and current reports directory:\n"
				+ "-> Usage: use arguments -previousReportsDirectory, -currentReportsDirectory\n"
				+ "-> Example: -currentReportsDirectory=/data/QG/trialAnalysis/dev/runs/corn/Yield_Trial/14E/HS_Huron__SD/D2501HS/reports/"
				+ "  "
				+ "-previousReportsDirectory=/data/QG/trialAnalysis/dev/runs/corn/Yield_Trial/14E/HS_Huron__SD/D2501HS/archive/ARCHIVE_20150611090834/reports/\n"
				+ "\n\nSCENARIO 4: Validate a specific trial given previous and current reports directory without matching trial names:\n"
				+ "-> Usage: use arguments -forceMatch -previousReportsDirectory, -currentReportsDirectory\n"
				+ "-> Example: -forceMatch=true  -currentReportsDirectory=/data/QG/trialAnalysis/dev/runs/corn/Yield_Trial/14E/HS_Huron__SD/D2501HS/reports/ "
				+ "-previousReportsDirectory=/data/QG/trialAnalysis/dev/runs/corn/Yield_Trial/14E/HS_Huron__SD/D2502HS/reports/\n\n\n"
				+ "OPTIONAL ARGUMENTS:\n\n"
				+ "Argument: -outputFormat\n"
				+ "\t->Description: It defines how the output will be printed\n"
				+ "\t->Possible Values: perTrait, perRequest\n"
				+ "\t\tperTrait:  output written in .CSV files (one file per trait). If a file for that trait was already created in the output directory on the same day (results are organized first per trait and then per day), it will append the output to that file.\n"
				+ "\t\tperRequest: output written in an Excel file (a different file for each run/request of the tool)"
				+ "\t->Default: perTrait\n\n"
				+ "Argument: -outputDirectory\n"
				+ "\t->Default: <base directory>/validation/\n\n"
				+ "Argument: -appConfig\n"
				+ "\t->Default: XML file App.xml in the folder config\n\n";
				
		
		try {
			if(XML.INSTANCE == null){
				XML.INSTANCE = new XML();
			}
			objArgumentParser = new ArgumentParser(args);
			if (!(objArgumentParser.hasNext())) {
				System.out
						.println(programUsage);
				objLogger.error("No argument(s) given in the command line");
				throw new RuntimeException(
						"No argument(s) given in the command line");
			}

			// /////////////////////////
			// The argument appConfig
			// /////////////////////////
			strApplicationConfigurationFile = null;
			strApplicationConfigurationArgument = objArgumentParser
					.getOption("appConfig");
			if (strApplicationConfigurationArgument != null) {
				if (Files.isReadable(Paths
						.get(strApplicationConfigurationArgument))) {
					strApplicationConfigurationFile = strApplicationConfigurationArgument;
				} else {
					objLogger.error(Paths
							.get(strApplicationConfigurationArgument)
							.toAbsolutePath().toString());
					objLogger
							.error("The application configuration file, App.xml, is not found or readable");
					throw new RuntimeException(
							"The application configuration file, App.xml, is not found or readable");
				}
			} else {
				objURLApplicationConfigurationFile = Thread.currentThread()
						.getContextClassLoader()
						.getResource(APPLICATION_CONFIGURATION);
				if (objURLApplicationConfigurationFile != null) {
					strApplicationConfigurationFile = objURLApplicationConfigurationFile
							.getFile();
				} else {
					objLogger
							.error("The application configuration file, App.xml, is not on the classpath");
					throw new RuntimeException(
							"The application configuration file, App.xml, is not on the classpath");
				}
			}
			if (strApplicationConfigurationFile != null) {
				XML.INSTANCE.deserialize(strApplicationConfigurationFile,
						App.INSTANCE);
			}
			
			//////////////////////////////////////////////////////////
			// Get all arguments and check if they were used accordingly
			//////////////////////////////////////////////////////////
			strSeasonNameArgument = objArgumentParser.getOption("seasonName");
			cropArgument = objArgumentParser.getOption("crop");
			classTypeArgument = objArgumentParser.getOption("classType");
			strPreviousReportsDirectoryArgument = objArgumentParser.getOption("previousReportsDirectory");
			strCurrentReportsDirectoryArgument = objArgumentParser.getOption("currentReportsDirectory");
			strTrialNameArgument = objArgumentParser.getOption("trialName");
			strProjectNameArgument = objArgumentParser.getOption("projectName");
			strForceMatchArgument = objArgumentParser.getOption("forceMatch");

			crop="";
			strSeasonName = "";
			objPreviousReportsDirectoryPath = null;
			objCurrentReportsDirectoryPath = null;
			forceMatch=false;
			trialName = "";
			projectName = "";
			
			
			// Scenario 1 - several trials (wee need crop, season, and class type and nothing more)
			// Scenario 2: scenario 1 + trial name and project name
			// Scenario 3: two reports directory (specifc trial)
			// Scenario 4: Scenario 3 + force match (specifc trial)
			
			
			
			// SCENARIO 1
			if ((strSeasonNameArgument != null) && (cropArgument !=null) && (classTypeArgument !=null)){
				
				crop=cropArgument;
				strSeasonName = strSeasonNameArgument;
				classType = classTypeArgument;
				
				if ((strCurrentReportsDirectoryArgument != null) || (strForceMatchArgument !=null) || (strPreviousReportsDirectoryArgument !=null)){
					// wrong combination of arguments
					System.out.println("Wrong combination of arguments!");
					System.out.println(programUsage);
					objLogger.error("Wrong combination of arguments!");
					throw new RuntimeException(
							"Unable to continue");
					
				}else{ 
					
			
					// now we check if it is SCENARIO 2 instead
					
					// The arguments trialName and projectName are implemented in a way
					// that they should be both given as input together
					// If one is missing then we have an error
					// //////////////////////////
					// The arguments trialName and projectName
					// //////////////////////////
					if ((strTrialNameArgument != null) && (strProjectNameArgument != null)){
						trialName = strTrialNameArgument;
						projectName = strProjectNameArgument;
					}else{
						if ((strTrialNameArgument == null) && (strProjectNameArgument == null)){
							trialName = "";
							projectName = "";
						}else{
							System.out.println("The arguments trialName and projectName should be both informed or omitted together");
							System.out.println(programUsage);
							objLogger.error("The arguments trialName and projectName should be both informed or omitted together");
							throw new RuntimeException(
									"Unable to continue");
							
						}
					}
					
				}
			}else{
				// we cannot be SCENARIO 1 neither SCENARIO 2 - we will check that
				//but it might be that there is incomplete information on these scenarios
				// or it scenarios 3 or 4
				
				// SCENARIO 3
				if ((strCurrentReportsDirectoryArgument != null) && (strPreviousReportsDirectoryArgument !=null)){
				
					
					
					objPreviousReportsDirectoryPath = Paths
							.get(strPreviousReportsDirectoryArgument);
					if (!(Files.exists(objPreviousReportsDirectoryPath))) {
						System.out.println(
								"\n\nThe previous reports directory: "
										+ strPreviousReportsDirectoryArgument
										+ " does not exit\n\n");
						objLogger.error("The previous reports directory: "
								+ strPreviousReportsDirectoryArgument
								+ " does not exit");
						throw new RuntimeException(
								"The previous reports directory: "
										+ strPreviousReportsDirectoryArgument
										+ " does not exit");
					}else{
						
						objPreviousReportsDirectoryPath = objPreviousReportsDirectoryPath.toAbsolutePath();
						if(!(objPreviousReportsDirectoryPath.getFileName().toString().startsWith("reports"))){
							
							System.out.println("\n\nProblem with -previousreportsDirectory="+objPreviousReportsDirectoryPath.toString()+"\n\nInform path to the reports directory for the argument -previousReportsDirectory\n");
							
							objLogger.error("The previous reports directory: "
									+ strPreviousReportsDirectoryArgument
									+ " does not point to the directory reports");
							throw new RuntimeException(
									"The previous reports directory: "
											+ strPreviousReportsDirectoryArgument
											+ " does not point to the directory reports");
							
						}
						
					}
					
					objCurrentReportsDirectoryPath = Paths
							.get(strCurrentReportsDirectoryArgument);
					if (!(Files.exists(objCurrentReportsDirectoryPath))) {
						System.out.println(
								"\n\nThe current reports directory: "
										+ strCurrentReportsDirectoryArgument
										+ " does not exit\n\n");
						objLogger.error("The current reports directory: "
								+ strCurrentReportsDirectoryArgument
								+ " does not exit");
						throw new RuntimeException(
								"The current reports directory: "
										+ strCurrentReportsDirectoryArgument
										+ " does not exit");
					}else{
						
						objCurrentReportsDirectoryPath = objCurrentReportsDirectoryPath.toAbsolutePath();
						if(!(objCurrentReportsDirectoryPath.getFileName().toString().startsWith("reports"))){
							
							System.out.println("\n\nInform path to the reports directory for the argument -currentReportsDirectory\n");
							
							objLogger.error("The current reports directory: "
									+ strCurrentReportsDirectoryArgument
									+ " does not point to the directory reports");
							throw new RuntimeException(
									"The current reports directory: "
											+ strCurrentReportsDirectoryArgument
											+ " does not point to the directory reports");
							
						}
					}
					
					if ((strSeasonNameArgument != null) || (cropArgument !=null) || (classType !=null) || (strTrialNameArgument != null) || (strProjectNameArgument != null)){

						// wrong combination of arguments
						System.out.println("Wrong combination of arguments!");
						System.out.println(programUsage);
						objLogger.error("Wrong combination of arguments!");
						throw new RuntimeException(
								"Unable to continue");

						
					}else{
						
						crop="";
						strSeasonName = "";
						classType = "";

						// SCENARIO 4
						
						// //////////////////////////
						// The boolean argument forceMatch
						// //////////////////////////
						if (strForceMatchArgument != null){
							if(strForceMatchArgument.equalsIgnoreCase("false")){
								forceMatch=false;
							}else{
								if(strForceMatchArgument.equalsIgnoreCase("true")){
									forceMatch=true;
								}
								else{
									objLogger.error("Invalid value for the boolean argument -forceMatch. Value should be either true or false");
									throw new RuntimeException(
											"Unable to continue");
								}
							}
						}else{
							forceMatch=false;
						}			
						
					}
					
				}else{
					
					// Wrong combination of arguments
					// The arguments were not enough for any of the scenarios
					// wrong combination of arguments
					System.out.println("Wrong combination of arguments!");
					System.out.println(programUsage);
					objLogger.error("Wrong combination of arguments!");
					throw new RuntimeException(
							"Unable to continue");
					
				}
			}
			
			
			///// OPTIONAL ARGUMENTS - outputDirectory and outputReportFormat
			strOutputReportArgument = objArgumentParser.getOption("outputDirectory");
			if ((strOutputReportArgument != null)){
				
				objOutputReportPath  = Paths
						.get(strOutputReportArgument);
				if (!(Files.exists(objOutputReportPath))) {
					System.out.println("\n\nThe output directory: "
							+ strOutputReportArgument
							+ " does not exit\n\n");
					objLogger.error("The output directory: "
							+ strOutputReportArgument
							+ " does not exit");
					throw new RuntimeException(
							"The previous reports directory: "
									+ strOutputReportArgument
									+ " does not exit");
				}
			}else{
			
				
				objOutputReportPath  = Paths
						.get(App.INSTANCE.getReportDirectory() + 
		           				System.getProperty("file.separator") +
		           				"validation"+
		           				System.getProperty("file.separator"));
				
			}
			
			// two options PerRequest or PerTrait
			strOutputFormatArgument = objArgumentParser.getOption("outputFormat");
			if ((strOutputFormatArgument != null)){
				if(strOutputFormatArgument.equalsIgnoreCase("PerTrait")){
					objOutputFormat="PerTrait";
				}else{
					if(strOutputFormatArgument.equalsIgnoreCase("PerRequest")){
						objOutputFormat="PerRequest";
					}
					else{
						System.out.println("\n\nInvalid value for the argument -outputFormat. \nValue should be either PerTrait or PerRequest\n\n");
						objLogger.error("Invalid value for the argument -outputFormat. Value should be either PerTrait or PerRequest");
						throw new RuntimeException(
								"Unable to continue");
					}
				}
			}else{
				objOutputFormat = "PerTrait";
			}
			
		}catch (Exception e) {
			e.printStackTrace();
			objLogger.fatal("Error", e);
		}
			
	}
		
	public void run(){
		
		if (strApplicationConfigurationFile != null) {
			objValidationProcess = new ValidationProcess(strSeasonName,
					objPreviousReportsDirectoryPath, objCurrentReportsDirectoryPath, classType, trialName, projectName,crop,forceMatch,objOutputReportPath,objOutputFormat);
			lngStartTime = System.currentTimeMillis();
			objValidationProcess.start();
			objLogger.info("Execution Time: "
					+ (System.currentTimeMillis() - lngStartTime) / 1000
					+ " seconds");
		} else {
			objLogger
					.error("Unable to continue without any application configuration file");
			throw new RuntimeException(
					"Unable to continue without any application configuration file");
		}
		
		
	}




}
