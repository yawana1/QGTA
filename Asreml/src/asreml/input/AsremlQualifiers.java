package asreml.input;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The Class Qualifiers is used to store a collection of {@link AsremlQualifier}
 * s.
 */
public class AsremlQualifiers {

	/** The qualifiers. */
	private List<AsremlQualifier> qualifiers = new ArrayList<AsremlQualifier>();

	/**
	 * Adds the.
	 * 
	 * @param qualifier
	 *            the qualifier
	 */
	public void add(AsremlQualifier qualifier) {
		qualifiers.add(qualifier);
	}
	
	public AsremlQualifier get(String name){
		AsremlQualifier qualifier = null;
		int index = qualifiers.indexOf(new AsremlQualifier(name));
		if(index != -1){
			qualifier = qualifiers.get(index);
		}
		return qualifier;
	}

	@Override
	public String toString() {
		String str = "";
		for (Iterator<AsremlQualifier> cit = qualifiers.iterator(); cit.hasNext();) {
			str += cit.next();
		}
		return str;
	}
}
