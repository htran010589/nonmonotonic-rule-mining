package com.mpii.saarland.germany.rulemining.nonmonotonicrule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mpii.saarland.germany.indexing.FactIndexer;
import com.mpii.saarland.germany.utils.Utils;

/**
 * 
 * @author Mohamed Gad-Elrab, Hai Dang Tran
 *
 */
public class ExceptionCandidateMiner {

	private static Map<String, Set<String>> rule2ExceptionCandidateSet;

	public static void findCandidates(String rule, Set<String> positivePairs, Set<String> negativePairs, int idx) {
		rule2ExceptionCandidateSet = new HashMap<String, Set<String>>();
		Map<String, Long> t2Support = new HashMap<String, Long>();
		Set<String> xSet = new HashSet<>();
		for (String pair : positivePairs) {
			String[] parts = pair.split("\t");
			if (xSet.contains(parts[idx])) {
				continue;
			}
			xSet.add(parts[idx]);
			Set<String> tSet = FactIndexer.getInstace().getTSetFromX(parts[idx]);
			for (String t : tSet) {
				Utils.addKeyLong(t2Support, t, 1);
			}
		}
		for (String pair : negativePairs) {
			String[] parts = pair.split("\t");
			Set<String> tSet = FactIndexer.getInstace().getTSetFromX(parts[idx]);
			for (String t : tSet) {
				t2Support.remove(t);
			}
		}
		rule2ExceptionCandidateSet.put(rule, t2Support.keySet());
//		List<String> topCandidates = Utils.getTopK(t2Support, 10);
//		for (String candidate : topCandidates) {
//			System.out.println(candidate);
//		}
	}

	public static Set<String> getExceptionCandidateSet(String rule) {
		return rule2ExceptionCandidateSet.get(rule);
	}

}
