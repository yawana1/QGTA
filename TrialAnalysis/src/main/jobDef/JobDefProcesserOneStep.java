package main.jobDef;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import main.trialanalysis.TrialAnalysisJobProcess;

import org.apache.log4j.Logger;

import utils.Funcs;
import asreml.AsremlAsd;
import data.collection.JobDefinition;
import data.collection.TrialData;
import data.xml.objects.Constants;
import data.xml.objects.JobDefinitions;
import data.xml.objects.Trial;
import db.modules.RetrieveDataDBTrial;

/**
 * Processes all the trials for a jobDef.
 * 
 * Queries all data for a jobDef and then splits that up into individual trial objects, runs a checksum on previous data to check if data has changed and this 
 * trial needs to be run now.
 * 
 * @author Scott Smith
 *
 */
public class JobDefProcesserOneStep implements JobDefProcesser {
	
	private static Logger log = Logger.getLogger(JobDefProcesserOneStep.class.getName());
	private boolean useChecksum;
	
	public JobDefProcesserOneStep(boolean useChecksum){
		this.useChecksum = useChecksum;
	}
	
	/**
	 * @return 
	 * 
	 */
	public List<Trial> processJobDef(JobDefinitions jobDef, List<String> exp) throws Exception{
		
		long timeStart = System.currentTimeMillis();
		//get data for all trials in this jobDef.
		TrialData multiTrialData = new RetrieveDataDBTrial(jobDef, exp).execute();
		long timeVariety = System.currentTimeMillis();
		log.warn("TIME - DATA - RETRIEVE = " + (timeVariety - timeStart));
		
		//split data into experiments
		Map<Object,TrialData> byExperiment = new HashMap<>();
		for(Map<String, Object> d : multiTrialData.getData()){
			String key = "gId";
			Object experimentName = jobDef.isUserDefined() ? jobDef.getName() : d.get(key);
			if(!byExperiment.containsKey(experimentName)){
				byExperiment.put(experimentName, new TrialData(new ArrayList<Map<String,Object>>(), multiTrialData.getTypes()));
			}
			byExperiment.get(experimentName).getData().add(d);
		}
		
		long timeSplitData = System.currentTimeMillis();
		log.warn("TIME - DATA - SPLIT = " + (timeSplitData - timeVariety));
		log.warn(String.format("Number of trials - %s for JobDef - %s", byExperiment.keySet().size(), jobDef.getFileName()));
		
		//check if each trial needs to be processed.
		List<Trial> trialsToRun = new ArrayList<>();
		for(Entry<Object, TrialData> trialData : byExperiment.entrySet()){
			//create trial object get work folder
			List<Map<String,Object>> data = trialData.getValue().getData();
			
			if(data != null && data.size() > 0){
				Map<String,Object> row = data.get(0);
				JobDefinition job = new JobDefinition(row);
				Trial trial = TrialAnalysisJobProcess.createTrial(jobDef, job, true);
				
				boolean checkSum = false; //default to true if no previous working folder = run
				Path workPath = Paths.get(trial.getTrialWorkDirectory());
				//create trial folder and put data file in it
				if(!Files.exists(workPath)){
					Funcs.createWithPermissions(workPath,true);
					AsremlAsd.createValuesFile(trial, data, "" + Constants.INSTANCE.getConstant("output_data_file"));
				}
				else{
					//if doesn't exist create data file
					String dataFile = "" + Constants.INSTANCE.getConstant("output_data_file");
					Path dataPath = workPath.resolve(dataFile);
					if(!Files.exists(dataPath)){
						AsremlAsd.createValuesFile(trial, data, "" + Constants.INSTANCE.getConstant("output_data_file"));
					}
					//if does exist create temp and do checksum
					else{
						//create temp data to compare with previous run data			
						Path currentDataFile = AsremlAsd.createValuesFile(trial, data, "" + Constants.INSTANCE.getConstant("output_data_file_current"));
			
						//run checksum on current and previous data
						Path previousFile = currentDataFile.resolveSibling(Paths.get(""+Constants.INSTANCE.getConstant("output_data_file")));
						if(useChecksum){
							checkSum = Funcs.checkSumCompare(currentDataFile.toString(), previousFile.toString());
						}
						
						//if same data delete temp file
						//else change temp to data file
						if(checkSum){
							Files.delete(currentDataFile);
							log.warn("Checksum shows no data change - " + trial.getTrialName());
						}
						else{
							Files.move(currentDataFile, previousFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
						}
					}
				}

				//if different submit job trial to run.
				if(!checkSum){
					trialsToRun.add(trial);
				}
			}
		}
		long timeCheckSum = System.currentTimeMillis();
		log.warn("TIME - DATA - CHECKSUM = " + (timeCheckSum - timeSplitData));
		log.warn(String.format("Number of trials with new data to run - %s for JobDef - %s", trialsToRun.size(), jobDef.getFileName()));
		return trialsToRun;
	}
}