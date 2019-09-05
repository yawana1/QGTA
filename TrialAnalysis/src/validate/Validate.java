package validate;

import data.xml.objects.Trial;

/* Interface that takes a Trial with full data and can be used to run any validation needed.
 * 
 * @author Scott Smith
 */
public interface Validate {

	public int validate(Trial trial) throws Exception;
}
