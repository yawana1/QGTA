package main.trialanalysis;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import asreml.Asreml;
import asreml.Asreml.RunAsremlException;
import asreml.AsremlAsd;
import asreml.Conversion;
import asreml.input.AsremlColumn;
import asreml.input.AsremlColumns;
import asreml.input.AsremlGrm;
import asreml.input.AsremlModel;
import data.XML;
import data.collection.DataProcesser;
import data.collection.ExpFBKs;
import data.collection.TrialData;
import data.collection.ValueMatrix;
import data.xml.objects.Analysis;
import data.xml.objects.App;
import data.xml.objects.Constants;
import data.xml.objects.StatModel;
import data.xml.objects.Trial;
import error.ErrorMessage;
import report.Archive;
import report.Output;
import report.ReportOutput;
import report.ReportOutputs;
import stats.CalcStats;
import stats.Tabulate;
import transformation.Transformation;
import utils.Funcs;
import utils.Globals.TrialType;
import utils.Parallel;
import validate.IndexColumn;
import validate.Validate;
import validation.ValidationEngine;
import vat.Vat;

/***
 * The base class for running an analysis.  Defines the sequence of events
 * for analysis
 * 
 * @author Scott Smith
 *
 */

public class TrialProcessor {

	static Logger log = Logger.getLogger(TrialProcessor.class.getName());

	private Trial trial;
	
	
	public TrialProcessor(Trial trial){
		this.trial = trial;
	}
	
	/***
	 * Main method that defines the sequence of events for an analysis
	 * 
	 * @return - Status of success or failure
	 */
	public boolean run(){
		boolean result = false;
		ExpFBKs fbks = null;
		
		try {
			long time = System.currentTimeMillis();
			log.info("MEM - START = " + Funcs.getUsedMemory());
			
			if(trial==null){
				log.error("Trial " + ErrorMessage.INSTANCE.getMessage("null_value"));
			}
			else{
				int executionStep = 0;

				//if not set run all steps
				if(trial.getExecutionStepEnd() == 0 && trial.getExecutionStepStart() == 0){
					trial.setExecutionStepEnd(Integer.MAX_VALUE);
				}
				
				//load data
				if(trial.getExecutionStepEnd() > executionStep
					&& trial.getExecutionStepStart() <= executionStep){
					TrialData data = loadData(); // TEK load data from DB or flat file
					if(data==null || data.isEmpty()){
						log.warn("No data to run - " + trial.getTrialName());
						return true;
					}
					long timeVariety = System.currentTimeMillis();
					log.warn("TIME - DATA = " + (timeVariety - time));
					log.info("MEM - DATA = " + Funcs.getUsedMemory());
					
					//load fbk object with loaded data
					// TEK load data into hsql db
					fbks = createFbks(data);
					if(!fbks.isDataLoaded()){
						throw new Exception("ExpFbk not created");
					}
	
					fbks.setTraits(trial.getTraits());
					trial.setFbks(fbks);
					long timeFBK = System.currentTimeMillis();
					log.warn("TIME - DATA_PROCESSING = " + (timeFBK - timeVariety));
					log.info("MEM - DATA_PROCESSING = " + Funcs.getUsedMemory());
					time = timeFBK;
				}
				else{
					trial.getFbks().loadData(trial);
				}
				
				trial.setExecutionStep(++executionStep);
				
				//validate
				if(trial.getExecutionStepEnd() > executionStep
						&& trial.getExecutionStepStart() <= executionStep){
					validateData();
					long timeValidate = System.currentTimeMillis();
					log.warn("TIME - VALIDATION = " + (timeValidate - time));
					time = timeValidate;
					
					createColMap();
					
					//set asreml column levels
					boolean success = setColumnLevels(fbks);
					if(!success){
						throw new Exception("Asreml Column levels not set");
					}
				}
				trial.setExecutionStep(++executionStep);
				
				//Transformation
				if(trial.getExecutionStepEnd() > executionStep
						&& trial.getExecutionStepStart() <= executionStep){
					runTransformations();
					long timeTranformations = System.currentTimeMillis();
					log.warn("TIME - TRANSFORMATIONS = " + (timeTranformations - time));
					log.info("MEM - TRANSFORMATIONS = " + Funcs.getUsedMemory());
					time = timeTranformations;
				}
				trial.setExecutionStep(++executionStep);
				
				AsremlAsd asd = null;
				if(trial.getExecutionStepEnd() > executionStep
						&& trial.getExecutionStepStart() <= executionStep){
					asd = createAsd();
					if(asd == null){
						throw new Exception("Asd file creation error");
					}
					log.info("MEM - CREATE ASD = " + Funcs.getUsedMemory());
				}
				trial.setExecutionStep(++executionStep);
				
				if(trial.getExecutionStepEnd() > executionStep
						&& trial.getExecutionStepStart() <= executionStep){
					executeAnalysis(asd);
					long timeAnalysis = System.currentTimeMillis();
					log.warn("TIME - EXECUTE_ANALYSIS = " + (timeAnalysis - time));
					log.info("MEM - EXECUTE_ANALYSIS = " + Funcs.getUsedMemory());
					time = timeAnalysis;
				}
				trial.setExecutionStep(++executionStep);
			    
				if(trial.getExecutionStepEnd() > executionStep
						&& trial.getExecutionStepStart() <= executionStep){
					createOutput();
					long timeOutput = System.currentTimeMillis();
					log.warn("TIME - REPORTS = " + (timeOutput - time));
					log.info("MEM - REPORTS = " + Funcs.getUsedMemory());
					time = timeOutput;
				
					try{
						String[] args = new String[2];
						String previousTrial = findLastArchive(trial);
						Path previousTrialReport = Paths.get(findLastArchive(trial), "reports");
						args[0] = "-currentReportsDirectory=" + trial.getReportDirectory(false);
						if(!Strings.isNullOrEmpty(previousTrial) && Files.exists(Paths.get(previousTrial)) && Files.exists(previousTrialReport)){
							args[1] = "-previousReportsDirectory=" + previousTrialReport;
							new ValidationEngine(args, false).run();
						}
					}
					catch(Exception e){
						log.warn("Validation Engine", e);
					}
					long timeValidationEngine = System.currentTimeMillis();
					log.warn("TIME - VALIDATION - ENGINE = " + (timeValidationEngine - timeOutput));
				}
				trial.setExecutionStep(++executionStep);
				
				result = true; //Success
			}
		}
		catch(Exception e){
			log.error("", e);
		}
		finally{
			if(fbks  != null){
				fbks.shutdown();
			}
		}
		return result;
	}

