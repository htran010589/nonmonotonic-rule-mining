package com.mpii.saarland.germany.utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

}
