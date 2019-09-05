package validate;

import org.apache.log4j.Logger;

import data.collection.ExpFBKs;
import data.xml.objects.Trial;
import utils.Funcs;

/**
 * Check each if there is only one hybrid in a block.
 * 
 * @author Scott Smith
 *
 */
public class HybridPerBlock implements Validate {

	private static Logger log = Logger.getLogger(HybridPerBlock.class.getName());
	private int threshold;
	
	public int validate(Trial trial) throws Exception {
		String genoType = trial.getDataLevel().get("genoType");
		String block = "blockId";
		threshold = threshold == 0 ? 2 : threshold; //default to one
				
		ExpFBKs fbks = trial.getFbks();

		//find all hybrid's only planted in one block
		String select = " SELECT " + Funcs.quoteString(genoType);
		String having = " HAVING COUNT(distinct " + Funcs.quoteString(block) + " ) < " + threshold + " ";
		String sql = fbks.createSql(select, "", " GROUP BY " + Funcs.quoteString(genoType), having);
		
		int rows = fbks.delete(sql, genoType);
		log.warn(String.format("Number of hybrids in only one block = %d", rows));
		return rows;
	}
}