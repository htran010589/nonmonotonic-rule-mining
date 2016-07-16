package com.mpii.saarland.germany.rulemining.nonmonotonicrule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mpii.saarland.germany.indexing.FactIndexer;
import com.mpii.saarland.germany.utils.Settings;
import com.mpii.saarland.germany.utils.Utils;

/**
 * 
 * @author Hai Dang Tran
 * 
 */
public class ExceptionRanker {

	private InstanceSetMiner form2Instances;

	private Map<String, Double> rule2Conviction;

	private Map<String, Set<String>> rule2OriginalNegativeInstances;

	public ExceptionRanker() {
		form2Instances = new InstanceSetForm2Miner();
		form2Instances.loadPositiveRules(Settings.YAGO_FORM2_POSITIVE_RULE_FILE_NAME);
		form2Instances.createPatterns();
		form2Instances.findInstances();
		form2Instances.findPositiveNegativeExamples();
		rule2Conviction = new HashMap<String, Double>();
		rule2OriginalNegativeInstances = new HashMap<String, Set<String>>();
		rule2OriginalNegativeInstances.putAll(form2Instances.rule2AbnormalSet);
	}

	/**
	 * 
	 * @TODO confused things start from here...
	 */
	public void predict(String rule) {
		String h = rule.split("\t")[2];
		Set<String> abnormalSet = rule2OriginalNegativeInstances.get(rule);
		for (String negativeExample : abnormalSet) {
			String[] parts = negativeExample.split("\t");
			String x = parts[0];
			String z = parts[1];
			boolean ok = true;
			for (int i = 0; i < 3; ++i) {
				Set<String> exceptionCandidateSet = ExceptionCandidateMiner.getExceptionCandidateSet(rule + "\t" + i);
				Set<String> tOrPSet = null;
				if (i < 2) {
					tOrPSet = FactIndexer.getInstace().getTSetFromX(parts[i]);
				} else {
					tOrPSet = FactIndexer.getInstace().getPSetFromXy(negativeExample);
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
//			System.out.println("Add predicted fact: " + x + " " + h + " " + z);
			parts = new String[] { x, h, z };
			FactIndexer.getInstace().indexFact(parts);
			FactIndexer.getInstace().indexPattern(parts);
		}
	}

	public double getConfidence(String rule) {
		double headCount = form2Instances.getNormalSet(rule).size();
		double bodyCount = headCount + form2Instances.getAbnormalSet(rule).size();
		return headCount / bodyCount;
	}

	public double getRelativeSupport(String head) {
		double support = FactIndexer.getInstace().getXySetFromP(head).size();
		double xSupport = FactIndexer.getInstace().getXSetFromP(head).size();
		double ySupport = FactIndexer.getInstace().getYSetFromP(head).size();
		return support / (xSupport * ySupport);
	}

	public double getConviction(String rule) {
		String[] parts = rule.split("\t");
		return (1 - getRelativeSupport(parts[2])) / (1 - getConfidence(rule));
	}

	/**
	 * 
	 * @TODO and here...
	 */
	public void recalculateConviction(String rule) {
		String[] parts = rule.split("\t");
		String p = parts[0];
		String q = parts[1];
		String h = parts[2];
		Set<String> bodyExamples = new HashSet<>();
		Set<String> ruleExamples = new HashSet<>();
		for (String yz : FactIndexer.getInstace().getXySetFromP(q)) {
			String y = yz.split("\t")[0];
			String z = yz.split("\t")[1];
			Set<String> xSet = FactIndexer.getInstace().getXSetFromPy(p + "\t" + y);
			if (xSet == null) {
				continue;
			}
			for (String x : xSet) {
				bodyExamples.add(x + "\t" + z);
				if (FactIndexer.getInstace().checkXpy(x + "\t" + h + "\t" + z)) {
					ruleExamples.add(x + "\t" + z);
				}
			}
		}
		double confidence = (double) ruleExamples.size() / (double) bodyExamples.size();
		double headSupport = getRelativeSupport(h);
		double conviction = (1 - headSupport) / (1 - confidence);
		System.out.println(h + "(x, z) <- " + p + "(x, y) ^ " + q + "(y, z)" + "\t" + conviction + "\t" + confidence
				+ "\t" + ruleExamples.size() + "\t" + bodyExamples.size());

		Map<String, Long> nonmonotonicBodyCount = new HashMap<String, Long>();
		Map<String, Long> nonmonotonicRuleCount = new HashMap<String, Long>();
		for (int i = 0; i < 3; ++i) {
			Set<String> exceptionCandidateSet = ExceptionCandidateMiner.getExceptionCandidateSet(rule + "\t" + i);
			for (String exception : exceptionCandidateSet) {
				nonmonotonicBodyCount.put(exception + "\t" + i, (long) bodyExamples.size());
				nonmonotonicRuleCount.put(exception + "\t" + i, (long) ruleExamples.size());
			}
		}
		for (String xz : bodyExamples) {
			parts = xz.split("\t");
			for (int i = 0; i < 3; ++i) {
				Set<String> tOrPSet = null;
				if (i < 2) {
					tOrPSet = FactIndexer.getInstace().getTSetFromX(parts[i]);
				} else {
					tOrPSet = FactIndexer.getInstace().getPSetFromXy(xz);
				}
				if (tOrPSet == null) {
					continue;
				}
				for (String tOrP : tOrPSet) {
					String execption = tOrP + "\t" + i;
					if (!nonmonotonicBodyCount.containsKey(execption)) {
						continue;
					}
					nonmonotonicBodyCount.put(execption, nonmonotonicBodyCount.get(execption) - 1);
					if (!ruleExamples.contains(xz)) {
						continue;
					}
					nonmonotonicRuleCount.put(execption, nonmonotonicRuleCount.get(execption) - 1);
				}
			}
		}
//		Map<String, Double> nonmonotonicRule2Conviction = new HashMap<String, Double>();
		double[] maxConviction = new double[3];
		String[] maxException = new String[3];
		for (int i = 0; i < 3; ++i) {
			maxConviction[i] = -1;
		}
		for (String exception : nonmonotonicRuleCount.keySet()) {
			parts = exception.split("\t");
			int type = Integer.parseInt(parts[1]);
			double nonmonotonicConfidence = (double) nonmonotonicRuleCount.get(exception)
					/ (double) nonmonotonicBodyCount.get(exception);
			double nonmonotonicConviction = (1 - headSupport) / (1 - nonmonotonicConfidence);
//			nonmonotonicRule2Conviction.put(exception, nonmonotonicConviction);
			if (maxConviction[type] < nonmonotonicConviction) {
				maxConviction[type] = nonmonotonicConviction;
				maxException[type] = parts[0];
			}
		}
//		Utils.sortByValue(nonmonotonicRule2Conviction);
//		int topCount = 0;
		System.out.println("Exceptions:");
		for (int i = 0; i < 3; ++i) {
//		for (String exception : nonmonotonicRule2Conviction.keySet()) {
			if (maxException[i] == null) {
				continue;
			}
			String exception = maxException[i] + "\t" + i;
//			System.out.println(exception);
			double nonmonotonicConfidence = (double) nonmonotonicRuleCount.get(exception)
					/ (double) nonmonotonicBodyCount.get(exception);
			double nonmonotonicConviction = (1 - headSupport) / (1 - nonmonotonicConfidence);
			System.out.print("not " + maxException[i]);
			if (i == 0) {
				System.out.print("(x)\t");
			} else if (i == 1) {
				System.out.print("(z)\t");
			} else {
				System.out.print("(x, z)\t");
			}
			System.out.println(nonmonotonicConviction + "\t" + nonmonotonicConfidence
					+ "\t" + nonmonotonicRuleCount.get(exception) + "\t" + nonmonotonicBodyCount.get(exception));
//			topCount++;
//			if (topCount >= 25) {
//				break;
//			}
		}
		System.out.println();
	}

	public void rankRulesWithExceptions() {
		for (String rule : form2Instances.positiveRules) {
			rule2Conviction.put(rule, getConviction(rule));
		}
		rule2Conviction = Utils.sortByValue(rule2Conviction);
		for (String rule : rule2Conviction.keySet()) {
			recalculateConviction(rule);
			predict(rule);
		}
	}

}
