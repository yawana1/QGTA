package asreml.input;


/**
 * Class used to define extra variance structures from blup analysis
 * 
 */
public class AsremlErrorVariance {

	private String rStructureAdj;
	private String errorVariance;
	
	public String getrStructureAdj() {
		return rStructureAdj;
	}

	public void setrStructureAdj(String rStructureAdj) {
		this.rStructureAdj = rStructureAdj;
	}

	public String getErrorVariance() {
		return errorVariance;
	}

	public void setErrorVariance(String errorVariance) {
		this.errorVariance = errorVariance;
	}

	@Override
	public String toString() {
		return (errorVariance + System.getProperty("line_separator")) + (rStructureAdj + System.getProperty("line_separator"));
	}

	public AsremlErrorVariance(String rStructureAdj, String  errorVariance) {
		this.rStructureAdj = rStructureAdj;
		this.errorVariance = errorVariance;
	}
}
