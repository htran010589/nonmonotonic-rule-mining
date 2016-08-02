package com.mpii.saarland.germany.rulemining.nonmonotonicrule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mpii.saarland.germany.indexing.FactIndexer;

/**
 * 
 * @author Hai Dang Tran
 * 
 */
public abstract class InstanceSetMiner {

	private static final Logger LOG = LoggerFactory.getLogger(InstanceSetMiner.class);

	protected Set<String> positiveRules;

	protected Map<String, Set<String>> positiveRule2NormalSet;

	protected Map<String, Set<String>> positiveRule2AbnormalSet;

	protected InstanceSetMiner() {
		positiveRules = new HashSet<String>();
		positiveRule2NormalSet = new HashMap<String, Set<String>>();
		positiveRule2AbnormalSet = new HashMap<String, Set<String>>();
	}

	public abstract void loadPositiveRules(String fileName);

	public abstract List<Set<String>> findInstances(String rule, FactIndexer facts);

	public void findInstances(FactIndexer facts) {
		for (String positiveRule : positiveRules) {
			List<Set<String>> instances = findInstances(positiveRule, facts);
			positiveRule2NormalSet.put(positiveRule, instances.get(0));
			positiveRule2AbnormalSet.put(positiveRule, instances.get(1));
		}
		LOG.info("Done with normal and abnormal sets");
	}

	public void findPositiveNegativeExamples(FactIndexer facts) {
		for (String positiveRule : positiveRules) {
			Set<String> normalSet = positiveRule2NormalSet.get(positiveRule);
			Set<String> abnormalSet = positiveRule2AbnormalSet.get(positiveRule);
			ExceptionMiner.findCandidates(positiveRule, abnormalSet, normalSet, facts);
		}
		LOG.info("Done with finding EWS");
	}

	public Set<String> getPositiveRules() {
		return positiveRules;
	}

	public Set<String> getNormalSet(String rule) {
		return positiveRule2NormalSet.get(rule);
	}

	public Set<String> getAbnormalSet(String rule) {
		return positiveRule2AbnormalSet.get(rule);
	}

}
