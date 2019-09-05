/*
 * Using JRE 1.6.0_02
 * 
 * @package 	utils
 * @class 		EntrySummaryComparator.java
 * @author 		u409397
 * @modified 	Jul 8, 2010
 * 
 */
package stats;

import java.util.Comparator;

import report.Summary;

// TODO: Auto-generated Javadoc
/**
 * The Class SummaryComparator is used to compare two {@link Summary by a selected value}.
 */
public class SummaryComparator implements Comparator<Summary>{

	/** The direction of comparison, ascending or descending. */
	private int dir;
	private String compareValue;
	
	/**
	 * Instantiates a new entry summary comparator.
	 * 
	 * @param dir
	 *            the direction of comparison
	 */
	public SummaryComparator(int dir, String compareValue){
		this.dir = dir;
		this.compareValue = compareValue;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@SuppressWarnings({ "unchecked", "null" })
	@Override
	public int compare(Summary o1, Summary o2) {
		int compare = 0;
		
		Object entry1 = null;
		entry1 = o1.getValues().get(compareValue);
		Object entry2 = null;
		entry2 = o2.getValues().get(compareValue);

		if(entry1 == null && entry2 != null){
			compare = -1;
		}
		else if (entry1 != null && entry2 == null) {
			compare = 1;
		}
		else if(entry1 == null && entry2 == null){
			compare = 0;
		}
		else{
			compare = ((Comparable<Object>)entry1).compareTo(entry2);
		}
		return dir * compare;
	}
}