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
	 * TODO: need to test
	 */
	public void predict(String rule) {
		String h = rule.split("\t")[2];
		Set<String> abnormalSet = rule2OriginalNegativeInstances.get(rule);
		Set<String> exceptionCandidateSet = ExceptionCandidateMiner.getExceptionCandidateSet(rule);
		for (String negativeExample : abnormalSet) {
			String[] parts = negativeExample.split("\t");
			String x = parts[0];
			String z = parts[1];
			boolean ok = true;
			Set<String> tSet = FactIndexer.getInstace().getTSetFromX(x);
			if (tSet == null) {
				continue;
			}
			for (String t : tSet) {
				if (exceptionCandidateSet.contains(t)) {
					ok = false;
					break;
				}
			}
			if (!ok) {
				continue;
			}
			parts = new String[]{x, h, z};
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
	 * @TODO: need to code
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
		double conviction = (1 - getRelativeSupport(h)) / (1 - confidence);
		System.out.println(rule + "\t" + conviction);
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
