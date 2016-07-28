package com.mpii.saarland.germany.rulemining.nonmonotonicrule;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mpii.saarland.germany.experiment.Experiment;
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
//			List<Set<String>> normalComponents = new ArrayList<Set<String>>();
//			List<Set<String>> abnormalComponents = new ArrayList<Set<String>>();
//			for (int i = 0; i < 2; ++i) {
//				normalComponents.add(new HashSet<String>());
//				abnormalComponents.add(new HashSet<String>());
//			}
//			for (String normalPair : normalSet) {
//				String[] parts = normalPair.split("\t");
//				for (int i = 0; i < 2; ++i) {
//					normalComponents.get(i).add(parts[i]);
//				}
//			}
//			for (String abnormalPair : abnormalSet) {
//				String[] parts = abnormalPair.split("\t");
//				for (int i = 0; i < 2; ++i) {
//					abnormalComponents.get(i).add(parts[i]);
//				}
//			}
			// This is for non-conflict & conflict cases, but not a concern anymore
			/*
			Set<String> ePlusNonConflict = new HashSet<>();
			Set<String> ePlusConflict = new HashSet<>();
			for (String instance : abnormalSet) {
				String[] parts = instance.split("\t");
				boolean ok = true;
				for (int i = 0; i < 2; i++) {
					if (normalComponents.get(i).contains(parts[i])) {
						ok = false;
					}
				}
				if (ok) {
					ePlusNonConflict.add(instance);
				} else {
					ePlusConflict.add(instance);
				}
			}
			Set<String> eMinusNonConflict = new HashSet<>();
			Set<String> eMinusConflict = new HashSet<>();
			for (String instance : normalSet) {
				String[] parts = instance.split("\t");
				boolean ok = true;
				for (int i = 0; i < 2; i++) {
					if (abnormalComponents.get(i).contains(parts[i])) {
						ok = false;
					}
				}
				if (ok) {
					eMinusNonConflict.add(instance);
				} else {
					eMinusConflict.add(instance);
				}
			}*/
			// Done non-conflict & conflict cases

			ExceptionCandidateMiner.findCandidates(rule, abnormalSet, normalSet, facts);
		}
		Experiment.date2 = new Date();
//		LOG.info("Done with E+ and E-");
		System.out.println("Time to find EWS (seconds): " + ((Experiment.date2.getTime() - Experiment.date1.getTime()) / 1000.0));
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
