package com.mpii.saarland.germany.rulemining.nonmonotonicrule;

import java.util.ArrayList;
import java.util.HashSet;
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
 * This class is to handle rules mined from pattern: P(x, y) ^ Q(y, z) -> H(x, z).
 */
public class InstanceSetForm2Miner extends InstanceSetMiner {

	private static final Logger LOG = LoggerFactory.getLogger(InstanceSetForm2Miner.class);

	public InstanceSetForm2Miner() {
	}

	@Override
	public void loadPositiveRules(String fileName) {
		List<String> lines = TextFileReader.readLines(fileName);
		for (String positiveRule : lines) {
			String[] parts = positiveRule.split("\t");
			positiveRules.add(parts[0] + "\t" + parts[1] + "\t" + parts[2]);
		}
	}

	@Override
	public List<Set<String>> findInstances(String positiveRule, FactIndexer facts) {
		String[] parts = positiveRule.split("\t");
		String p = parts[0];
		String q = parts[1];
		String h = parts[2];
		Set<String> normalExamples = new HashSet<>();
		Set<String> abnormalExamples = new HashSet<>();
		for (String yz : facts.getXySetFromP(q)) {
			String y = yz.split("\t")[0];
			String z = yz.split("\t")[1];
			Set<String> xSet = facts.getXSetFromPy(p + "\t" + y);
			if (xSet == null) {
				continue;
			}
			for (String x : xSet) {
				if (facts.checkXpy(x + "\t" + h + "\t" + z)) {
					normalExamples.add(x + "\t" + z);
				} else {
					abnormalExamples.add(x + "\t" + z);
				}
			}
		}
		List<Set<String>> result = new ArrayList<Set<String>>();
		result.add(normalExamples);
		result.add(abnormalExamples);
		return result;
	}

}
