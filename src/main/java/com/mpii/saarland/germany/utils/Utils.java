package com.mpii.saarland.germany.utils;

import java.util.ArrayList;
import java.util.Comparator;
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

	public static void addKeyString(Map<String, Set<String>> key2values, String key, String value) {
		Set<String> values = key2values.get(key);
		if (values == null) {
			values = new HashSet<>();
		}
		values.add(value);
		key2values.put(key, values);
	}

	public static void addKeyLong(Map<String, Long> key2Total, String key, long value) {
		long total = 0;
		if (key2Total.containsKey(key)) {
			total = key2Total.get(key);
		}
		total += value;
		key2Total.put(key, total);
	}

	public static List<String> getTopK(Map<String, Long> key2Total, int k) {
		Map<String, Long> key2SortedTotal = new TreeMap<String, Long>(new Comparator<String>() {

			@Override
			public int compare(String key1, String key2) {
				long total1 = key2Total.get(key1);
				long total2 = key2Total.get(key2);
				if (total1 > total2) {
					return -1;
				}
				if (total1 < total2) {
					return 1;
				}
				return key1.compareTo(key2);
			}

		});
		key2SortedTotal.putAll(key2Total);
		List<String> topResult = new ArrayList<>();
		int count = 0;
		for (String key : key2SortedTotal.keySet()) {
			++count;
			if (count > k) break;
			topResult.add(key + "\t" + key2Total.get(key));
		}
		return topResult;
	}
}
