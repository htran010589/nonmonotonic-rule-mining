package com.mpii.saarland.germany.rulemining.patternmining;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mpii.saarland.germany.indexing.FactIndexer;
import com.mpii.saarland.germany.utils.Utils;

/**
 * 
 * This class is to mine patterns of the form: h(X, Z) <- p(X, Y) ^ q(Y, Z)
 */
public class PatternForm1Miner {

	/**
	 * If type = 0, we mine patterns based on #(X, Z), otherwise #(X, Y, Z).
	 */
	public static void minePatterns(String factFileName) {
		FactIndexer facts = new FactIndexer(factFileName);
		Map<String, Long> pattern2Long = new HashMap<>();
		Map<String, Set<String>> pattern2Pair = new HashMap<>();
		int count = 0;
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
				Set<String> hSet = facts.getPSetFromXy(x + "\t" + z);
				if (hSet == null) {
					continue;
				}
				for (String h : hSet) {
					if (h.equals(p) && p.equals(q)) {
						continue;
					}
					Utils.addKeyLong(pattern2Long, h + "\t" + p + "\t" + q, 1);
				}
			}
			count++;
			if (count % 10000 == 0) {
				System.out.print(".");
			}
		}
		System.out.println();
		List<String> topPatterns = Utils.getTopK(pattern2Long, pattern2Long.size());
		for (String pattern : topPatterns) {
			System.out.println(pattern);
		}
	}

}