	private static String findLastArchive(Trial trial) throws IOException{
		Path dir = Paths.get(trial.getTrialDirectory(), Archive.getBaseFolderName());
		
		String lastArchive = "";
		if(Files.exists(dir)){
			try(DirectoryStream<Path> ds = Files.newDirectoryStream(dir)){
		    	for(Path file : ds){
		    		if(lastArchive.compareTo(file.toString()) < 0){
		    			lastArchive = file.toString();
		    		}
		    	}
	    	}
		}
		
		return lastArchive;
	}
	
	/***
	 * Data read either from a database or from a specified flat file.
	 * 
	 * @return - List of row's of data.
	 */
	public TrialData loadData(){
		TrialData data = null;
		
		if(trial.useValuesFile()){
			try {
				data = new ValueMatrix(trial.getColumnNames()).load(trial.getTrialWorkDirectory() + "/" + trial.getValuesFile());
			} catch (IOException e) {
				log.error("", e);
			}
		}
		else{
			data = trial.getRetrieveData().execute(trial);
		}
		
		if(data != null){
			//if data loaded from the file don't write the same file
			if(!trial.useValuesFile()){
				//write values.asd file
				AsremlAsd.createValuesFile(trial, data.getData());
			}
			
			//add columns that will be used as boolean
			AsremlColumns asremlColumns	= trial.getColumns();
			List<String> booleanColumns = asremlColumns.getColumnsBoolean();
			data.getBooleanColunns().addAll(booleanColumns);
			
			//add indexes for the trial data levels
			//largest to smallest
			List<String> indexColumns = ExpFBKs.getIndexColumns(trial);
			data.setIndexColumns(indexColumns);
			
			if(data.isEmpty()){
				log.warn("Experiment data " + ErrorMessage.INSTANCE.getMessage("empty"));
			}
		}
	
		return data;
	}
	
