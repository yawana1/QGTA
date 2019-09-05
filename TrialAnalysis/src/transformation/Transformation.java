package transformation;

import data.xml.objects.Trial;

/**
 * 
 * 
 * @author Scott Smith
 * @param <T>
 * @param <T>
 *
 */
public abstract class Transformation implements Runnable{
	
	protected Trial trial;

	public Trial getTrial() {
		return trial;
	}

	public void setTrial(Trial trial) {
		this.trial = trial;
	}
}
