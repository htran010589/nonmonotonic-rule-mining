package mpii.saarland.germany.rulemining.nonmonotonicrule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mpii.saarland.germany.indexing.FactIndexer;
import mpii.saarland.germany.rules.Exception;
import mpii.saarland.germany.rules.ExceptionType;
import mpii.saarland.germany.rules.PositiveRule;

/**
 * 
 * @author Mohamed Gad-Elrab, Hai Dang Tran
 * 
 */
public class ExceptionMiner {

	private static Map<PositiveRule, Set<Exception>> rule2ExceptionSet;

	static {
		rule2ExceptionSet = new HashMap<>();
	}

	/**
	 * 
	 * This method is to find exception candidates given a rule, set of facts,
	 * positive and negative instances.
	 * 
	 */
	public static void findCandidates(PositiveRule rule, Set<String> positivePairs, Set<String> negativePairs,
			FactIndexer facts) {
		Set<Exception> exceptionSet = new HashSet<>();
		for (int i = 0; i < 3; ++i) {
			ExceptionType type = null;
			if (i == 0) {
				type = ExceptionType.FIRST;
			} else if (i == 1) {
				type = ExceptionType.SECOND;
			} else {
				type = ExceptionType.BOTH;
			}
			Set<String> textExceptionSet = new HashSet<String>();
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
					textExceptionSet.addAll(positiveExceptionSet);
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
					textExceptionSet.removeAll(negativeExceptionSet);
				}
			}
			for (String textException : textExceptionSet) {
				exceptionSet.add(new Exception(textException, type));
			}
		}
		rule2ExceptionSet.put(rule, exceptionSet);
	}

	public static Set<Exception> getExceptionCandidateSet(PositiveRule rule) {
		return rule2ExceptionSet.get(rule);
	}

}
