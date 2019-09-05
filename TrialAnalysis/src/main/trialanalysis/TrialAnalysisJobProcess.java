package main.trialanalysis;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import data.XML;
import data.collection.JobDefinition;
import data.xml.objects.App;
import data.xml.objects.Constants;
import data.xml.objects.Experiment;
import data.xml.objects.JobDefinitions;
import data.xml.objects.StatModels;
import data.xml.objects.Traits;
import data.xml.objects.Trial;
import db.Sessions;
import error.ErrorMessage;
import report.Archive;
import utils.Funcs;
import utils.Globals.TrialType;
import utils.SystemEx;

/***
 * Initialize the System to run an analysis on a trial
 * 
 * @author Scott Smith
 *
 */

public class TrialAnalysisJobProcess {

	private static Logger log = Logger.getLogger(TrialAnalysisJobProcess.class.getName());
	private boolean archive;
	private String[] args; //pass through args from command line if queued
	
	public TrialAnalysisJobProcess(boolean archive){
		this.archive = archive;
		loadProperties();  //load db and constansts configurations
	}

	public TrialAnalysisJobProcess(boolean archive, String[] args){
		this.archive = archive;
		this.args = args;
		loadProperties();  //load db and constansts configurations
	}

	
	/**
	 * Start an analysis job for the given trial file
	 * 
	 * @param file - Trial.xml to run
	 */
	public void start(String file){
		start(file, false);
	}
	
	/**
	 * Start an analysis job for the given trial
	 * 
	 * @param trial - Trial to run
	 */
	public void start(Trial trial){
		start(trial, false);
	}
	
	/***
	 * Run the specified trial.xml file.
	 * 
	 * @param file - Trial to run
	 * @param cluster - True will queue the jobs to the cluster
	 */
	public void start(String file, boolean cluster){
		if(file == null){
			run(cluster);
		}
		else{
			run(file, cluster);
		}
	}
	
	/***
	 * Run the specified trial file.
	 * 
	 * @param trial - Trial to run
	 * @param cluster - True will queue the jobs to the cluster
	 */
	public void start(Trial trial, boolean cluster){
		run(trial, cluster);
	}
	
	/***
	 * Run every trial.xml file found in the models directory
	 * 
	 * @param cluster - True will queue the jobs to the cluster
	 */
	public void run(boolean cluster){
		List<String> trialFiles = getTrialFiles();
		for(String file : trialFiles){
			run(file, cluster);
		}
	}
	
	/**
	 * Pick the appropriate priority queue to send this job to based on the TrialType.
	 * 
	 * @param trialType
	 * @return
	 */
	public String getQueue(TrialType trialType){
		String result = Constants.INSTANCE.getConstant("lowPriorityQueue").toString();
		
		switch (trialType) {
		case MULTI:
			result = Constants.INSTANCE.getConstant("normalPriorityQueue").toString();
			break;
		case SINGLE:
			result = Constants.INSTANCE.getConstant("highPriorityQueue").toString();
			break;
		default:
			break;
		}
		
		return result;
	}
	
	public static String getMem(TrialType trialType){
		String result = "";
		
		switch (trialType) {
		default:
			break;
		}
		
		return result;
	}
	
	public static int concurrentProcessMax(TrialType trialType){
		int result = 8; //default
		
		switch (trialType) {
		case MULTI:
			result = 8;
			break;
		case SINGLE:
			result = 4;
			break;
		default:
			break;
		}
		
		return result;
	}
	
	protected void recordJob(@SuppressWarnings("unused") int processors) {
		// No work to do here, needed for subclass use
	}
	
	protected String buildCmd(Trial trial, String file) {
		TrialType trialType = trial.getType();
		
		String args = " ";
		for(int i=0; i< this.args.length; i++){
			String a = this.args[i];
			//don't double add file
			if(a.contains("-f")){
				i++;
			}
			else{
				args += a + " ";
			}
		}
		
		//keep memory option if in parameter list, if nothing check default settings
		if(!args.contains("-m")){
			String mem = getMem(trialType);
			if(mem != null && !"".equals(mem)){
				args = " -m " + mem + args;
			}
		}
		
		// This is building what the application used by default
		String cmd = App.INSTANCE.getRunScriptDirectory() 
				+ "/trialAnalysis.ceres.new.sh -l " 
				+ App.INSTANCE.getLogDir() 
				+ " -f " + file 
				+ " -q " + getQueue(trialType)
				+ " -p " + trial.getConcurrentProcessMax()
				+ args;

		return cmd;
	}
	
