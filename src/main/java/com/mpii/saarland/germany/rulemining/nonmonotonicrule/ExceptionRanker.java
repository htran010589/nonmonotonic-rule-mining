package com.mpii.saarland.germany.rulemining.nonmonotonicrule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mpii.saarland.germany.indexing.FactIndexer;
import com.mpii.saarland.germany.utils.Utils;

/**
 * 
 * @author Hai Dang Tran
 * 
 */
public class ExceptionRanker {

	private FactIndexer facts, newFacts;

	private InstanceSetMiner form2Instances;

	private Map<String, Double> positiveRule2Conviction;

	private Map<String, Double> negativeRule2Conviction;

	private List<Integer> numberOfExceptions = new ArrayList<Integer>();

	public ExceptionRanker(String patternFileName, FactIndexer facts) {
		this.facts = facts;
		newFacts = facts.cloneFact();
		form2Instances = new InstanceSetForm2Miner();
		form2Instances.loadPositiveRules(patternFileName);
		form2Instances.findInstances(facts);
		form2Instances.findPositiveNegativeExamples(facts);
		positiveRule2Conviction = new HashMap<String, Double>();
		negativeRule2Conviction = new HashMap<String, Double>();
	}

	/**
	 * 
	 * This method is to predict new facts using all exceptions.
	 */
	public void predict(String rule) {
		String h = rule.split("\t")[2];
		Set<String> abnormalSet = form2Instances.positiveRule2AbnormalSet.get(rule);
		if (abnormalSet == null) {
			return;
		}
		for (String negativeExample : abnormalSet) {
			String[] parts = negativeExample.split("\t");
			String x = parts[0];
			String z = parts[1];
			boolean ok = true;
			for (int i = 0; i < 3; ++i) {
				Set<String> exceptionCandidateSet = ExceptionMiner.getExceptionCandidateSet(rule + "\t" + i);
				Set<String> tOrPSet = null;
				if (i < 2) {
					tOrPSet = facts.getTSetFromX(parts[i]);
				} else {
					tOrPSet = facts.getPSetFromXy(negativeExample);
				}
				if (tOrPSet != null) {
					for (String tOrP : tOrPSet) {
						if (exceptionCandidateSet.contains(tOrP)) {
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
			newFacts.indexFact(parts);
			newFacts.indexPattern(parts);
		}
	}

	public double getConfidence(String rule) {
		double headCount = 0;
		if (form2Instances.getNormalSet(rule) != null) {
			headCount = form2Instances.getNormalSet(rule).size();
		}
		double bodyCount = headCount;
		if (form2Instances.getAbnormalSet(rule) != null) {
			bodyCount += form2Instances.getAbnormalSet(rule).size();
		}
		return headCount / bodyCount;
	}

	public double getRelativeSupport(String head) {
		double support = 0;
		if (facts.getXySetFromP(head) != null) {
			support = facts.getXySetFromP(head).size();
		}
		double xSupport = 0;
		if (facts.getXSetFromP(head) != null) {
			xSupport = facts.getXSetFromP(head).size();
		}
		double ySupport = 0;
		if (facts.getYSetFromP(head) != null) {
			ySupport = facts.getYSetFromP(head).size();
		}
		return support / (xSupport * ySupport);
	}

	public double getConviction(String rule) {
		String[] parts = rule.split("\t");
		return (1 - getRelativeSupport(parts[2])) / (1 - getConfidence(rule));
	}

	/**
	 * 
	 * This method is to recalculate conviction of negative rules based on old and new facts.
	 */
	public void recalculateConviction(String rule) {
		String[] parts = rule.split("\t");
		String p = parts[0];
		String q = parts[1];
		String h = parts[2];
		List<Set<String>> instances = form2Instances.findInstances(rule, newFacts);
		Set<String> bodyExamples = new HashSet<String>();
		Set<String> posHeadRuleExamples = instances.get(0);
		bodyExamples.addAll(instances.get(0));
		Set<String> negHeadRuleExamples = instances.get(1);
		bodyExamples.addAll(instances.get(1));

		double confidence = (double) posHeadRuleExamples.size() / (double) bodyExamples.size();
		double headSupport = getRelativeSupport(h);
		double conviction = (1 - headSupport) / (1 - confidence);
		System.out.println(h + "(x, z) <- " + p + "(x, y) ^ " + q + "(y, z)" + "\t" + conviction + "\t" + confidence
				+ "\t" + posHeadRuleExamples.size() + "\t" + bodyExamples.size());

		Map<String, Long> negativeExceptionBodyCount = new HashMap<String, Long>();
		Map<String, Long> negativeExceptionPositiveHeadRuleCount = new HashMap<String, Long>();
		Map<String, Long> positiveExceptionBodyCount = new HashMap<String, Long>();
		Map<String, Long> positiveExceptionNegativeHeadRuleCount = new HashMap<String, Long>();
		int totalCandidates = 0;
		for (int i = 0; i < 3; ++i) {
			Set<String> exceptionCandidateSet = ExceptionMiner.getExceptionCandidateSet(rule + "\t" + i);
			totalCandidates += exceptionCandidateSet.size();
			for (String exception : exceptionCandidateSet) {
				negativeExceptionBodyCount.put(exception + "\t" + i, (long) bodyExamples.size());
				negativeExceptionPositiveHeadRuleCount.put(exception + "\t" + i, (long) posHeadRuleExamples.size());
				positiveExceptionBodyCount.put(exception + "\t" + i, 0L);
				positiveExceptionNegativeHeadRuleCount.put(exception + "\t" + i, 0L);
			}
		}
		numberOfExceptions.add(totalCandidates);
		for (String xz : bodyExamples) {
			parts = xz.split("\t");
			for (int i = 0; i < 3; ++i) {
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
					String exception = tOrP + "\t" + i;
					if (!negativeExceptionBodyCount.containsKey(exception)) {
						continue;
					}
					negativeExceptionBodyCount.put(exception, negativeExceptionBodyCount.get(exception) - 1);
					positiveExceptionBodyCount.put(exception, positiveExceptionBodyCount.get(exception) + 1);
					if (posHeadRuleExamples.contains(xz)) {
						negativeExceptionPositiveHeadRuleCount.put(exception, negativeExceptionPositiveHeadRuleCount.get(exception) - 1);
					} else {
						positiveExceptionNegativeHeadRuleCount.put(exception, positiveExceptionNegativeHeadRuleCount.get(exception) + 1);
					}
				}
			}
		}
		double[] maximumPositiveNegativeConviction = new double[3];
		double[] maximumStandardConviction = new double[3];
		String[] maximumException = new String[3];
		for (int i = 0; i < 3; ++i) {
			maximumPositiveNegativeConviction[i] = -1;
			maximumStandardConviction[i] = -1;
		}
		for (String exception : negativeExceptionPositiveHeadRuleCount.keySet()) {
			parts = exception.split("\t");
			int type = Integer.parseInt(parts[1]);
			double negativeExceptionPositiveHeadConfidence = (double) negativeExceptionPositiveHeadRuleCount.get(exception)
					/ (double) negativeExceptionBodyCount.get(exception);
			double positiveExceptionNegativeHeadConfidence = (double) positiveExceptionNegativeHeadRuleCount.get(exception)
					/ (double) positiveExceptionBodyCount.get(exception);
			double standardConviction = (1 - headSupport) / (1 - negativeExceptionPositiveHeadConfidence);
			double auxiliaryConviction = headSupport / (1 - positiveExceptionNegativeHeadConfidence);
			double posNegConviction = (standardConviction + auxiliaryConviction) / 2;
			if (maximumPositiveNegativeConviction[type] < posNegConviction
					|| (maximumPositiveNegativeConviction[type] == posNegConviction && maximumStandardConviction[type] < standardConviction)) {
				maximumPositiveNegativeConviction[type] = posNegConviction;
				maximumStandardConviction[type] = standardConviction;
				maximumException[type] = parts[0];
			}
		}
		int[] types = new int[3];
		for (int i = 0; i < 3; ++i) {
			types[i] = i;
		}
		for (int i = 0; i < 3; ++i) {
			for (int j = i + 1; j < 3; j++) {
				if (maximumPositiveNegativeConviction[i] < maximumPositiveNegativeConviction[j] || (maximumPositiveNegativeConviction[i] == maximumPositiveNegativeConviction[j]
						&& maximumStandardConviction[i] < maximumStandardConviction[j])) {
					double tempConv = maximumPositiveNegativeConviction[i];
					maximumPositiveNegativeConviction[i] = maximumPositiveNegativeConviction[j];
					maximumPositiveNegativeConviction[j] = tempConv;

					tempConv = maximumStandardConviction[i];
					maximumStandardConviction[i] = maximumStandardConviction[j];
					maximumStandardConviction[j] = tempConv;

					int tempType = types[i];
					types[i] = types[j];
					types[j] = tempType;

					String tempException = maximumException[i];
					maximumException[i] = maximumException[j];
					maximumException[j] = tempException;
				}
			}
		}
		System.out.println("Exceptions:");
		double maxConv = -1;
		for (int i = 0; i < 3; ++i) {
			if (maximumException[i] == null) {
				continue;
			}
			String exception = maximumException[i] + "\t" + types[i];
			// System.out.println(exception);
			double negExPosHeadConfidence = (double) negativeExceptionPositiveHeadRuleCount.get(exception)
					/ (double) negativeExceptionBodyCount.get(exception);
			double posExNegHeadConfidence = (double) positiveExceptionNegativeHeadRuleCount.get(exception)
					/ (double) positiveExceptionBodyCount.get(exception);
			double stdConviction = (1 - headSupport) / (1 - negExPosHeadConfidence);
			double auxConviction = headSupport / (1 - posExNegHeadConfidence);
			double posNegConviction = (stdConviction + auxConviction) / 2;
			System.out.print("not " + maximumException[i]);
			if (types[i] == 0) {
				System.out.print("(x)\t");
			} else if (types[i] == 1) {
				System.out.print("(z)\t");
			} else {
				System.out.print("(x, z)\t");
			}
			if (i == 0) {
				maxConv = stdConviction;
			}
			System.out.println(posNegConviction + "\t" + stdConviction + "\t" + auxConviction + "\t"
					+ negExPosHeadConfidence + "\t" + posExNegHeadConfidence + "\t"
					+ negativeExceptionPositiveHeadRuleCount.get(exception) + "\t" + negativeExceptionBodyCount.get(exception) + "\t"
					+ positiveExceptionNegativeHeadRuleCount.get(exception) + "\t" + positiveExceptionBodyCount.get(exception));
		}
		System.out.println();

		String bestRule = rule;
		bestRule = bestRule + "\t" + maximumException[0] + "\t" + types[0];
		negativeRule2Conviction.put(bestRule, maxConv);
	}

	public void rankRulesWithExceptions() {
		for (String rule : form2Instances.positiveRules) {
			if (form2Instances.getNormalSet(rule) == null) {
				continue;
			}
			positiveRule2Conviction.put(rule, getConviction(rule));
		}
		positiveRule2Conviction = Utils.sortByValue(positiveRule2Conviction);
		for (String rule : positiveRule2Conviction.keySet()) {
			recalculateConviction(rule);
			predict(rule);
		}
		negativeRule2Conviction = Utils.sortByValue(negativeRule2Conviction);

		System.out.println("Number of processed rules: " + negativeRule2Conviction.size());
		Collections.sort(numberOfExceptions);
		System.out.println("Median number of exception cadidates for all rules are: " + numberOfExceptions.get(numberOfExceptions.size() / 2));

	}

	public double getPositiveRuleConviction(String positiveRule) {
		return positiveRule2Conviction.get(positiveRule);
	}

	public double getNegativeRuleConviction(String negativeRule) {
		return negativeRule2Conviction.get(negativeRule);
	}

	public Set<String> getNegativeRules() {
		return negativeRule2Conviction.keySet();
	}

}
