/*
 * Using JRE 1.6.0_02
 * 
 * @package 	utils
 * @class 		TraitComparator.java
 * @author 		u409397
 * @modified 	Jul 8, 2010
 * 
 */
package utils;

import java.util.Comparator;

import data.xml.objects.Trait;

/**
 * The Class TraitComparator is used to compare two {@link Trait}s.
 */
public class TraitComparator implements Comparator<Trait> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Trait o1, Trait o2) {
		String n1 = o1 == null ? "" : o1.getName();
		String n2 = o2 == null ? "" : o2.getName();
		return n1.compareTo(n2);
	}

}
