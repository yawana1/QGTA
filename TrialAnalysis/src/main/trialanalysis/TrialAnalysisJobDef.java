package main.trialanalysis;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import main.Main;
import main.jobDef.JobDefProcesser;
import main.jobDef.JobDefProcesserOneStep;
import main.jobDef.JobDefProcesserTwoStep;

import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import utils.Funcs;
import data.XML;
import data.xml.objects.App;
import data.xml.objects.Constants;
import data.xml.objects.JobDefinitions;
import data.xml.objects.Schedule;
import data.xml.objects.Traits;
import data.xml.objects.Trial;
import db.Sessions;
import error.ErrorMessage;

/***
 * Creates xml properties files that define how to run an analysis.
 * Uses existing xml template files and filles in analysis specific information
 *  
 * @author Scott Smith
 *
 */

public class TrialAnalysisJobDef {

	private static Logger log = Logger.getLogger(TrialAnalysisJobDef.class.getName());
	private List<String> exps;
	private List<String> jobs;
	private String jobDefDir;
	private final static String EXPERIMENT_DELIMITER = ",";
	boolean useArchive;
	boolean useQueue;
	boolean useUpdated;
	boolean useChecksum;
	boolean useSchedule;
	boolean writeFile;
	boolean omitHeatmap;
	private JobDefProcesser jobDefProcessor;
	private TrialAnalysisJobProcess trialProcessor;
	
	/***
	 * Check if jobDef directory exists and is readable
	 * Parse experiments if a delimited list was supplied.
	 * 
	 * @param force - True = Pull all trials whether or not they've been updated. False = Only run if data has changed
	 * @param exp - delimit string of experiments to run
	 * @param dir - Directory where the jobDef files are
	 * @param args 
	 */
	public TrialAnalysisJobDef(boolean force, String exp, String dir, String[] args) {
		if(dir == null){
			log.error("JobDef directory " + ErrorMessage.INSTANCE.getMessage("null_value"));
		}
		else{
			Path path = Paths.get(dir);
			if(!Files.isReadable(path)){
				log.error(ErrorMessage.INSTANCE.getMessage("not_found_file") + " - " + dir);
			}
			else{
				trialProcessor = new TrialAnalysisJobProcess(useArchive, args);
				jobDefProcessor = new JobDefProcesserTwoStep();
				writeFile = true;
				String[] experiments = exp==null ? null : exp.split(EXPERIMENT_DELIMITER);
				if(experiments == null){
					this.exps = null;
				}
				else{
					this.exps = new ArrayList<String>(experiments.length);
					Collections.addAll(this.exps, experiments);
				}
				
				jobDefDir = dir;
				loadProperties(); //load database connection
			}
		}
	}
	
	public TrialAnalysisJobDef(boolean useArchive, boolean useQueue, boolean useUpdated,
			boolean useChecksum, boolean useSchedule, boolean omitHeatmap, String exps,
			String jobs, String dirJobs, String[] args) {
		if(dirJobs == null){
			log.error("JobDef directory " + ErrorMessage.INSTANCE.getMessage("null_value"));
		}
		else{
			this.jobDefDir = dirJobs;
			Path path = Paths.get(dirJobs);
			if(!Files.isReadable(path)){
				log.error(ErrorMessage.INSTANCE.getMessage("not_found_file") + " - " + dirJobs);
			}
			else{
				trialProcessor = new TrialAnalysisJobProcess(useArchive, args);
				jobDefProcessor = new JobDefProcesserOneStep(useChecksum);
				
				this.useArchive = useArchive;
				this.useQueue = useQueue;
				this.useUpdated = useUpdated;
				this.useChecksum = useChecksum;
				this.useSchedule = useSchedule;
				this.omitHeatmap = omitHeatmap;
				writeFile = useQueue;
				
				String[] lstExps = exps==null ? null : exps.split(EXPERIMENT_DELIMITER);
				String[] lstJobs = jobs==null ? null : jobs.split(EXPERIMENT_DELIMITER);
				if(lstExps == null){
					this.exps = null;
				}
				else{
					this.exps = new ArrayList<String>(lstExps.length);
					Collections.addAll(this.exps, lstExps);
				}
				if(lstJobs == null){
					this.jobs = null;
				}else{
					this.jobs = new ArrayList<String>(lstJobs.length);
					Collections.addAll(this.jobs, lstJobs);
				}

				loadProperties(); //load database connection
			}
		}
	}
	
