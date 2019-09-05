package validate;

import java.util.Collection;

import org.apache.log4j.Logger;

import utils.Funcs;
import data.collection.ExpFBKs;
import data.xml.objects.Trait;
import data.xml.objects.Trial;

/**
 * Set any trait data outside stddev of threshold to null.
 * 
 * @author Scott Smith
 *
 */
public class StdDevOutliers implements Validate {
	private int threshold;
	private static Logger log = Logger.getLogger(StdDevOutliers.class.getName());
	
	public int validate(Trial trial) throws Exception {
		Collection<Trait> traits = trial.getTraits();
		ExpFBKs fbks = trial.getFbks();
		threshold = threshold == 0 ? 3 : threshold;
		int result = 0;
		for(Trait trait : traits ){
			//set null any column data outside stddev
			
			//get mean and std
			String name = Funcs.quoteString(trait.getName());
			double mean =  Double.parseDouble(fbks.get(" SELECT avg(cast("+name+" as double)) as \"rawMean\"", "","").get(0).get("rawMean").toString());
			double stddev =  Double.parseDouble(fbks.get(" SELECT ISNULL(stddev_samp(cast("+name+" as double)), 0) AS \"stdev\"", "","").get(0).get("stdev").toString());
			
			String select = " UPDATE EXP_FBK SET :trait = NULL ";
			String where = " WHERE cast(:trait AS DOUBLE) < " + String.format("%.3f", (mean - stddev*threshold)) + " OR cast(:trait AS DOUBLE) > " + String.format("%.3f", (mean + stddev*threshold));
			String sql = select + where;
			sql = sql.replace(":trait", Funcs.quoteString(trait.getName()));
			int rows = fbks.update(sql);
			result += rows;
			log.warn(String.format("Number of removed values for %s = %d", trait.getName(), rows));
		}
		return result;
	}
}