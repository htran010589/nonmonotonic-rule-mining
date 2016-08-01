package com.mpii.saarland.germany.rulemining.nonmonotonicrule;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mpii.saarland.germany.experiment.Experiment;
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

	private Map<String, Double> rule2Conviction;

	public Map<String, Double> negRule2Conviction;

	public static Map<String, String> toID;

	private static List<Integer> numExCandidates = new ArrayList<Integer>();

	public ExceptionRanker(FactIndexer facts, String patternFileName) {
		this.facts = facts;
		newFacts = facts.cloneFact();
		form2Instances = new InstanceSetForm2Miner(facts);
		form2Instances.loadPositiveRules(patternFileName);
		form2Instances.createPatterns();
		form2Instances.findInstances();
		form2Instances.findPositiveNegativeExamples();
		rule2Conviction = new HashMap<String, Double>();
		negRule2Conviction = new HashMap<String, Double>();
	}

	/**
	 * 
	 * @TODO confused things start from here...
	 */
	public void predict(String rule) {
		String h = rule.split("\t")[2];
		Set<String> abnormalSet = form2Instances.rule2AbnormalSet.get(rule);
		if (abnormalSet == null) {
			return;
		}
		for (String negativeExample : abnormalSet) {
			String[] parts = negativeExample.split("\t");
			String x = parts[0];
			String z = parts[1];
			boolean ok = true;
			for (int i = 0; i < 3; ++i) {
				Set<String> exceptionCandidateSet = ExceptionCandidateMiner.getExceptionCandidateSet(rule + "\t" + i);
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
			// System.out.println("Add predicted fact: " + x + " " + h + " " +
			// z);
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
	 * @TODO and here...
	 */
	public void recalculateConviction(String rule) {
		String[] parts = rule.split("\t");
		String p = parts[0];
		String q = parts[1];
		String h = parts[2];
		Set<String> bodyExamples = new HashSet<>();
		Set<String> posHeadRuleExamples = new HashSet<>();
		Set<String> negHeadRuleExamples = new HashSet<>();
		for (String yz : newFacts.getXySetFromP(q)) {
			String y = yz.split("\t")[0];
			String z = yz.split("\t")[1];
			Set<String> xSet = newFacts.getXSetFromPy(p + "\t" + y);
			if (xSet == null) {
				continue;
			}
			for (String x : xSet) {
				bodyExamples.add(x + "\t" + z);
				if (newFacts.checkXpy(x + "\t" + h + "\t" + z)) {
					posHeadRuleExamples.add(x + "\t" + z);
				} else {
					negHeadRuleExamples.add(x + "\t" + z);
				}
			}
		}
		double confidence = (double) posHeadRuleExamples.size() / (double) bodyExamples.size();
		double headSupport = getRelativeSupport(h);
		double conviction = (1 - headSupport) / (1 - confidence);
		System.out.println(h + "(x, z) <- " + p + "(x, y) ^ " + q + "(y, z)" + "\t" + conviction + "\t" + confidence
				+ "\t" + posHeadRuleExamples.size() + "\t" + bodyExamples.size());

		Map<String, Long> negExBodyCount = new HashMap<String, Long>();
		Map<String, Long> negExPosHeadRuleCount = new HashMap<String, Long>();
		Map<String, Long> posExBodyCount = new HashMap<String, Long>();
		Map<String, Long> posExNegHeadRuleCount = new HashMap<String, Long>();
		int totalCands = 0;
		for (int i = 0; i < 3; ++i) {
			Set<String> exceptionCandidateSet = ExceptionCandidateMiner.getExceptionCandidateSet(rule + "\t" + i);
			totalCands += exceptionCandidateSet.size();
			for (String exception : exceptionCandidateSet) {
				negExBodyCount.put(exception + "\t" + i, (long) bodyExamples.size());
				negExPosHeadRuleCount.put(exception + "\t" + i, (long) posHeadRuleExamples.size());
				posExBodyCount.put(exception + "\t" + i, 0L);
				posExNegHeadRuleCount.put(exception + "\t" + i, 0L);
			}
		}
		numExCandidates.add(totalCands);
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
					if (!negExBodyCount.containsKey(exception)) {
						continue;
					}
					negExBodyCount.put(exception, negExBodyCount.get(exception) - 1);
					posExBodyCount.put(exception, posExBodyCount.get(exception) + 1);
					if (posHeadRuleExamples.contains(xz)) {
						negExPosHeadRuleCount.put(exception, negExPosHeadRuleCount.get(exception) - 1);
					} else {
						posExNegHeadRuleCount.put(exception, posExNegHeadRuleCount.get(exception) + 1);
					}
				}
			}
		}
		// Map<String, Double> nonmonotonicRule2Conviction = new HashMap<String,
		// Double>();
		double[] maxPosNegConviction = new double[3];
		double[] maxStdConviction = new double[3];
		String[] maxException = new String[3];
		for (int i = 0; i < 3; ++i) {
			maxPosNegConviction[i] = -1;
			maxStdConviction[i] = -1;
		}
		for (String exception : negExPosHeadRuleCount.keySet()) {
			parts = exception.split("\t");
			int type = Integer.parseInt(parts[1]);
			double negExPosHeadConfidence = (double) negExPosHeadRuleCount.get(exception)
					/ (double) negExBodyCount.get(exception);
			double posExNegHeadConfidence = (double) posExNegHeadRuleCount.get(exception)
					/ (double) posExBodyCount.get(exception);
			double stdConviction = (1 - headSupport) / (1 - negExPosHeadConfidence);
			double auxConviction = headSupport / (1 - posExNegHeadConfidence);
			double posNegConviction = (stdConviction + auxConviction) / 2;
			// nonmonotonicRule2Conviction.put(exception,
			// nonmonotonicConviction);
			if (maxPosNegConviction[type] < posNegConviction
					|| (maxPosNegConviction[type] == posNegConviction && maxStdConviction[type] < stdConviction)) {
				maxPosNegConviction[type] = posNegConviction;
				maxStdConviction[type] = stdConviction;
				maxException[type] = parts[0];
			}
		}
		// Utils.sortByValue(nonmonotonicRule2Conviction);
		// int topCount = 0;
		int[] types = new int[3];
		for (int i = 0; i < 3; ++i) {
			types[i] = i;
		}
		for (int i = 0; i < 3; ++i) {
			for (int j = i + 1; j < 3; j++) {
				if (maxPosNegConviction[i] < maxPosNegConviction[j] || (maxPosNegConviction[i] == maxPosNegConviction[j]
						&& maxStdConviction[i] < maxStdConviction[j])) {
					double tempConv = maxPosNegConviction[i];
					maxPosNegConviction[i] = maxPosNegConviction[j];
					maxPosNegConviction[j] = tempConv;

					tempConv = maxStdConviction[i];
					maxStdConviction[i] = maxStdConviction[j];
					maxStdConviction[j] = tempConv;

					int tempType = types[i];
					types[i] = types[j];
					types[j] = tempType;

					String tempException = maxException[i];
					maxException[i] = maxException[j];
					maxException[j] = tempException;
				}
			}
		}
		System.out.println("Exceptions:");
		double maxConv = -1;
		for (int i = 0; i < 3; ++i) {
			// for (String exception : nonmonotonicRule2Conviction.keySet()) {
			if (maxException[i] == null) {
				continue;
			}
			String exception = maxException[i] + "\t" + types[i];
			// System.out.println(exception);
			double negExPosHeadConfidence = (double) negExPosHeadRuleCount.get(exception)
					/ (double) negExBodyCount.get(exception);
			double posExNegHeadConfidence = (double) posExNegHeadRuleCount.get(exception)
					/ (double) posExBodyCount.get(exception);
			double stdConviction = (1 - headSupport) / (1 - negExPosHeadConfidence);
			double auxConviction = headSupport / (1 - posExNegHeadConfidence);
			double posNegConviction = (stdConviction + auxConviction) / 2;
			System.out.print("not " + maxException[i]);
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
					+ negExPosHeadRuleCount.get(exception) + "\t" + negExBodyCount.get(exception) + "\t"
					+ posExNegHeadRuleCount.get(exception) + "\t" + posExBodyCount.get(exception));
		}
		System.out.println();

		String bestRule = rule;
		bestRule = bestRule + "\t" + maxException[0] + "\t" + types[0];
		negRule2Conviction.put(bestRule, maxConv);
	}

	public void rankRulesWithExceptions() {
		for (String rule : form2Instances.positiveRules) {
			if (form2Instances.getNormalSet(rule) == null) {
				continue;
			}
			rule2Conviction.put(rule, getConviction(rule));
		}
		rule2Conviction = Utils.sortByValue(rule2Conviction);
		for (String rule : rule2Conviction.keySet()) {
			recalculateConviction(rule);
			predict(rule);
		}
		negRule2Conviction = Utils.sortByValue(negRule2Conviction);

		System.out.println("Number of processed rules: " + negRule2Conviction.size());
		Collections.sort(numExCandidates);
		System.out.println("Median number of exception cadidates for all rules are: " + numExCandidates.get(numExCandidates.size() / 2));

		try {
			for (String type : Experiment.types) {
				for (int maxCnt : Experiment.maxCnts) {
					int cnt = 0;
					Writer wr = new PrintWriter(new File(Experiment.RULE_FILE + type + maxCnt));
					double convSum = 0;
					for (String negRule : negRule2Conviction.keySet()) {
						cnt++;
						if (cnt > maxCnt) {
							break;
						}
						String[] parts = negRule.split("\t");
						String posRule = parts[2] + "(X, Z) :- " + parts[0] + "(X, Y), " + parts[1] + "(Y, Z)";
						String negation = "";
						if (parts[4].equals("0")) {
							negation = Experiment.toID.get(parts[3]) + "(X).";
						} else if (parts[4].equals("1")) {
							negation = Experiment.toID.get(parts[3]) + "(Z).";
						} else {
							negation = parts[3] + "(X, Z).";
						}
						if (type.equals(".neg.")) {
							wr.write(posRule + ", not " + negation + "\n");
							double conviction = negRule2Conviction.get(negRule);
							convSum += conviction;
						} else if (type.equals(".pos.")) {
							wr.write(posRule + ".\n");
							double conviction = rule2Conviction.get(parts[0] + "\t" + parts[1] + "\t" + parts[2]);
							convSum += conviction;
						} else {
							wr.write(posRule + ", not " + negation + "\n");
							wr.write("not_" + posRule + ", " + negation + "\n");
						}
					}
					wr.close();
					System.out.println("Done with " + Experiment.RULE_FILE + type + maxCnt + " file");
					System.out.println("avg conv = " + (convSum / maxCnt));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Experiment.date3 = new Date();
		System.out.println("Time for revisions (seconds): " + ((Experiment.date3.getTime() - Experiment.date2.getTime()) / 1000.0));
	}

}
