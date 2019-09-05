package data.xml.objects;

import java.util.List;
import java.util.Map;

public class JobDefinitions {

	private Schedule schedule;
	private Crop crop;
	private Map<Season, List<String>> experiments;
	private List<Integer> classId;
	private List<String> analysisStage;
	private List<Integer> analysisState;
	private List<Integer> randType;
	private List<String> restrictions;
	private String seasonName;
	private String seasonId;
	private String zone;
	private List<String> zones;
	private List<Integer> directions;
	private String region;
	private String sqlFile;
	private String trialFile;
	private String name;
	private String fileName;
	private String trialRestrictions;
	
	public boolean isUserDefined() {
		boolean result = false;
		
		if(name != null && (experiments != null || (zone != null && region != null))){
			result = true;
		}
		
		return result;
	}
	public Schedule getSchedule() {
		return schedule;
	}
	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}
	public Crop getCrop() {
		return crop;
	}
	public void setCrop(Crop crop) {
		this.crop = crop;
	}
	public List<Integer> getClassId() {
		return classId;
	}
	public void setClassId(List<Integer> classId) {
		this.classId = classId;
	}
	public List<String> getAnalysisStage() {
		return analysisStage;
	}
	public void setAnalysisStage(List<String> analysisStage) {
		this.analysisStage = analysisStage;
	}
	public List<Integer> getAnalysisState() {
		return analysisState;
	}
	public void setAnalysisState(List<Integer> analysisState) {
		this.analysisState = analysisState;
	}
	public List<Integer> getRandType() {
		return randType;
	}
	public void setRandType(List<Integer> randType) {
		this.randType = randType;
	}
	public List<String> getRestrictions() {
		return restrictions;
	}
	public void setRestrictions(List<String> restrictions) {
		this.restrictions = restrictions;
	}
	public String getSeasonName() {
		return seasonName;
	}
	public void setSeasonName(String seasonName) {
		this.seasonName = seasonName;
	}
	public String getSqlFile() {
		return sqlFile;
	}
	public void setSqlFile(String sqlFile) {
		this.sqlFile = sqlFile;
	}
	public String getTrialFile() {
		return trialFile;
	}
	public void setTrialFile(String trialFile) {
		this.trialFile = trialFile;
	}
	public Map<Season, List<String>> getExperiments() {
		return experiments;
	}
	public void put(Season season, String experimentName){
		if(experiments != null && experiments.containsKey(season)){
			List<String> list = experiments.get(season);
			list.add(experimentName);
		}
	}
	public void setExperiments(Map<Season, List<String>> experiments) {
		this.experiments = experiments;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getSeasonId() {
		return seasonId;
	}
	public void setSeasonId(String seasonId) {
		this.seasonId = seasonId;
	}
	public String getZone() {
		return zone;
	}
	public void setZone(String zone) {
		this.zone = zone;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public List<String> getZones() {
		return zones;
	}
	public void setZones(List<String> zones) {
		this.zones = zones;
	}
	public List<Integer> getDirections() {
		return directions;
	}
	public void setDirections(List<Integer> directions) {
		this.directions = directions;
	}
	public String getTrialRestrictions() {
		return trialRestrictions;
	}
	public void setTrialRestrictions(String trialRestrictions) {
		this.trialRestrictions = trialRestrictions;
	}
	public JobDefinitions(){
		
	}
}