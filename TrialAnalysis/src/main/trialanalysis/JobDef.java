package main.trialanalysis;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import data.XML;
import data.xml.objects.App;
import db.Sessions;
import error.ErrorMessage;

public class JobDef {

	private static Logger log = Logger.getLogger(JobDef.class.getName());
	private final static String DELIM = ",";
	boolean useArchive;
	boolean useQueue;
	boolean useUpdated;
	boolean useChecksum;
	boolean useSchedule;
	private List<String> exps;
	private List<String> jobs;
	private String dirJobs;
	
	public JobDef(boolean useArchive, boolean useQueue, boolean useUpdated,
			boolean useChecksum, boolean useSchedule, String exps,
			String jobs, String dirJobs) {
		if(dirJobs == null){
			log.error("JobDef directory " + ErrorMessage.INSTANCE.getMessage("null_value"));
		}
		else{
			Path path = Paths.get(dirJobs);
			if(!Files.isReadable(path)){
				log.error(ErrorMessage.INSTANCE.getMessage("not_found_file") + " - " + dirJobs);
			}
			else{
				this.useArchive = useArchive;
				this.useQueue = useQueue;
				this.useUpdated = useUpdated;
				this.useChecksum = useChecksum;
				this.useSchedule = useSchedule;
				
				String[] lstExps = exps==null ? null : exps.split(DELIM);
				String[] lstJobs = jobs==null ? null : jobs.split(DELIM);
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
				
				this.dirJobs = dirJobs;
				loadProperties(); //load database connection
			}
		}
	}
	
	public void loadProperties(){
		XML.INSTANCE.deserialize(App.INSTANCE.getPropertiesDirectory()+"/Db.xml", Sessions.INSTANCE);
	}
}
