package com.mpii.saarland.germany.rulemining.nonmonotonicrule;

import java.util.HashMap;
import java.util.HashSet;
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

	protected FactIndexer facts;

	protected Set<String> positiveRules;

	protected Map<String, Set<String>> rule2NormalSet;

	protected Map<String, Set<String>> rule2AbnormalSet;

	protected InstanceSetMiner(FactIndexer facts) {
		this.facts = facts;
		positiveRules = new HashSet<String>();
		rule2NormalSet = new HashMap<String, Set<String>>();
		rule2AbnormalSet = new HashMap<String, Set<String>>();
	}

	public abstract void loadPositiveRules(String fileName);

	public abstract void createPatterns();

	public abstract void findInstances();

	public void findPositiveNegativeExamples() {
		for (String rule : positiveRules) {
			Set<String> normalSet = rule2NormalSet.get(rule);
			Set<String> abnormalSet = rule2AbnormalSet.get(rule);
			ExceptionMiner.findCandidates(rule, abnormalSet, normalSet, facts);
		}
		LOG.info("Done with finding EWS");
	}

	public Set<String> getPositiveRules() {
		return positiveRules;
	}

	public Set<String> getNormalSet(String rule) {
		return rule2NormalSet.get(rule);
	}

	public Set<String> getAbnormalSet(String rule) {
		return rule2AbnormalSet.get(rule);
	}

}
