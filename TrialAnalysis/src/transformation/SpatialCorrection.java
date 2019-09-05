package transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import data.collection.ExpFBKs;
import data.xml.objects.Trial;

/**
 * Class to create the spline coefficients needed to run the spatial correction
 * model.
 * 
 * @author Scott Smith
 * 
 */
public class SpatialCorrection extends Transformation {

	private static Logger log = Logger.getLogger(SpatialCorrection.class.getName());

	protected ExpFBKs fbks;
	private Trial trial;
	protected List<Integer> rnPass = new ArrayList<Integer>();
	protected List<Integer> rnRange = new ArrayList<Integer>();
	private String type;

	private List<Double> passKnots;
	private List<Double> rangeKnots;
	private Map<Integer,Integer> maxPass;
	private Map<Integer,Integer> minPass;
	private Map<Integer,Integer> maxRange;
	private Map<Integer,Integer> minRange;
	
	public ExpFBKs getFbks() {
		return fbks;
	}

	public void setFbks(ExpFBKs fbks) {
		this.fbks = fbks;
	}

	public List<Integer> getRnPass() {
		return rnPass;
	}

	public void setRnPass(List<Integer> rnPass) {
		this.rnPass = rnPass;
	}

	public List<Integer> getRnRange() {
		return rnRange;
	}

	public void setRnRange(List<Integer> rnRange) {
		this.rnRange = rnRange;
	}

	public void run() {
		
	}
	
	public void setGlobals(){
		
		
	}

	protected void setType(String type) {
		this.type = type;
	}

	protected String getType() {
		return type;
	}

	public Trial getTrial() {
		return trial;
	}

	public void setTrial(Trial trial) {
		this.trial = trial;
	}
}
