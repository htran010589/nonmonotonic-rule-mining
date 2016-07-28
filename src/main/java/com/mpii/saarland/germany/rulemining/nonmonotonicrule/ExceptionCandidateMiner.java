package com.mpii.saarland.germany.rulemining.nonmonotonicrule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.mpii.saarland.germany.indexing.FactIndexer;

/**
 * 
 * @author Mohamed Gad-Elrab, Hai Dang Tran
 *
 */
public class ExceptionCandidateMiner {

	private static Map<String, Set<String>> rule2ExceptionCandidateSet;

	static {
		rule2ExceptionCandidateSet = new HashMap<String, Set<String>>();
	}

	/**
	 * 
	 * @TODO need to reconstruct something from here
	 * 
	 */
	public static void findCandidates(String rule, Set<String> positivePairs, Set<String> negativePairs,
			FactIndexer facts) {
		for (int i = 0; i < 3; ++i) {
			Set<String> exceptionSet = new HashSet<String>();
			if (positivePairs != null) {
				for (String pair : positivePairs) {
					String[] parts = pair.split("\t");
					Set<String> posExceptionSet = null;
					if (i < 2) {
						posExceptionSet = facts.getTSetFromX(parts[i]);
					} else {
						posExceptionSet = facts.getPSetFromXy(pair);
					}
					if (posExceptionSet == null) {
						continue;
					}
					exceptionSet.addAll(posExceptionSet);
				}
			}
			if (negativePairs != null) {
				for (String pair : negativePairs) {
					String[] parts = pair.split("\t");
					Set<String> negExceptionSet = null;
					if (i < 2) {
						negExceptionSet = facts.getTSetFromX(parts[i]);
					} else {
						negExceptionSet = facts.getPSetFromXy(pair);
					}
					if (negExceptionSet == null) {
						continue;
					}
					exceptionSet.removeAll(negExceptionSet);
				}
			}
			rule2ExceptionCandidateSet.put(rule + "\t" + i, exceptionSet);
		}

		// System.out.println("rule = " + rule);
		// for (String str : t2Support.keySet()) {
		// System.out.println(str);
		// }
		// System.out.println("+++++");

		// List<String> topCandidates = Utils.getTopK(t2Support, 10);
		// for (String candidate : topCandidates) {
		// System.out.println(candidate);
		// }
	}

	public static Set<String> getExceptionCandidateSet(String rule) {
		// System.out.println("rule lan nay la: " + rule);
		return rule2ExceptionCandidateSet.get(rule);
	}

}
