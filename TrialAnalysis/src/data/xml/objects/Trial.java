package data.xml.objects;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import report.Output;
import transformation.Transformation;
import utils.Globals.CheckType;
import utils.Globals.TrialType;
import validate.Validate;
import asreml.input.AsremlColumns;
import data.collection.ExpFBKs;
import db.modules.RetrieveData;
import db.modules.SQLBuilder;

/**
 * Base object that defines what data to run, how to analysis it, and what statistics to report.
 * @author Scott Smith
 *
 */
public class Trial {

	private Crop crop;
	private ExpFBKs fbks; //stores the data for this trial
	private String trialName;
	private List<Experiment> experiments; //Used by MultiYear xml deserialize cannot do Collections
	private List<String> experimentNames; //Used by Giant1C
	private TrialType type;
	private String seasonId;
	private String seasonName;
	private String projectName;
	private String analysisStage;
	private String className; //Type of experiments data collected  ie Yield, Silage etc.
	private Date analysisDate;
	private Date lastUpdated;
	private List<Integer>  analysisState;
	private List<Integer>  analysisZone;
	private Integer analysisPeopleId;
	private List<Trait> traits;
	private String sqlTemplateFile;
	private List<SqlColumn> sqlColumns;
	private AsremlColumns columns; //columns that will be used by Asreml
	private List<Analysis> analyses; //List of mixed models to be run on the data
	private List<Transformation> transformations;  //List of Transformations to perform on the data
	private String workingDirectory;
	private CheckType checkType;
	private String valuesFile;
	private DataLevelParams dataLevel;
	private String xlsColumnFile;
	private List<Output> outputs;
	private List<String> zones;
	private List<Integer> directions;
	private String region;
	private String expZone;
	private List<Validate> validators;
	private RetrieveData retrieveData = new SQLBuilder();
	private int concurrentProcessMax;
	private boolean doHeatMap = false;
	private String trialRestriction;
	private int executionStepStart;
	private int executionStepEnd;
	private int executionStep;

