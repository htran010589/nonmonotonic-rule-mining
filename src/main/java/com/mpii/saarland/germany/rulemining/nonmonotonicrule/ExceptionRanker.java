package com.mpii.saarland.germany.rulemining.nonmonotonicrule;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mpii.saarland.germany.indexing.FactIndexer;
import com.mpii.saarland.germany.rules.Exception;
import com.mpii.saarland.germany.rules.ExceptionType;
import com.mpii.saarland.germany.rules.NegativeRule;
import com.mpii.saarland.germany.rules.PositiveRule;

/**
 * 
 * @author Hai Dang Tran
 * 
 */
public class ExceptionRanker {

	private FactIndexer facts, newFacts;

	private InstanceSetMiner form2Instances;

	private List<NegativeRule> choosenNegativeRules;

	public ExceptionRanker(String patternFileName, FactIndexer facts) {
		this.facts = facts;
		newFacts = facts.cloneFact();
		form2Instances = new InstanceSetForm2Miner();
		form2Instances.loadPositiveRules(patternFileName);
		form2Instances.findInstances(facts);
		form2Instances.findPositiveNegativeExamples(facts);
		choosenNegativeRules = new ArrayList<>();
	}

	/**
	 * 
	 * This method is to predict new facts using all exceptions.
	 */
	public void predict(PositiveRule positiveRule, long frequency) {
		String h = positiveRule.getHead();
		Set<String> abnormalSet = form2Instances.positiveRule2AbnormalSet.get(positiveRule);
		if (abnormalSet == null) {
			return;
		}
		Set<Exception> exceptionCandidateSet = ExceptionMiner.getExceptionCandidateSet(positiveRule);
		for (String negativeExample : abnormalSet) {
			String[] parts = negativeExample.split("\t");
			String x = parts[0];
			String z = parts[1];
			boolean ok = true;
			for (int i = 0; i < 3; ++i) {
				ExceptionType type = null;
				if (i == 0) {
					type = ExceptionType.FIRST;
				} else if (i == 1) {
					type = ExceptionType.SECOND;
				} else {
					type = ExceptionType.BOTH;
				}

				Set<String> tOrPSet = null;
				if (i < 2) {
					tOrPSet = facts.getTSetFromX(parts[i]);
				} else {
					tOrPSet = facts.getPSetFromXy(negativeExample);
				}
				if (tOrPSet != null) {
					for (String tOrP : tOrPSet) {
						Exception newException = new Exception(tOrP, type);
						if (exceptionCandidateSet.contains(newException)) {
							ok = false;
							break;
						}
					}
				}
				if (!ok) {
					break;
				}
			}
			if (!ok) {
				continue;
			}
			parts = new String[] { x, h, z };
			newFacts.indexFact(parts, frequency);
			newFacts.indexPattern(parts, frequency);
		}
	}

