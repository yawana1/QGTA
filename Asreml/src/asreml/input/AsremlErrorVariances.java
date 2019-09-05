package asreml.input;

/**
 * The Class AsremlErrorVariances is used to store a collection of {@link AsremlErrorVariance}
 * 
 * @author Scott Smith
 *
 */
public class AsremlErrorVariances {

	private AsremlErrorVariance errorVariance;

	public AsremlErrorVariance getErrorVariance() {
		return errorVariance;
	}

	public void setErrorVariance(AsremlErrorVariance errorVariance) {
		this.errorVariance = errorVariance;
	}

	@Override
	public String toString() {
		return errorVariance == null? null: errorVariance.toString();
	}
}
