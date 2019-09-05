package main.jobDef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.trialanalysis.TrialAnalysisJobProcess;

import org.apache.log4j.Logger;

import data.collection.JobDefinition;
import data.xml.objects.Constants;
import data.xml.objects.JobDefinitions;
import data.xml.objects.Trial;
import db.modules.JobDefSQLBuilder;

/**
 * Create trial.xml files and place in models folder to run later that are in this jobDef.
 * 
 * @author Scott Smith
 *
 */
public class JobDefProcesserTwoStep implements JobDefProcesser {

	private static Logger log = Logger.getLogger(JobDefProcesserTwoStep.class.getName());
	
	/***
	 * Run query to retrieve all trials for this jobDef.
	 * 
	 * @param jobDef
	 * @return - Map of experiment name, jobDef objects
	 * @throws Exception 
	 */
	public List<Trial> processJobDef(JobDefinitions jobDef, List<String> exp) throws Exception{
		Map<String, JobDefinition> changedJobs = new HashMap<String, JobDefinition>(); 

		changedJobs = queryJobs(jobDef, exp);

		//create trial.xml files
		List<Trial> trials = null;
		if(changedJobs!=null && !changedJobs.isEmpty()){
			trials = saveJobs(jobDef, changedJobs);
		}
		
		return trials;
	}
	
	
	
	private Map<String, JobDefinition> queryJobs(JobDefinitions jobDef, List<String> exp) throws Exception{
		List<JobDefinition> jobs = new ArrayList<JobDefinition>();
		Map<String, JobDefinition> changedJobs = new HashMap<String, JobDefinition>();
		
		jobs = JobDefSQLBuilder.getInstance().execute(jobDef, exp);
		
		if(null != jobs){
			for(JobDefinition job : jobs){
				boolean hasChanged = false;
				if(job.getAnalysisPeopleId()==null){
					hasChanged = true;
				}
				else if(!job.getAnalysisPeopleId().toString().equals(Constants.INSTANCE.getConstant("peopleId"))){
					hasChanged = true;
				}
				else if(job.getAnalysisDate()==null){
					hasChanged = true;
				}
				else if(job.getAnalysisDateChanged()==null){
					hasChanged = true;
				}
				else if(job.getAnalysisDate().before(job.getAnalysisDateChanged())){
					hasChanged = true;
				}
				else{
					hasChanged = false;
				}
				
				String experimentName = job.getExpName();
				
				//name set means group all experiments by this name
				if(jobDef.isUserDefined()){
					experimentName = jobDef.getName();
				}
				
				
				if(hasChanged && !changedJobs.containsKey(experimentName)){
					changedJobs.put(experimentName, job);
				}
				
				if(jobDef.isUserDefined()){
					break;
				}
			}
		}
		
		return changedJobs;
	}
	
	/**
	 * Write trial.xml files every experiment found using the jobDef as the template xml.
	 * 
	 * @param jobDef -  Template xml that trial.xml's are built from
	 * @param changedJobs - Collection of trials that need files written
	 * @return 
	 */
	private List<Trial> saveJobs(JobDefinitions jobDef, Map<String, JobDefinition> changedJobs){
		List<Trial> trials = new ArrayList<>();
		for(String jobName : changedJobs.keySet()){
			try{
				JobDefinition job = changedJobs.get(jobName);
				Trial trial = TrialAnalysisJobProcess.createTrial(jobDef, job, false);

				if(trial != null){
					trials.add(trial);
				}
			}catch(Exception e){
				log.error(jobName ,e);
			}
		}
		return trials;
	}
}