	/**
	 * Initalize Fbk with data
	 * @param list
	 * @return
	 */
	public ExpFBKs createFbks(TrialData trialData){
		ExpFBKs fbk = null;
		try{
			fbk = new ExpFBKs();
			fbk.setBooleanColumns(trialData.getBooleanColunns());
			fbk.loadData(trialData);
			
			trialData = null; //no more need for trialData
		}
		catch(Exception e){
			log.error("", e);
		}
		
		return fbk;
	}
	
	/**
	 * Create colMap and write out values file
	 * @param trialData
	 */
	public void createColMap(){
		new DataProcesser(trial.getFbks(), trial.getDataLevel()).processData(); //create index maps.
	}
	
	/**
	 * If any one the validation classes specified by the trial failure throw and exception and stop the analaysis
	 * 
	 * @throws Exception 
	 * 
	 */
	public void validateData() throws Exception{
		//add default validations for every trial
		List<Validate> validators = new ArrayList<>();
		validators.add(new IndexColumn());
		
		//add validations specified by the trial.
		List<Validate> trialValidator = trial.getValidators();
		if(trialValidator != null){
			validators.addAll(trialValidator);	
		}

		if(validators != null){
			for(Validate validate : validators){
				validate.validate(trial);
			}
		}
	}
	
	/**
	 * Set Asreml column levels from the colMap
	 * 
	 * @param fbks
	 * @return - Flag completed successfully
	 */
	public boolean setColumnLevels(ExpFBKs fbks){
		boolean result = false;
		
		AsremlColumns columns = trial.getColumns();
		if(columns == null || columns.getColumns() == null){
			log.error("Asreml Columns on the Trial" + ErrorMessage.INSTANCE.getMessage("null_value"));
		}
		else{		
			for(AsremlColumn column : columns.getColumns()){
				if(!column.isCoeff()){
					String name = column.getName();
					List<Object> list = fbks.getColMap(name);
					if(list == null){
						log.error("No entry in the colMap for " + name);
						log.error("check that there is a sql column for this asreml column " + name);
						result = false;
						break;
					}
					else{
						Integer level = list.size();
						column.setLevel(level);
					}
					result = true;
				}
			}
		}
		return result;
	}
	
	/**
	 * Run a data Transformation classes defined for this trial in parallel.
	 * If only one transformation no need to thread.
	 */
	public void runTransformations(){
		//setup each transformation as a Runnable task
		List<Transformation> transformations = trial.getTransformations();		
		Collection<Runnable> tasks = new ArrayList<>();
		
		if(transformations != null && transformations.size() > 0){
			for(Transformation transformation: transformations){
				transformation.setTrial(trial);
				tasks.add(transformation);
			}
			
			//if one transformation no need to thread
			if(tasks.size() == 1){
				transformations.get(0).run();
			}
			else{
				//run all transformations, block till until whole list is completed
				Parallel parallel = new Parallel(trial.getConcurrentProcessMax());
				parallel.run(tasks);
			}
		}		
	}
	
	public AsremlAsd createAsd(){
		AsremlAsd asd;
		try {
			asd = new AsremlAsd(trial, false);
			asd.createAsd();
		} catch (Exception e) {
			log.error("Writing asd file");
			asd = null;
		}
		return asd;
	}
	
