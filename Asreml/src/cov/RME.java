package cov;

public class RME {

	//bad naming so can directly serialize to be using in the RME.
	private String crop;
	private Boolean run_pedigree;
	private Boolean run_similarity;
	private Boolean run_matrix_operations;
	private Boolean run_gmatrix;
	private Boolean run_bend;
	private String geno_id_file_name;
	private String pedigree_file_name;
	private String similarity_file_name;
	private MatrixOptions matrix_options;
	
	public String getCrop() {
		return crop;
	}
	public void setCrop(String crop) {
		this.crop = crop;
	}
	public Boolean getRunPedigree() {
		return run_pedigree;
	}
	public void setRunPedigree(Boolean runPedigree) {
		this.run_pedigree = runPedigree;
	}
	public Boolean getRunSimilarity() {
		return run_similarity;
	}
	public void setRunSimilarity(Boolean runSimilarity) {
		this.run_similarity = runSimilarity;
	}
	public Boolean getRunMatrixOperations() {
		return run_matrix_operations;
	}
	public void setRunMatrixOperations(Boolean runMatrixOperations) {
		this.run_matrix_operations = runMatrixOperations;
	}
	public Boolean getRunGMatrix() {
		return run_gmatrix;
	}
	public void setRunGMatrix(Boolean runGMatrix) {
		this.run_gmatrix = runGMatrix;
	}
	public String getGenoIdFile() {
		return geno_id_file_name;
	}
	public void setGenoIdFile(String genoIdFile) {
		this.geno_id_file_name = genoIdFile;
	}
	public String getPedigreeFileName() {
		return pedigree_file_name;
	}
	public void setPedigreeFileName(String pedigreeFileName) {
		this.pedigree_file_name = pedigreeFileName;
	}
	public String getSimilarityFileName() {
		return similarity_file_name;
	}
	public void setSimilarityFileName(String similarityFileName) {
		this.similarity_file_name = similarityFileName;
	}
	public MatrixOptions getMatrixOptions() {
		return matrix_options;
	}
	public void setMatrixOptions(MatrixOptions matrixOptions) {
		this.matrix_options = matrixOptions;
	}
	public Boolean getRunBend() {
		return run_bend;
	}
	public void setRunBend(Boolean runBend) {
		this.run_bend = runBend;
	}
}