	/**
	 * 
	 */
	public void run() {
		run(getJobDefFiles());
	}
	
	public void run(List<String> jobDefFiles){
		try{
			for(String jobDefFile : jobDefFiles){
				try{
					Path path = Paths.get(jobDefFile);
					String name = path.getFileName().toString();
					Main.setLogger(Funcs.getFileNameWithoutExtension(name), name.split("\\.")[0], false);
					JobDefinitions jobDef = new JobDefinitions();
					XML.INSTANCE.deserialize(jobDefFile, jobDef);
					
					//if outside of defined schedule skip
					if(useSchedule){
						if(!Schedule.canRun(jobDef.getSchedule())){
							continue;
						}
					}
					
					jobDef.setFileName(jobDefFile);
					
					//update to full paths
					if(useUpdated){
						//TODO
						String sqlFile = jobDef.getSqlFile();
						jobDef.setSqlFile(sqlFile.replace("pheno", "pheno.updated"));
					}
					jobDef.setSqlFile(Paths.get(jobDefDir,jobDef.getSqlFile()).toString());
					jobDef.setTrialFile(Paths.get(jobDefDir,jobDef.getTrialFile()).toString());
					
					List<Trial> trials = jobDefProcessor.processJobDef(jobDef, exps);

					for(Trial t : trials){
						t.setDoHeatMap(!omitHeatmap);
					}
					
					LoggerFactory.getLogger("metrics").info(String.format("%s, %s", Paths.get(jobDefFile).getFileName() ,trials.size()));
					if(writeFile){
						writeTrialFiles(trials);
					}
					else{
						processTrials(trials);
					}
				}
				catch (Exception e) {
					log.error("Error on - " + jobDefFile, e);
				}
			}
		}catch(Exception e){
			log.error("", e);
		}
	}

	/**
	 * Retrieve all the .xml files in the jobDef directory
	 * 
	 * @return - List of full path to jobDef files
	 */
	private List<String> getJobDefFiles(){
		List<String> files;
		File dir = new File(jobDefDir);
		
		if(jobs != null && jobs.size() > 0){
			//get files from j parameter passed in
			files = new ArrayList<>();
			if(jobs != null && jobs.size() > 0){
				for(String jobFileName : jobs){
					files.add(jobFileName);
				}
			}
		}
		else{
			//get files from jobDef directory
			FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					boolean result = false;
					result = filename.endsWith(".xml");
					if(jobs != null && jobs.size() > 0 && !jobs.contains(filename)){
						result = false;
					}
					return result;
				}
			};
			files = new ArrayList<>(Arrays.asList(dir.list(filter)));						
		}
		
		List<String> xmls = new ArrayList<String>(files.size());
		for(String file : files){
			xmls.add(Paths.get(jobDefDir,file).toString());
		}
		return xmls;
	}
	
	/**
	 * List trials to xml files in the models folder
	 * With name of trial.season.xml
	 * @param trials
	 */
	private void writeTrialFiles(List<Trial> trials){
		if(trials != null){
			for(Trial trial : trials){
				String trialName = trial.getTrialName();
				try{
					trialName = Trial.cleanName(trialName);
					if(trial != null){
						//write trial file to models folder
						String file = Trial.getTrialXMLName(App.INSTANCE.getModelsDirectory(), trial).toString();
						XML.INSTANCE.serialize(trial, file);
						if(useQueue){
							trialProcessor.run(file, useQueue);
						}
					}
				}catch(Exception e){
					log.error(trialName ,e);
				}
			}
		}
	}
	
	private void processTrials(List<Trial> trials){
		if(trials != null){
			for(Trial trial : trials){
				trialProcessor.run(trial, false);
			}
		}
	}
	
	/**
	 * Loads xml properties files into objects that are needed for this JobType.
	 */
	public void loadProperties(){
		XML.INSTANCE.deserialize(App.INSTANCE.getPropertiesDirectory()+"/Db.xml", Sessions.INSTANCE);
		XML.INSTANCE.deserialize(App.INSTANCE.getPropertiesDirectory()+"/Traits.xml", Traits.INSTANCE);
		XML.INSTANCE.deserialize(App.INSTANCE.getPropertiesDirectory()+"/Constants.xml", Constants.INSTANCE);
	}
}