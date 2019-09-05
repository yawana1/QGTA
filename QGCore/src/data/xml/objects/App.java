package data.xml.objects;

public class App {

	public final static App INSTANCE = new App();
	
	private String logDir;
	private String reportDirectory;
	private String winDirectory;
	private String failedDirectory;
	private String workDirectory;
	private String modelsDirectory;
	private String runningDirectory;
	private String propertiesDirectory;
	private String sqlDirectory;
	private String jobDefDirectory;
	private String asremlDirectory;
	private String runScriptDirectory;
	private String heatmapDirectory;
	
	public String getHeatmapDirectory() {
		return heatmapDirectory;
	}
	public void setHeatmapDirectory(String heatmapDirectory) {
		this.heatmapDirectory = heatmapDirectory;
	}
	public String getLogDir() {
		return logDir;
	}
	public void setLogDir(String logDir) {
		this.logDir = logDir;
	}
	public String getReportDirectory() {
		return reportDirectory;
	}
	public void setReportDirectory(String reportDirectory) {
		this.reportDirectory = reportDirectory;
	}
	public String getWinDirectory() {
		return winDirectory;
	}
	public void setWinDirectory(String winDirectory) {
		this.winDirectory = winDirectory;
	}
	public String getFailedDirectory() {
		return failedDirectory;
	}
	public void setFailedDirectory(String failedDirectory) {
		this.failedDirectory = failedDirectory;
	}
	public String getWorkDirectory() {
		return workDirectory;
	}
	public void setWorkDirectory(String workDirectory) {
		this.workDirectory = workDirectory;
	}
	public String getModelsDirectory() {
		return modelsDirectory;
	}
	public void setRunningDirectory(String directory) {
		this.runningDirectory = directory;
	}
	public String getRunningDirectory() {
		return runningDirectory;
	}
	public void setModelsDirectory(String modelsDirectory) {
		this.modelsDirectory = modelsDirectory;
	}
	public String getPropertiesDirectory() {
		return propertiesDirectory;
	}
	public void setPropertiesDirectory(String propertiesDirectory) {
		this.propertiesDirectory = propertiesDirectory;
	}	
	public String getSqlDirectory() {
		return sqlDirectory;
	}
	public void setSqlDirectory(String sqlDirectory) {
		this.sqlDirectory = sqlDirectory;
	}	
	public String getJobDefDirectory() {
		return jobDefDirectory;
	}
	public void setJobDefDirectory(String jobDefDirectory) {
		this.jobDefDirectory = jobDefDirectory;
	}
	public String getAsremlDirectory() {
		return asremlDirectory;
	}
	public void setAsremlDirectory(String asremlDirectory) {
		this.asremlDirectory = asremlDirectory;
	}
	public String getRunScriptDirectory() {
		return runScriptDirectory;
	}
	public void setRunScriptDirectory(String runScriptDirectory) {
		this.runScriptDirectory = runScriptDirectory;
	}
	
	private App(){}
}