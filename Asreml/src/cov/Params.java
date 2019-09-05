package cov;

public class Params 
{
	String genoIdFileName;
	String pedigreeFileName;
	String markerMatrixName;
	ANALYSIS_TYPES anType;
	
	public String getGenoIdFileName() {
		return genoIdFileName;
	}

	public String getPedigreeFileName() {
		return pedigreeFileName;
	}

	public String getMarkerMatrixName() {
		return markerMatrixName;
	}

	public int getAnType() {
		int anType;
		switch(this.anType)
		{
		case inbred:
			anType = 1;
			break;
		case hybrid:
			anType = 2;
			break;
		case forward:
			anType = 4;
			break;
		default:
			anType = -1;
		}
		return anType;
	}
	
	/**
	 * 
	 * @param g - id file
	 * @param p - pedigree file
	 * @param m - similarity file
	 * @param anType
	 */
	Params(String g, String p, String m, ANALYSIS_TYPES anType) 
	{
		genoIdFileName = g;
		pedigreeFileName = p;
		markerMatrixName = m;
		this.anType = anType;
	}
}

