package utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Product {

	/***
	 * Pass in a Map of lists ie.
	 * 			id1 = list of id1 values
	 * 			id2 = list of id2 values
	 * 			id3 = list of id3 values
	 * Get out the cartesian Product of all those values in a list of a Map of ids
	 * @param <K>
	 * @param <V>
	 * @param lists
	 * @return
	 */
	public static <K,V> List<LinkedHashMap<K,V>> cartesianProduct(LinkedHashMap<K, List<V>> lists) {
		return cartesianProduct(0, lists);
	}

	/***
	 * Perform recursive cartesian product on values passed in.
	 * @param <K>
	 * @param <V>
	 * @param index
	 * @param lists
	 * @return
	 */
	private static <K,V> List<LinkedHashMap<K, V>> cartesianProduct(int index, LinkedHashMap<K, List<V>> lists) {
		List<LinkedHashMap<K, V>> ret = new ArrayList<LinkedHashMap<K, V>>();
		if (index == lists.size()) {
			ret.add(new LinkedHashMap<K,V>());
		} else {
			for (V obj : lists.get(lists.keySet().toArray()[index])) {
				for (LinkedHashMap<K,V> list : cartesianProduct(index+1, lists)) {
					List<K> keys = new ArrayList<K>(lists.keySet());
					list.put(keys.get(index),obj);

					//reorder map to keep filter order correct.
					LinkedHashMap<K,V> reorder = new LinkedHashMap<K, V>();
					reorder.put(keys.get(index),obj);
					reorder.putAll(list);
					ret.add(reorder);
				}
			}
		}
		return ret;
	}
}