	public int getConcurrentProcessMax() {
		return concurrentProcessMax;
	}
	public void setConcurrentProcessMax(int concurrentProcessMax) {
		this.concurrentProcessMax = concurrentProcessMax;
	}
	public List<Output> getOutputs() {
		return outputs;
	}
	public void setOutputs(List<Output> outputs) {
		this.outputs = outputs;
	}
	public String getXlsColumnFile() {
		return xlsColumnFile;
	}
	public void setXlsColumnFile(String xlsColumnFile) {
		this.xlsColumnFile = xlsColumnFile;
	}
	public Crop getCrop() {
		return crop;
	}
	public void setCrop(Crop crop) {
		this.crop = crop;
	}
	public ExpFBKs getFbks() {
		return fbks;
	}
	public void setFbks(ExpFBKs fbks) {
		this.fbks = fbks;
	}
	public String getTrialName() {
		return trialName;
	}
	public void setTrialName(String trialName) {
		this.trialName = trialName;
	}
	public List<Experiment> getExperiments() {
		if(experiments != null && experiments.size() == 0){
			for(String name : getExperimentNames()){
				experiments.add(new Experiment(name, getSeasonName(), getSeasonId()));
			}
		}
		
		return experiments;
	}
	public void setExperiments(List<Experiment> experiments) {
		this.experiments = experiments;
	}
	public List<String> getExperimentNames() {
		if(TrialType.MULTI.equals(type)){
			if(null != experiments){
				for(Experiment experiment : experiments){
					if(experimentNames == null){
						experimentNames = new ArrayList<String>(experiments.size());
					}
					experimentNames.add(experiment.getName());
				}
			}
		}
		return experimentNames;
	}
	public void setExperimentNames(List<String> experimentNames) {
		this.experimentNames = experimentNames;
	}
	public TrialType getType() {
		return type;
	}
	public void setType(TrialType type) {
		this.type = type;
	}
	public String getSeasonId() {
		return seasonId;
	}
	public void setSeasonId(String seasonId) {
		this.seasonId = seasonId;
	}
	public String getSeasonName() {
		return seasonName;
	}
	public void setSeasonName(String seasonName) {
		this.seasonName = seasonName;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getAnalysisStage() {
		return analysisStage;
	}
	public void setAnalysisStage(String analysisStage) {
		this.analysisStage = analysisStage;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public Date getAnalysisDate() {
		return analysisDate;
	}
	public void setAnalysisDate(Date analysisDate) {
		this.analysisDate = analysisDate;
	}
	public Date getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	public List<Integer> getAnalysisState() {
		return analysisState;
	}
	public void setAnalysisState(List<Integer> analysisState) {
		this.analysisState = analysisState;
	}
	public List<Integer> getAnalysisZone() {
		return analysisZone;
	}
	public void setAnalysisZone(List<Integer> analysisZone) {
		this.analysisZone = analysisZone;
	}
	public Integer getAnalysisPeopleId() {
		return analysisPeopleId;
	}
	public void setAnalysisPeopleId(Integer analysisPeopleId) {
		this.analysisPeopleId = analysisPeopleId;
	}
	public List<Trait> getTraits() {
		return traits;
	}
	public void setTraits(List<Trait> traits) {
		this.traits = traits;
	}
	public List<SqlColumn> getSqlColumns() {
		return sqlColumns;
	}
	public void setSqlColumns(List<SqlColumn> sqlColumns) {
		this.sqlColumns = sqlColumns;
	}
	public List<Analysis> getAnalyses() {
		return analyses;
	}
	public void setAnalyses(List<Analysis> analyses) {
		this.analyses = analyses;
	}
	public void setSqlTemplateFile(String sqlTemplateFile) {
		this.sqlTemplateFile = sqlTemplateFile;
	}
	public String getSqlTemplateFile() {
		return sqlTemplateFile;
	}
	public AsremlColumns getColumns() {
		return columns;
	}
	public void setColumns(AsremlColumns columns) {
		this.columns = columns;
	}
	public List<Transformation> getTransformations() {
		return transformations;
	}
	public void setTransformations(List<Transformation> transformations) {
		this.transformations = transformations;
	}
	public String getWorkingDirectory() {
		return workingDirectory;
	}
	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}
	public Trial(String workingDirectory){
		this.workingDirectory = workingDirectory;
	}
	public Trial(){
		
	}
	public String getValuesFile() {
		return valuesFile;
	}
	public void setValuesFile(String valuesFile) {
		this.valuesFile = valuesFile;
	}	
	public CheckType getCheckType() {
		return checkType;
	}
	public void setCheckType(CheckType checkType) {
		this.checkType = checkType;
	}	
	public Map<String, String> getDataLevel() {
		return dataLevel.getDataLevelMap();
	}
	public void setDataLevel(Map<String, String> dataLevel) {
		this.dataLevel.setDataLevel(dataLevel);
	}
	public List<String> getZones() {
		return zones;
	}
	public void setZones(List<String> list) {
		this.zones = list;
	}
	public List<Integer> getDirections() {
		return directions;
	}
	public void setDirections(List<Integer> directions) {
		this.directions = directions;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getExpZone() {
		return expZone;
	}
	public void setExpZone(String expZone) {
		this.expZone = expZone;
	}
	public List<Validate> getValidators() {
		return validators;
	}
	public void setValidators(List<Validate> validators) {
		this.validators = validators;
	}
	public RetrieveData getRetrieveData() {
		return retrieveData;
	}
	public void setRetrieveData(RetrieveData retrieveData) {
		this.retrieveData = retrieveData;
	}
	public String getTrialRestriction() {
		return trialRestriction;
	}
	public void setTrialRestriction(String trialRestriction) {
		this.trialRestriction = trialRestriction;
	}
	/**
	 * Full path to trials directory
	 * @return
	 */
	public String getTrialWorkDirectory(){
		Path path = Paths.get(getTrialDirectory(), "work");
		return path.toString();
	}
	
	public String getTrialDirectory(){
		Path path = Paths.get(workingDirectory).resolve(getTrialRelative());
		return path.toString();
	}
	
	/**
	 * Relative trial path
	 * crop/class/season/project/trialName
	 * @return
	 */
	public Path getTrialRelative(){
		Path path = Paths.get(
				 				 crop.name()
				 				,cleanName(className)
				 				,seasonName
				 				,cleanName(projectName)
				 				,cleanName(trialName));

		return path;
	}
	
	public static String cleanName(String name){
		if(null != name){
			name = name.replace(":", "_");
			name = name.replace(" ","_");
			name = name.replace("\\","");
			name = name.replace("/","_");
			name = name.replace(",","_");
			name = name.replace("(", "");
			name = name.replace(")", "");
			name = name.replace("&", "");
		}
		return name;
	}
	
	public static Path getTrialXMLName(String dir, Trial trial){
		String trialName = Trial.cleanName(trial.getTrialName());
		return Paths.get(dir, trialName + "." + trial.getSeasonName()+".xml");
	}
	
	public String getReportDirectory(boolean win){
		String directory =  (win ? App.INSTANCE.getWinDirectory() : App.INSTANCE.getReportDirectory());
		
		if(!win){
			File expDir = new File(directory);
			if(!expDir.exists()){
				expDir.mkdirs();
			}
		}
		
		Path path = Paths.get(directory, getTrialRelative().toString(), "reports");
		return path.toString();
	}
	
	public String getHeatMapDirectory(boolean win) {
		String proj = projectName;
		proj = proj.replace(":", "_");
		proj = proj.replace(" ","_");
		proj = proj.replace("\\","");
		proj = proj.replace(",","_");
		String directory =  (win ? App.INSTANCE.getWinDirectory() : App.INSTANCE.getHeatmapDirectory());
		
		if(!win){
			File expDir = new File(directory);
			if(!expDir.exists()){
				expDir.mkdirs();
			}
		}
		return directory;
	}
	
	public String[] getColumnNames(){
		List<String> names = new ArrayList<String>();
		if(null != getTraits() && null != getSqlColumns()){
			for(Trait trait : getTraits()){
				names.add(trait.getName());
			}
			for(SqlColumn column : getSqlColumns()){
				names.add(column.getName());
			}
		}
		return names.toArray(new String[names.size()]);
	}
	
	public boolean useValuesFile() {
		boolean result = false;
		if(null != valuesFile && !valuesFile.isEmpty()){
			result = true;
		}
		return result;
	}
	
	public String getFileExtention(String fileType){
		String extension = null;
		if(Constants.INSTANCE.getConstant(fileType)!=null){
			extension = Constants.INSTANCE.getConstant(fileType).toString();
		}
		return extension;
	}
	public boolean isDoHeatMap() {
		return doHeatMap;
	}
	public void setDoHeatMap(boolean doHeatMap) {
		this.doHeatMap = doHeatMap;
	}
	public int getExecutionStepStart() {
		return executionStepStart;
	}
	public void setExecutionStepStart(int executionStep) {
		this.executionStepStart = executionStep;
	}
	public int getExecutionStepEnd() {
		return executionStepEnd;
	}
	public void setExecutionStepEnd(int executionStep) {
		this.executionStepEnd = executionStep;
	}
	public int getExecutionStep() {
		return executionStep;
	}
	public void setExecutionStep(int executionStep) {
		this.executionStep = executionStep;
	}
}