	/**
	 * @param file - Trial xml to run
	 * @param cluster - Run on the cluster or not
	*/
	public void run(String file, boolean cluster){
		Trial trial;
		try {
			trial = getTrial(file);
			
			if(trial != null){
				//qsub the job if cluster parameter is set
				if(cluster){
					
					//don't archive if just sending to cluster only archive on actual trial processing.
					boolean archiveFlag = archive;
					if(archive && cluster){
						archiveFlag = false;
					}
					init(trial, archiveFlag, cluster);
					
					recordJob(trial.getConcurrentProcessMax());
					String cmd = buildCmd(trial, file);
					SystemEx.run(cmd);
				}
				else{
					run(trial, cluster);
				}
			}
		} 
		catch (Exception e) {
			log.error("Trial failed file name - " + file, e);
		}
	 }
	
	/**
	 * Process trial based on the cluster flag.  If true create cluster queue unix command to schedule the job.  Otherwise process the trial normally. 
	 * 
	 * @param file - Trial xml to run
	 * @param cluster - Run on the cluster or not
	 */
	public void run(Trial trial, boolean cluster){
		try{			
			if(trial != null){
				//qsub the job if cluster parameter is set
				if(!cluster){
					trial = init(trial, archive, cluster);
					
					boolean success = processTrial(trial);

					//cleanup
					cleanup(trial, success);
				}
			}
		} 
		catch (Exception e) {
			String name =  trial != null ? trial.getTrialName() : "";
			log.error("Trial - " + name, e);
		}
	}
	
	/**
	 * Load .xml config files
	 */
	public void loadProperties(){
		XML.INSTANCE.deserialize(App.INSTANCE.getPropertiesDirectory()+"/Constants.xml", Constants.INSTANCE);
		XML.INSTANCE.deserialize(App.INSTANCE.getPropertiesDirectory()+"/Traits.xml", Traits.INSTANCE);
		XML.INSTANCE.deserialize(App.INSTANCE.getPropertiesDirectory()+"/StatisticsModels.xml", StatModels.INSTANCE);
		XML.INSTANCE.deserialize(App.INSTANCE.getPropertiesDirectory()+"/Db.xml", Sessions.INSTANCE);
	}
	
