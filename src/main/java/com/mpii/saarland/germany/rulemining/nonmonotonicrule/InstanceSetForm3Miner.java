package com.mpii.saarland.germany.rulemining.nonmonotonicrule;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mpii.saarland.germany.indexing.FactIndexer;
import com.mpii.saarland.germany.utils.TextFileReader;

/**
 * 
 * @author Hai Dang Tran
 * 
 * This class is to handle rules mined from pattern: P(x, y) ^ T(y) ^ Q(x, z) ^ R(x).
 */
public abstract class InstanceSetForm3Miner extends InstanceSetMiner {

	private static final Logger LOG = LoggerFactory.getLogger(InstanceSetForm3Miner.class);

	protected InstanceSetForm3Miner() {
	}

	@Override
	public void loadPositiveRules(String fileName) {
		List<String> lines = TextFileReader.readLines(fileName);
		for (String positiveRule : lines) {
			String[] parts = positiveRule.split("\t");
			positiveRules.add(parts[0] + "\t" + parts[1] + "\t" + parts[3] + "\t" + parts[2]);
		}
	}

	//  To be finished...
	@Override
	public List<Set<String>> findInstances(String positiveRule, FactIndexer facts) {
		String[] parts = positiveRule.split("\t");
		String p = parts[0];
		String t = parts[1];
		String r = parts[2];
		String q = parts[3];
		Set<String> xSet = facts.getXSetFromPt(p + "\t" + t);
		if (xSet == null) {
			return null;
		}
		Set<String> zSet = facts.getXSetFromT(r);
		if (zSet == null) {
			return null;
		}
		/*
		 * Do something
		 */
		return null;
	}

}
