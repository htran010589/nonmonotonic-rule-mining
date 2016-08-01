package com.mpii.saarland.germany.rulemining.nonmonotonicrule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mpii.saarland.germany.indexing.FactIndexer;
import com.mpii.saarland.germany.utils.TextFileReader;
import com.mpii.saarland.germany.utils.Utils;

/**
 * 
 * @author Hai Dang Tran
 * 
 * This class is to handle rules mined from pattern: P(x, y) ^ Q(y, z) -> H(x, z).
 */
public class InstanceSetForm2Miner extends InstanceSetMiner {

	private static final Logger LOG = LoggerFactory.getLogger(InstanceSetForm2Miner.class);

	protected Set<String> pqPatterns;

	public InstanceSetForm2Miner(FactIndexer facts) {
		super(facts);
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
	public void createPatterns() {
		pqPatterns = new HashSet<String>();
		for (String positiveRule : positiveRules) {
			String[] parts = positiveRule.split("\t");
			pqPatterns.add(parts[0] + "\t" + parts[1]);
		}
	}

	@Override
	public void findInstances() {
		Map<String, Set<String>> pattern2Instance = new HashMap<>();
		for (String fact : facts.getXpySet()) {
			String[] parts = fact.split("\t");
			String y = parts[0];
			String q = parts[1];
			String z = parts[2];
			Set<String> pxSet = facts.getPxSetFromY(y);
			if (pxSet == null) {
				continue;
			}
			for (String px : pxSet) {
				String p = px.split("\t")[0];
				String x = px.split("\t")[1];
				if (!pqPatterns.contains(p + "\t" + q)) {
					continue;
				}

				Utils.addKeyString(pattern2Instance, p + "\t" + q, x + "\t" + z);
				Set<String> hSet = facts.getPSetFromXy(x + "\t" + z);
				if (hSet == null) {
					continue;
				}
				for (String h : hSet) {
					if (!positiveRules.contains(p + "\t" + q + "\t" + h)) {
						continue;
					}

					Utils.addKeyString(pattern2Instance, p + "\t" + q + "\t" + h, x + "\t" + z);
				}
			}
		}
		for (String pattern : positiveRules) {
			String[] parts = pattern.split("\t");
			Set<String> bodyInstance = pattern2Instance.get(parts[0] + "\t" + parts[1]);
			if (bodyInstance == null) {
				continue;
			}
			Set<String> normalInstance = pattern2Instance.get(parts[0] + "\t" + parts[1] + "\t" + parts[2]); 
			if (normalInstance == null) {
				continue;
			}
			Set<String> abnormalInstance = new HashSet<>();
			for (String instance : bodyInstance) {
				if (normalInstance.contains(instance)) {
					continue;
				}
				abnormalInstance.add(instance);
			}
			rule2NormalSet.put(pattern, normalInstance);
			rule2AbnormalSet.put(pattern, abnormalInstance);
		}
		LOG.info("Done with normal and abnormal sets");
	}

}
