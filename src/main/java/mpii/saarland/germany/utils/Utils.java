package com.mpii.saarland.germany.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 
 * @author Hai Dang Tran
 *
 */
public class Utils {

	public static void updateKeyString(Map<String, Set<String>> key2values, String key, String value, boolean add) {
		Set<String> values = key2values.get(key);
		if (values == null) {
			values = new HashSet<>();
		}
		if (add) {
			// Add value to values.
			values.add(value);
		} else {
			// Remove value from values.
			values.remove(value);
		}
		key2values.put(key, values);
	}

	public static void addKeyLong(Map<String, Long> key2Total, String key, long frequency) {
		long total = 0;
		if (key2Total.containsKey(key)) {
			total = key2Total.get(key);
		}
		total += frequency;
		key2Total.put(key, total);
	}

	public static <K extends Comparable<? super K>, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> key2Value) {
		Map<K, V> key2SortedValue = new TreeMap<K, V>(new Comparator<K>() {

			@Override
			public int compare(K key1, K key2) {
				V value1 = key2Value.get(key1);
				V value2 = key2Value.get(key2);
				if (value1.compareTo(value2) > 0) {
					return -1;
				}
				if (value1.compareTo(value2) < 0) {
					return 1;
				}
				return key1.compareTo(key2);
			}

		});
		key2SortedValue.putAll(key2Value);
		return key2SortedValue;
	}

	public static List<String> getTopK(Map<String, Long> key2Total, int k) {
		Map<String, Long> key2SortedTotal = sortByValue(key2Total);
		List<String> topResult = new ArrayList<>();
		int count = 0;
		for (String key : key2SortedTotal.keySet()) {
			++count;
			if (count > k) break;
			topResult.add(key + "\t" + key2Total.get(key));
		}
		return topResult;
	}

	public static Map<String, Set<String>> cloneMap(Map<String, Set<String>> key2Values) {
		Map<String, Set<String>> result = new HashMap<>();
		for (String key : key2Values.keySet()) {
			Set<String> values = key2Values.get(key);
			for (String value : values) {
				updateKeyString(result, key, value, true);
			}
		}
		return result;
	}

}