	/**
	 * 
	 * This method is to recalculate conviction of negative rules based on old
	 * and new facts.
	 */
	public void recalculateConviction(PositiveRule positiveRule, FactIndexer newFacts) {
		List<Set<String>> instances = form2Instances.findInstances(positiveRule, newFacts);
		Set<String> bodyExamples = new HashSet<String>();
		Set<String> positiveHeadRuleExamples = instances.get(0);
		bodyExamples.addAll(instances.get(0));
		bodyExamples.addAll(instances.get(1));

		// Print positive rule with statistics like standard conviction,
		// confidence, ...
		System.out.println(positiveRule.toStringWithStatistics());

		Set<Exception> exceptionCandidateSet = ExceptionMiner.getExceptionCandidateSet(positiveRule);
		Map<Exception, Long> negativeExceptionBodyCount = new HashMap<>();
		Map<Exception, Long> negativeExceptionPositiveHeadRuleCount = new HashMap<>();
		Map<Exception, Long> positiveExceptionBodyCount = new HashMap<>();
		Map<Exception, Long> positiveExceptionNegativeHeadRuleCount = new HashMap<>();
		for (Exception exception : exceptionCandidateSet) {
			negativeExceptionBodyCount.put(exception, (long) bodyExamples.size());
			negativeExceptionPositiveHeadRuleCount.put(exception, (long) positiveHeadRuleExamples.size());
			positiveExceptionBodyCount.put(exception, 0L);
			positiveExceptionNegativeHeadRuleCount.put(exception, 0L);
		}

		for (String xz : bodyExamples) {
			String[] parts = xz.split("\t");
			for (int i = 0; i < 3; ++i) {
				ExceptionType type = null;
				if (i == 0) {
					type = ExceptionType.FIRST;
				} else if (i == 1) {
					type = ExceptionType.SECOND;
				} else {
					type = ExceptionType.BOTH;
				}

				Set<String> tOrPSet = null;
				if (i < 2) {
					tOrPSet = newFacts.getTSetFromX(parts[i]);
				} else {
					tOrPSet = newFacts.getPSetFromXy(xz);
				}
				if (tOrPSet == null) {
					continue;
				}
				for (String tOrP : tOrPSet) {
					Exception exception = new Exception(tOrP, type);
					if (!negativeExceptionBodyCount.containsKey(exception)) {
						continue;
					}
					negativeExceptionBodyCount.put(exception, negativeExceptionBodyCount.get(exception) - 1);
					positiveExceptionBodyCount.put(exception, positiveExceptionBodyCount.get(exception) + 1);
					if (positiveHeadRuleExamples.contains(xz)) {
						negativeExceptionPositiveHeadRuleCount.put(exception,
								negativeExceptionPositiveHeadRuleCount.get(exception) - 1);
					} else {
						positiveExceptionNegativeHeadRuleCount.put(exception,
								positiveExceptionNegativeHeadRuleCount.get(exception) + 1);
					}
				}
			}
		}

		// Calculate statistics
		List<NegativeRule> negativeRules = new ArrayList<>();
		for (Exception exception : exceptionCandidateSet) {
			NegativeRule newNegativeRule = new NegativeRule(positiveRule, exception);
			newNegativeRule.setNegativeExceptionBodyCount(negativeExceptionBodyCount.get(exception));
			newNegativeRule
					.setNegativeExceptionPositiveHeadRuleCount(negativeExceptionPositiveHeadRuleCount.get(exception));
			newNegativeRule.setPositiveExceptionBodyCount(positiveExceptionBodyCount.get(exception));
			newNegativeRule
					.setPositiveExceptionNegativeHeadRuleCount(positiveExceptionNegativeHeadRuleCount.get(exception));
			newNegativeRule.calculateConviction();
			negativeRules.add(newNegativeRule);
		}

		// Sort negative rules according to positive negative, standard
		// convictions
		Comparator<NegativeRule> sortByPositiveNegativeConviction = (NegativeRule r1,
				NegativeRule r2) -> new Double(r2.getPositiveNegativeConviction())
						.compareTo(r1.getPositiveNegativeConviction());
		Comparator<NegativeRule> sortByStandardConviction = (NegativeRule r1,
				NegativeRule r2) -> new Double(r2.getStandardConviction()).compareTo(r1.getStandardConviction());
		negativeRules.sort(sortByPositiveNegativeConviction.thenComparing(sortByStandardConviction));

		int count = 0;
		for (NegativeRule negativeRule : negativeRules) {
			count++;
			System.out.println(negativeRule.toStringWithStatistics());
			if (count > 10) {
				break;
			}
		}
		System.out.println();

		// Select best revised rule
		if (!negativeRules.isEmpty()) {
			choosenNegativeRules.add(negativeRules.get(0));
		}
	}

	public void rankRulesWithExceptions(RankingType type) {
		for (PositiveRule rule : form2Instances.positiveRules) {
			if (form2Instances.getNormalSet(rule) == null) {
				continue;
			}
			rule.setHeadSupport(facts);
			rule.setHeadCount(form2Instances);
			rule.setBodyCount(form2Instances);
			rule.setConfidence();
			rule.setConviction();
		}
		if (type == RankingType.OPM) {
			// Ordered partial materialization is conducted.
			form2Instances.positiveRules.sort(
					(PositiveRule r1, PositiveRule r2) -> new Double(r2.getConviction()).compareTo(r1.getConviction()));
			for (PositiveRule rule : form2Instances.positiveRules) {
				recalculateConviction(rule, newFacts);
				predict(rule, 1L);
			}
		} else if (type == RankingType.PM) {
			// Partial materialization is conducted.
			for (PositiveRule rule : form2Instances.positiveRules) {
				predict(rule, 1L);
			}
			for (PositiveRule rule : form2Instances.positiveRules) {
				predict(rule, -1L);
				recalculateConviction(rule, newFacts);
				predict(rule, 1L);
			}
		} else {
			// Naive ranking is conducted.
			for (PositiveRule rule : form2Instances.positiveRules) {
				recalculateConviction(rule, facts);
			}			
		}

		Comparator<NegativeRule> sortByPositiveNegativeConviction = (NegativeRule r1,
				NegativeRule r2) -> new Double(r2.getPositiveNegativeConviction())
						.compareTo(r1.getPositiveNegativeConviction());
		choosenNegativeRules.sort(sortByPositiveNegativeConviction);
	}

	public List<NegativeRule> getChoosenNegativeRules() {
		return choosenNegativeRules;
	}

	public static void main(String[] args) {
		String patternFileName = args[0];
		FactIndexer facts = new FactIndexer(args[1]);
		int type = Integer.parseInt(args[2]);
		ExceptionRanker ranker = new ExceptionRanker(patternFileName, facts);
		ranker.rankRulesWithExceptions(RankingType.values()[type]);
	}

}
