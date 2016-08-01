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
public class ExceptionMiner {

	private static Map<String, Set<String>> rule2ExceptionSet;

	static {
		rule2ExceptionSet = new HashMap<String, Set<String>>();
	}

	/**
	 * 
	 * This method is to find exception candidates given a rule, set of facts, positive and negative instances.
	 * 
	 */
	public static void findCandidates(String rule, Set<String> positivePairs, Set<String> negativePairs,
			FactIndexer facts) {
		for (int i = 0; i < 3; ++i) {
			Set<String> exceptionSet = new HashSet<String>();
			if (positivePairs != null) {
				for (String pair : positivePairs) {
					String[] parts = pair.split("\t");
					Set<String> positiveExceptionSet = null;
					if (i < 2) {
						positiveExceptionSet = facts.getTSetFromX(parts[i]);
					} else {
						positiveExceptionSet = facts.getPSetFromXy(pair);
					}
					if (positiveExceptionSet == null) {
						continue;
					}
					exceptionSet.addAll(positiveExceptionSet);
				}
			}
			if (negativePairs != null) {
				for (String pair : negativePairs) {
					String[] parts = pair.split("\t");
					Set<String> negativeExceptionSet = null;
					if (i < 2) {
						negativeExceptionSet = facts.getTSetFromX(parts[i]);
					} else {
						negativeExceptionSet = facts.getPSetFromXy(pair);
					}
					if (negativeExceptionSet == null) {
						continue;
					}
					exceptionSet.removeAll(negativeExceptionSet);
				}
			}
			rule2ExceptionSet.put(rule + "\t" + i, exceptionSet);
		}

	}

	public static Set<String> getExceptionCandidateSet(String rule) {
		return rule2ExceptionSet.get(rule);
	}

}
