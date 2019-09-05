package data.collection;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * A Job Definition defines the criteria that are used to group a set of trials together.
 * 
 * @author Scott Smith
 *
 */
public class JobDefinition {

	static Logger log = Logger.getLogger(JobDefinition.class.getName());
	
	private Integer expId;
	private String expName;
	private String projectName;
	private Integer seasonId;
	private String seasonName;
	private Integer classId;
	private String className;
	private String analysisStage;
	private Date analysisDate;
	private Date analysisDateChanged;
	private Integer analysisPeopleId;
	private Integer analysisState;
	private boolean multi;

	public boolean isMulti() {
		return multi;
	}
	public void setMulti(boolean multi) {
		this.multi = multi;
	}
	public Integer getExpId() {
		return expId;
	}
	public void setExpId(Integer expId) {
		this.expId = expId;
	}
	public String getExpName() {
		return expName;
	}
	public void setExpName(String expName) {
		this.expName = expName;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public Integer getSeasonId() {
		return seasonId;
	}
	public void setSeasonId(Integer seasonId) {
		this.seasonId = seasonId;
	}
	public String getSeasonName() {
		return seasonName;
	}
	public void setSeasonName(String seasonName) {
		this.seasonName = seasonName;
	}
	public Integer getClassId() {
		return classId;
	}
	public void setClassId(Integer classId) {
		this.classId = classId;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getAnalysisStage() {
		return analysisStage;
	}
	public void setAnalysisStage(String analysisStage) {
		this.analysisStage = analysisStage;
	}
	public Date getAnalysisDate() {
		return analysisDate;
	}
	public void setAnalysisDate(Date analysisDate) {
		this.analysisDate = analysisDate;
	}
	public Date getAnalysisDateChanged() {
		return analysisDateChanged;
	}
	public void setAnalysisDateChanged(Date analysisDateChanged) {
		this.analysisDateChanged = analysisDateChanged;
	}
	public Integer getAnalysisPeopleId() {
		return analysisPeopleId;
	}
	public void setAnalysisPeopleId(Integer analysisPeopleId) {
		this.analysisPeopleId = analysisPeopleId;
	}
	public Integer getAnalysisState() {
		return analysisState;
	}
	public void setAnalysisState(Integer analysisState) {
		this.analysisState = analysisState;
	}

	public JobDefinition(){
		
	}
	
	public JobDefinition(Map<String, Object> map){
		try{
			this.expId 					= map.get("gId") == null ? null : Integer.parseInt(map.get("gId").toString());
			this.expName 				= map.get("groupName")==null ? null : map.get("groupName").toString();
			this.projectName 			= map.get("projectName")==null ? null : map.get("projectName").toString();
			this.seasonId 				= map.get("seasonId")==null ? null : Integer.parseInt(map.get("seasonId").toString());
			this.seasonName				= map.get("seasonName")==null ? null : map.get("seasonName").toString();
			this.classId				= map.get("classId")==null ? null : Integer.parseInt(map.get("classId").toString());
			this.className				= map.get("className")==null ? null : map.get("className").toString();
			this.analysisStage			= map.get("analysisStage")==null ? null : map.get("analysisStage").toString();
			this.analysisDate			= map.get("analysisDate")==null ? null : (Date) map.get("analysisDate");
			this.analysisDateChanged 	= map.get("analysisDateChanged")==null ? null : (Date) map.get("analysisDateChanged");
			this.analysisPeopleId 		= map.get("analysisPeopleId")==null ? null : Integer.parseInt(map.get("analysisPeopleId").toString());
			this.analysisState			= map.get("analysisState")==null ? null : Integer.parseInt(map.get("analysisState").toString());
			
			this.multi = !map.get("gId").equals(map.get("expId")) ? true : false ; 
		}catch(Exception e){
			log.error(this.getClass().getName()+".constructor :: "+e.getMessage());
		}
	}
}