	/***
	 * Get all trial.xml files in the models folder
	 * 
	 * @return - A List of all trial.xml files in the models folder
	 */
	public List<String> getTrialFiles(){
		String[] files;
		File dir = new File(App.INSTANCE.getModelsDirectory());
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".xml") && !filename.contains("step2");
			}
		};
		files = dir.list(filter);
		List<String> xmls = new ArrayList<String>();
		for(String file : files){
			xmls.add(App.INSTANCE.getModelsDirectory()+"/"+file);
		}
		return xmls;
	}
	
	public boolean processTrial(Trial trial){
		boolean result = false;
		try {
			TrialProcessor proc = new TrialProcessor(trial);
			proc.run();
			result = true;
		} 
		catch (Exception e) {
			String name =  trial != null ? trial.getTrialName() : "";
			log.error("Trial - " + name, e);
		}
		return result;
	}
	
	/**
	 * Setup trial logger to log messages to this trial.
	 * create directory for trial data.
	 * archive previous run
	 * move model file to running folder
	 * @param trial
	 * @param archive
	 * @param cluster
	 * @throws Exception
	 */
	public static Trial init(Trial trial, boolean archive, boolean cluster) throws Exception{
		//setup logger for this trial only if not being sent to the cluster
		System.setProperty("experimentId",trial.getTrialName());
		System.setProperty("seasonId",trial.getSeasonName());
		URL logFile = Thread.currentThread().getContextClassLoader().getResource("log4j.properties");
		if(trial.getExecutionStepStart() > 0){
			Properties properties = new Properties();
			properties.load(logFile.openStream());
			properties.setProperty("log4j.appender.file.Append","true");
			PropertyConfigurator.configure(properties);
		}
		else{
			PropertyConfigurator.configure(logFile);
		}
		log = Logger.getLogger(TrialAnalysisJobProcess.class.getName());
		
		//create base work dir
		Path dir = Paths.get(trial.getTrialDirectory());
		Funcs.createWithPermissions(dir,true);
		
		dir = Paths.get(trial.getTrialWorkDirectory());
		Funcs.createWithPermissions(dir,true);
		
		dir = Paths.get(trial.getReportDirectory(false));
		Funcs.createWithPermissions(dir,true);
		
		if(archive){
			log.info("Hostname : " + InetAddress.getLocalHost().getHostName());
			archive(trial);
		}
			
		//delete trial.xml in the models folder
		if(cluster){
			//set
			if(!TrialType.SINGLE.equals(trial.getType())){
				int exectuionStepRunAsreml = 5;
				trial.setExecutionStepEnd(exectuionStepRunAsreml);
			}
			//write runnable xml file to models folder
			Path serializedTrialFile = Trial.getTrialXMLName(App.INSTANCE.getModelsDirectory(), trial);
			XML.INSTANCE.serialize(trial, serializedTrialFile.toString());
			
			//create second step run for non single trials
			if(!TrialType.SINGLE.equals(trial.getType())){
				trial.setExecutionStepStart(4);
				trial.setExecutionStepEnd(Integer.MAX_VALUE);
				serializedTrialFile = Trial.getTrialXMLName(App.INSTANCE.getModelsDirectory(), trial);
				serializedTrialFile = Paths.get(serializedTrialFile.toString().replace(".xml", ".step2.xml"));
				XML.INSTANCE.serialize(trial, serializedTrialFile.toString());
			}
		}
		else{
			Path serializedTrialFile = Trial.getTrialXMLName(trial.getTrialWorkDirectory(), trial);
			//Files.deleteIfExists(Trial.getTrialXMLName(App.INSTANCE.getModelsDirectory(), trial));
			if(Files.exists(Trial.getTrialXMLName(App.INSTANCE.getModelsDirectory(), trial))){
				Files.move(Trial.getTrialXMLName(App.INSTANCE.getModelsDirectory(), trial), Trial.getTrialXMLName(App.INSTANCE.getRunningDirectory(), trial), StandardCopyOption.REPLACE_EXISTING);
			}
			XML.INSTANCE.serialize(trial, serializedTrialFile.toString());
			try{
				Files.setPosixFilePermissions(serializedTrialFile, Files.getPosixFilePermissions(serializedTrialFile.getParent()));
			}
			catch(AccessDeniedException e){
				//eat access deined for setting permissions
			}
		}
		
		//pull serialized state if current execution step is past 0
		if(trial.getExecutionStepStart() > 0){
			Path path = Paths.get(trial.getTrialWorkDirectory(), Serializer.getBinaryFileName());
			if(Files.exists(path)){
				int start = trial.getExecutionStepStart();
				int end = trial.getExecutionStepEnd();
				//if deserialization fails just rerun full
				try{
					trial = Serializer.INSTANCE.deserialize(Trial.class, path);
					trial.setExecutionStepStart(start);
					trial.setExecutionStepEnd(end);
				}
				catch(Exception e){
					trial.setExecutionStepStart(0);
				}
			}
			else{
				trial.setExecutionStepStart(0);
			}
		}
		return trial;
	}
	
	/**
	 * Delete model file from running folder 
	 * @throws IOException 
	 */
	public void cleanup(Trial trial, boolean success){
		if(success){
			Path file = Trial.getTrialXMLName(App.INSTANCE.getRunningDirectory(), trial);
			try{
				Files.deleteIfExists(file);
			}
			catch(Exception e){
				log.error(" not in the running directory to delete");
			}
		}
	}
	
	/**
	 * Deserialize the trial.xml and create the base working directory for analysis
	 * 
	 * @param file - Trial.xml to run
	 * @param deleteFile - Delete trial.xml from models folder
	 * @return - Trial object created from trial.xml
	 * @throws IOException
	 */
	public static Trial getTrial(String file){
		Trial trial = null;
		try{
			Path trialFile = Paths.get(file);
			
			if(!Files.isReadable(trialFile)){
				log.error(ErrorMessage.INSTANCE.getMessage("not_found_file") + file);
			}
			else{
				//load trial from properties file
				trial = new Trial(App.INSTANCE.getWorkDirectory());
				XML.INSTANCE.deserialize(file, trial); //load genoType and environment levels
				XML.INSTANCE.deserialize(trial, trial.getDataLevel());
				
				trial.setConcurrentProcessMax(concurrentProcessMax(trial.getType()));
			}
		}
		catch (Exception e) {
			trial = null;
			log.error(ErrorMessage.INSTANCE.getMessage("init_error") + file, e);
		}
		return trial;
	}
	
	/**
	 * Create trial Object by using template file and then filing that out with the data from the jobDef and job objects.
	 * 
	 * @param jobDef
	 * @param job
	 * @return
	 */
	public static Trial createTrial(JobDefinitions jobDef, JobDefinition job, boolean dataLevel) {
		Trial trial = null;
		try{
			trial = new Trial(App.INSTANCE.getWorkDirectory());
			XML.INSTANCE.deserialize(jobDef.getTrialFile(), trial);
			if(dataLevel){
				XML.INSTANCE.deserialize(trial, trial.getDataLevel());
			}
			trial.setCrop(jobDef.getCrop());
			
			if (jobDef.isUserDefined() && job.isMulti()) {
				trial.setTrialName(jobDef.getName());
			} 
			else {
				trial.setTrialName(job.getExpName());
			}
			
			if (trial.getExperiments() == null)
				trial.setExperiments(new ArrayList<Experiment>());
			if (trial.getExperimentNames() == null)
				trial.setExperimentNames(new ArrayList<String>());
			if (jobDef.getExperiments() != null) {
				trial.getExperiments().addAll(Experiment.getExperiments(jobDef.getExperiments()));
			}
			if (jobDef.isUserDefined() || job.isMulti() || TrialType.MULTI.equals(trial.getType())) {
				trial.setType(TrialType.MULTI);
				trial.setProjectName(trial.getType().toString());
				trial.setSeasonName(jobDef.getSeasonName());
				trial.setExpZone(jobDef.getZone());
				trial.setRegion(jobDef.getRegion());
				trial.setDirections(jobDef.getDirections());
				trial.setZones(jobDef.getZones());
				trial.setTrialRestriction(jobDef.getTrialRestrictions());
				trial.setSeasonId(jobDef.getSeasonId());
			}
			else{
				trial.getExperimentNames().add(job.getExpName());
				trial.setProjectName(job.getProjectName());
				trial.setSeasonId("" + job.getSeasonId());
				trial.setSeasonName(job.getSeasonName());
			}

			trial.setAnalysisStage(job.getAnalysisStage());
			trial.setClassName(job.getClassName());
			trial.setAnalysisDate(job.getAnalysisDate());
			trial.setLastUpdated(job.getAnalysisDateChanged());
			if (trial.getAnalysisState() == null){
				trial.setAnalysisState(new ArrayList<Integer>());
			}
			trial.setAnalysisState(jobDef.getAnalysisState());
			trial.setAnalysisPeopleId(job.getAnalysisPeopleId());
			trial.setSqlTemplateFile(App.INSTANCE.getSqlDirectory() + "/" + trial.getSqlTemplateFile());
			if (trial.getXlsColumnFile() != null) {
				trial.setXlsColumnFile(App.INSTANCE.getPropertiesDirectory() + "/" + trial.getXlsColumnFile());
			}
			trial.setConcurrentProcessMax(TrialAnalysisJobProcess.concurrentProcessMax(trial.getType()));
		} catch (Exception e) {
			log.error(job.getExpName(), e);
		}
		return trial;
	}
	
	private static void archive(Trial trial) throws Exception{
		//archive folders
		long timeStart = System.currentTimeMillis();
		Object exclusion = null;
		if((exclusion = Constants.INSTANCE.getConstant("output_data_file")) == null){
			throw new Exception("output_data_file not set as a Constant");
		}
		String[] exclusions = {exclusion.toString(),"insilico.txt"};
		Archive.archive(trial, Arrays.asList(exclusions));
		long timeArchive = System.currentTimeMillis();
		TrialProcessor.log.warn("TIME - ARCHIVE = " + (timeArchive - timeStart));
	}
}