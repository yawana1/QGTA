package main;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.google.common.base.Strings;

import data.TrialXML;
import data.XML;
import data.xml.objects.App;
import main.trialanalysis.TrialAnalysisJobDef;
import main.trialanalysis.TrialAnalysisJobProcess;

public class Main {
	
	private static Logger log;
	private final static String APP_CONFIG = "App.xml"; //default app config file
	private static Options options;

	public static void main(String[] args) {
		options = new Options();
		options.addOption("a", "archive", false, "Archive previous run of analysis");
		options.addOption("x", "config", true, "Path to application configuration file");
		options.addOption("j", "jobs", true, "Name of job xml to run");
		options.addOption("t", "type", true, "Run Type, =q queue to cluster, =r run existing trial.xml's =d run jobDef and analysis");
		options.addOption("u", "updated", false, "Retrieve only updated trials from Variety");
		options.addOption("c", "checksum", false, "Appy checksum filtering of trials to analyze");
		options.addOption("s", "schedule", false, "Observe schedule of jobDefs");
		options.addOption("h", "help", false, "Print usage of application");
		options.addOption("o", "old", false, "Use old version");
		options.addOption("e", "exps", true, "List of experiments to filter data by. [comma separated string list]");
		options.addOption("m", "mem", true, "Request heap space");
		options.addOption("f", "file", true, "Trial xml file");
		options.addOption("i", "omitHeatmap", false, "Omit inserting heatmap data to Variety");
		
		CommandLineParser parser = new DefaultParser();
		try{
			CommandLine cli = parser.parse(options, args);
			String config = cli.hasOption("config") ? cli.getOptionValue("config") : null;
			if(config != null){
				//load App Config defined by the command line
				if(!Files.isReadable(Paths.get(config))){
					System.err.println("App config passed in is not found or readable");
				}
			}
			else{
				//load default App Config on the classpath
				URL url = Thread.currentThread().getContextClassLoader().getResource(APP_CONFIG); 
				
				if(url == null){
					System.err.println("App.xml not on the classpath");
				}
				else{
					config = url.getFile();
				}
			}
			boolean useArchive = cli.hasOption("archive");
			boolean useUpdated = cli.hasOption("updated");
			boolean useChecksum = cli.hasOption("checksum");
			boolean useSchedule = cli.hasOption("schedule");
			boolean useOld = cli.hasOption("old");
			boolean omitHeatmap = cli.hasOption("omitHeatmap");
			String jobs = cli.hasOption("jobs") ? cli.getOptionValue("jobs") : null;
			String exps = cli.hasOption("exps") ? cli.getOptionValue("exps") : null;
			String runType = cli.hasOption("type") ? cli.getOptionValue("type") : "";
			String file = cli.hasOption("file") ? cli.getOptionValue("file") : "";
			
			boolean useQueue = false;
			if(runType.equals("q")){
				useQueue = true;
			}
			
			if(cli.hasOption("help")){
				usage();
				System.exit(0);
			}
			
			if(config == null){
				System.err.println("App config file not found");
			}
			else{
				XML.INSTANCE = TrialXML.INSTANCE;
				XML.INSTANCE.deserialize(config, App.INSTANCE);
				setLogger("ALL", "ALL", false);
				log = Logger.getLogger(Main.class.getName()); //load logger after configured
				
				TrialAnalysisJobDef jobDef = null;
				//run jobs
				if(runType.equals("r")){
					//setup the logger
					//since file is passed in run this trial.xml
					if(!Strings.isNullOrEmpty(file)){
						String[] name = file.split("/");
						setLogger(name[name.length-1].split("\\.")[0], name[name.length-1].split("\\.")[1],file.contains("step2.xml"));
						log = Logger.getLogger(Main.class.getName()); //load logger after configured
						new TrialAnalysisJobProcess(useArchive, args).start(file, false);
					}
					//no file specified so queue all files in the models folder
					else{
						new TrialAnalysisJobProcess(useArchive, args).run(true);
					}
				}
				//queue trial.xml to run
				else if(useQueue && !Strings.isNullOrEmpty(file)){
					new TrialAnalysisJobProcess(useArchive, args).start(file, useQueue);
				}
				else{
					log = Logger.getLogger(Main.class.getName()); //load logger after configured
					
					if(useOld){
						jobDef = new TrialAnalysisJobDef(true, exps, App.INSTANCE.getJobDefDirectory(), args);
					}
					else{
						jobDef = new TrialAnalysisJobDef(useArchive, useQueue, useUpdated, useChecksum, useSchedule, omitHeatmap, exps, jobs, App.INSTANCE.getJobDefDirectory(), args);
					}
					//new TrialAnalysisJobProcess(useArchive).start(file);
					jobDef.run();
				}
			}
		}
		catch(Exception e){
			if(log != null){
				log.fatal("", e);
			}
			System.err.println("Parsing command line arguments failed.\nReason: "+e.getMessage());
			usage();
			System.exit(2);
		}
	}

	private static void usage(){
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Help", options);
	}
	
	public static void setLogger(String experiment, String season, boolean append) throws IOException{
		URL logFile = Thread.currentThread().getContextClassLoader().getResource("log4j.properties");
		System.setProperty("experimentId", experiment);
		System.setProperty("seasonId", season);
		System.setProperty("log.dir",App.INSTANCE.getLogDir());
		if(append){
			Properties properties = new Properties();
			properties.load(logFile.openStream());
			properties.setProperty("log4j.appender.file.Append","true");
			PropertyConfigurator.configure(properties);
		}
		else{
			PropertyConfigurator.configure(logFile);
		}
	}
}