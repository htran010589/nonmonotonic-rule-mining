package com.mpii.saarland.germany.experiment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mpii.saarland.germany.utils.TextFileReader;

public class ManualChecker {

	public Map<String, String> getYagoEvals(String fileName) {
		Map<String, String> ret = new HashMap<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				String[] parts = line.split("\t");
				if (parts.length < 6)
					continue;
				String x = parts[1].substring(1, parts[1].length() - 1);
				String p = parts[2].substring(1, parts[2].length() - 1);
				String y = parts[3].substring(1, parts[3].length() - 1);
				String xpy = x + "\t" + p + "\t" + y;
				// System.out.println(parts[4]);
				ret.put(xpy, parts[5]);
			}
			br.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}

	public void compareWithEvals() throws Exception {
		List<String> lines = TextFileReader.readLines(
				"/home/htran/Research_Work/Code/nonmonotonic-rule-mining/data/experiment/IMDB/imdb.manual.eval.txt");
		Map<String, String> check = new HashMap<String, String>();
		for (String line : lines) {
			// System.out.println(line);
			String[] parts = line.split("\t");
			// System.out.println(line);
			if (parts.length < 4)
				continue;
			check.put(parts[0] + "\t" + parts[1] + "\t" + parts[2], parts[3]);
		}
		for (int maxCnt : Conductor.TOP_RULE_COUNTS) {
			String file1 = Conductor.extensionPrefixFileName + ".diff." + maxCnt + ".needcheck";
			BufferedReader br = new BufferedReader(new FileReader(file1));
			String line;
			int good = 0;
			int bad = 0;
			int unknown = 0;
			while ((line = br.readLine()) != null) {
				if (check.get(line) == null) {
					unknown++;
				}
				if (check.get(line).equals("1")) {
					good++;
				} else {
					bad++;
				}
			}
			br.close();
			System.out.println("With file: " + file1);
			System.out.println("Number of good facts is " + good);
			System.out.println("Number of bad facts is " + bad);
			System.out.println("Number of unknown facts is " + unknown);
			System.out.println("Rate = " + (good * 1.0) / (good + bad));
			System.out.println("+++++");
		}

//		Map<String, String> ma = getYagoEvals("data/experiment/YAGO/DLV/evaluations/std-confidence.tsv");
//		ma.putAll(getYagoEvals("data/experiment/YAGO/DLV/evaluations/pca-confidence.tsv"));
//		ma.putAll(getYagoEvals("data/experiment/YAGO/DLV/evaluations/joint-prediction.tsv"));
//		for (int maxCnt : Conductor.TOP_RULE_COUNTS) {
//			int cnt = 0;
//			String file1 = Conductor.extensionPrefixFileName + ".pos." + maxCnt + ".needcheck";
//			BufferedReader br = new BufferedReader(new FileReader(file1));
//			String line;
//			while ((line = br.readLine()) != null) {
//				if (ma.containsKey(line)) {
//					cnt++;
//				}
//			}
//			br.close();
//			System.out.println("With file: " + file1 + ", number of facts contained in AMIE evalution is " + cnt);
//		}
	}

}