	/***
	 * Load AsremlModel object from asreml xml template.
	 * Write .as files and run.  Construct the Tabulate records outside of Asreml
	 * @param asd
	 * @return
	 * @throws RunAsremlException - Halts entire program
	 */
	public void executeAnalysis(AsremlAsd asd) throws Exception{
		//Multi trial types use the himem cluster queue
		String asCmd = trial.getType().equals(TrialType.SINGLE) ? Constants.INSTANCE.getConstant("asCmd").toString() : Constants.INSTANCE.getConstant("asCmdHimem").toString();
	
		int threadPoolSize = trial.getConcurrentProcessMax() * 2;
		Asreml asreml = new Asreml(false, asCmd, threadPoolSize);
			
		for(Analysis analysis: trial.getAnalyses()){
			StatModel statModel = analysis.getModel();
				
			if(statModel == null){
				String error = "Analysis stat model is blank";
				log.error(error);
				throw new Exception(error);
			}
			else if(statModel.isValid()){
				if(analysis.getAsremlModel() == null){
					//create AsremlModel object for each analysis that needs to be run
					Path modelFile = Paths.get(trial.getTrialWorkDirectory(), statModel.getName());
					AsremlModel asremlModel = new AsremlModel(trial.getColumns(), asd.getAsd(), modelFile, Conversion.trait(analysis.getTraits()));
			
					//add xml AsremlModel
					try {
						XML.INSTANCE.deserialize(statModel.getFile(), asremlModel, trial.getDataLevel());
						asremlModel.validateModelFile();
					} catch (Exception e) {
						log.warn(analysis.getModel().getFile() + " Failed to load", e);
					}
						
					//update grm directory since xml has only the file without the full path
					if(null != asremlModel.getGrms() && !asremlModel.getGrms().isEmpty()){
						for(AsremlGrm grm : asremlModel.getGrms().getGrms()){
							grm.setGrmFile(trial.getTrialWorkDirectory() + "/" + grm.getGrmFile());
						}
					}

					analysis.setAsremlModel(asremlModel);
				}
				asreml.addModel(analysis.getAsremlModel());
			}
		}
		
		//write asreml file
		asreml.writeScript();
		
		if(trial.getExecutionStepStart() < trial.getExecutionStep()){
	
			//run the Asreml jobs and stop
			if(trial.getExecutionStepEnd() == trial.getExecutionStep()+1){
				//Just run asreml and stop 
				asreml.runScript();
				Serializer.INSTANCE.checkpoint(trial, trial.getTrialWorkDirectory());
				return;
			}
			else{
				//run and load 
				asreml.runScriptLoadAsremlOutputs();
			}
		}	
		else if(trial.getExecutionStepStart() == trial.getExecutionStep()){
			asreml.loadAsremlOutupts();
		}

		log.info("MEM - ASReml = " + Funcs.getUsedMemory());
		
		//create tabs add to the output
		List<AsremlModel> models = asreml.getModels();
		ExpFBKs fbks = trial.getFbks();

		Tabulate.createTabs(models, fbks, threadPoolSize);
		log.info("MEM - Tabs = " + Funcs.getUsedMemory());
	}
	
	private ReportOutput calcStatistics() {
		ReportOutputs reportOutputs = new ReportOutputs();
		//run for each model/trait/summary ie anove/yield/entrySummary
		ReportOutput reportOutput = CalcStats.calcStats(reportOutputs, trial);

		return reportOutput;
	}
	
	private void createOutput(){
		//calc statistics for each Model
		ReportOutput reportOutput = calcStatistics();

		//create Vat output
		Vat vat = new Vat();
		XML.INSTANCE.deserialize(App.INSTANCE.getPropertiesDirectory() + "/Vat.xml", vat, trial.getDataLevel());
		
		//create txt file 
		reportOutput.export(trial,vat); 
		
		/*if (trial.isDoHeatMap()) {
			Path heatmapDir = Paths.get(App.INSTANCE.getHeatmapDirectory());
			if (!Files.isDirectory(heatmapDir)) {
				Files.createDirectories(heatmapDir);
			}
			
			String cmd = String.format("%s/heatimage.sh %s %s %s" 
					,App.INSTANCE.getRunScriptDirectory()
					,trial.getReportDirectory(false)
					,App.INSTANCE.getHeatmapDirectory()
					,String.format("%s.%s.csv",trial.getTrialName(),trial.getSeasonName())
					);
			SystemEx.run(cmd);
		}*/		
		//run output to vat and/or excel
		runOutputs(vat, reportOutput);
	}
	
	/**
	 * Run all the outputs defined by the trial.
	 * @param vat
	 * @param reportOutput
	 * @see Output
	 */
	public void runOutputs(Vat vat, ReportOutput reportOutput){
		if(trial.getOutputs()!=null){
			for(Output output: trial.getOutputs()){
				output.runOutput(vat, trial, reportOutput);
			}
		}else{
			log.warn("no outputs were found on the trial");
		}
		
	}
}