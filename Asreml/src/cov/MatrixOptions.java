package cov;

import asreml.AsremlGlobals.GrmType;

public class MatrixOptions {
	private String operation_mode;
	private GrmType analysis_mode;
	private String output_matrix_list;
	private Boolean matrix_invert;
	private String data_type;
	private Boolean iterative;
	private Double startWeight;
	private Double thresholdWeight;
	private Double thresholdCondition;
	private Boolean removeNegative;
	private Boolean reduction;
	private String matrix_format;
	private String matrix_suffix;
	private Boolean DevMode;
	
	public String getOperationMode() {
		return operation_mode;
	}
	public void setOperationMode(String operationMode) {
		this.operation_mode = operationMode;
	}
	public GrmType getAnalysisMode() {
		return analysis_mode;
	}
	public void setAnalysisMode(GrmType analysisMode) {
		this.analysis_mode = analysisMode;
	}
	public String getOutputMatrixList() {
		return output_matrix_list;
	}
	public void setOutputMatrixList(String outputMatrixList) {
		this.output_matrix_list = outputMatrixList;
	}
	public Boolean getMatrixInvert() {
		return matrix_invert;
	}
	public void setMatrixInvert(Boolean matrixInvert) {
		this.matrix_invert = matrixInvert;
	}
	public Boolean getReduction() {
		return reduction;
	}
	public void setReduction(Boolean reduction) {
		this.reduction = reduction;
	}
	public String getMatrixFormat() {
		return matrix_format;
	}
	public void setMatrixFormat(String matrixFormat) {
		this.matrix_format = matrixFormat;
	}
	public String getMatrixSuffix() {
		return matrix_suffix;
	}
	public void setMatrixSuffix(String matrixSuffix) {
		this.matrix_suffix = matrixSuffix;
	}
	public Boolean getIterative() {
		return iterative;
	}
	public void setIterative(Boolean iterative) {
		this.iterative = iterative;
	}
	public Double getStartWeight() {
		return startWeight;
	}
	public void setStartWeight(Double startWeight) {
		this.startWeight = startWeight;
	}
	public Boolean getRemoveNegative() {
		return removeNegative;
	}
	public void setRemoveNegative(Boolean removeNegative) {
		this.removeNegative = removeNegative;
	}
	public Double getThresholdWeight() {
		return thresholdWeight;
	}
	public void setThresholdWeight(Double thresholdWeight) {
		this.thresholdWeight = thresholdWeight;
	}
	public Double getThresholdCondition() {
		return thresholdCondition;
	}
	public void setThresholdCondition(Double thresholdCondition) {
		this.thresholdCondition = thresholdCondition;
	}
	public Boolean getDevMode() {
		return DevMode;
	}
	public void setDevMode(Boolean devMode) {
		DevMode = devMode;
	}
	public String getDataType() {
		return data_type;
	}
	public void setDataType(String dataType) {
		this.data_type = dataType;
	}
}
