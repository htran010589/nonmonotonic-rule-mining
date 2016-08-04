package com.mpii.saarland.germany.experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mpii.saarland.germany.indexing.FactIndexer;
import com.mpii.saarland.germany.rulemining.nonmonotonicrule.ExceptionRanker;
import com.mpii.saarland.germany.utils.Settings;
import com.mpii.saarland.germany.utils.TextFileReader;
import com.mpii.saarland.germany.utils.Utils;

public class Experiment {

	public static final String MOD = "IMDB";

	public static final int[] maxCnts = { 5, 10, 15, 20 };

	public static final String[] types = { ".neg.", ".pos.", ".neg.x2." };

	public static String IDEAL_DATA;

	public static String TYPE_DATA;

	public static String ENCODE_FILE;

	public static String TRAIN_DATA;

	public static String TRAIN_DATA_DLV;

	public static String RULE_FILE;

	public static String EXT_FILE;

	public static String DLV_BIN_FILE;

	public static Date date1, date2, date3, date4;

	static {
		if (MOD.equals("IMDB")) {
			IDEAL_DATA = "data/imdb.facts.tsv";
			TYPE_DATA = null;
			ENCODE_FILE = "data/experiment/IMDB/imdb.mapping.data.txt";
			TRAIN_DATA = "data/experiment/IMDB/imdb.learning.data.txt";
			TRAIN_DATA_DLV = "data/experiment/IMDB/DLV/imdb.train.kg";
			RULE_FILE = "data/experiment/IMDB/DLV/imdb.rule";
			DLV_BIN_FILE = "data/experiment/IMDB/DLV/dlv.bin";
			EXT_FILE = "data/experiment/IMDB/DLV/imdb.ext.kg";
		} else {
			IDEAL_DATA = "data/experiment/YAGO/yago2s.ideal.data.txt";
			TYPE_DATA = "data/experiment/YAGO/yago2.type.txt";
			ENCODE_FILE = "data/experiment/YAGO/DLV/yago2s.mapping.data.txt";
			TRAIN_DATA = "data/experiment/YAGO/yago2.training.data.txt";
			TRAIN_DATA_DLV = "data/experiment/YAGO/DLV/yago2.training.kg";
			RULE_FILE = "data/experiment/YAGO/DLV/yago.rule";
			DLV_BIN_FILE = "data/experiment/YAGO/DLV/dlv.bin";
			EXT_FILE = "data/experiment/YAGO/DLV/yago.ext.kg";
		}
	}

	public FactIndexer facts, learningFacts;

	public static Map<String, String> toID, fromID;

	public void sample2() {
		try {
			Writer wr = new PrintWriter(new File("yago.learning.data.txt"));
			Map<String, Long> pCnt = new HashMap<String, Long>();
			Map<String, Long> delPCnt = new HashMap<String, Long>();
			Map<String, Long> deg = new HashMap<String, Long>();
			for (String xpy : facts.getXpySet()) {
				String[] parts = xpy.split("\t");
				Utils.addKeyLong(deg, parts[0], 1);
				Utils.addKeyLong(deg, parts[2], 1);
				Utils.addKeyLong(pCnt, parts[1], 1);
				Utils.addKeyLong(delPCnt, parts[1], 1);
			}
			for (String xpy : facts.getXpySet()) {
				String[] parts = xpy.split("\t");
				boolean del = true;
				if (deg.get(parts[0]) <= 1)
					del = false;
				if (deg.get(parts[2]) <= 1)
					del = false;
				if ((1.0 * (delPCnt.get(parts[1]) - 1) / pCnt.get(parts[1])) < 0.8)
					del = false;
				if (!del) {
					wr.write(xpy + "\n");
				} else {
					Utils.addKeyLong(deg, parts[0], -1);
					Utils.addKeyLong(deg, parts[2], -1);
					Utils.addKeyLong(delPCnt, parts[1], -1);
				}
			}
			for (String x : facts.getXSet()) {
				if (facts.getTSetFromX(x) == null)
					continue;
				for (String t : facts.getTSetFromX(x)) {
					wr.write(x + "\ttype\t" + t + "\n");
				}
			}
			wr.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void readSample2() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(TRAIN_DATA));
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split("\t");
				learningFacts.indexFact(parts);
				learningFacts.indexPattern(parts);
				// System.out.println(parts[1] + "(" + facts.getId(parts[0]) +
				// ", " + facts.getId(parts[2]) + ").");
			}
			br.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// for (String e : facts.getEntities()) {
		// String id = facts.getId(e);
		// System.out.println("encoding: " + e + "\t" + id);
		// }
	}

	public void evaluate() {
		if (MOD.equals("IMDB")) {
			facts = new FactIndexer(IDEAL_DATA);
		} else {
			facts = new FactIndexer(IDEAL_DATA);
		}
		for (String str : facts.getPSet()) {
			System.out.println(str);
		}
		System.out.println("Number of predicates in ideal graph: " + facts.getPSet().size());
		for (int i = 0; i < 2; ++i) {
			for (int maxCnt : maxCnts) {
				String extFile = EXT_FILE + types[i] + maxCnt;
				evaluate(extFile);
			}
		}
	}

	public void evaluate(String fileName) {
		try {
			Writer wr1 = new PrintWriter(new File(fileName + ".good"));
			Writer wr2 = new PrintWriter(new File(fileName + ".needcheck"));

			int cnt1 = 0;
			int cnt2 = 0;
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			br.readLine();
			br.readLine();
			int cnt0 = 0;
			String line = br.readLine();
			String[] facts1 = line.split(", ");
			for (String fact : facts1) {
				// System.out.println(fact);
				if (fact.startsWith("{")) {
					fact = fact.substring(1);
				}
				if (fact.endsWith("}")) {
					fact = fact.substring(0, fact.length() - 1);
				}
				String[] parts = fact.split("\\(|\\)|,");
				// System.out.println(parts[0] + "\t" + parts[1] + "\t" +
				// parts[2]);
				String p = parts[0];
				String x = fromID.get(parts[1]);
				String y = fromID.get(parts[2]);
				String xpy = x + "\t" + p + "\t" + y;
				if (learningFacts.checkXpy(xpy)) {
					cnt0++;
					continue;
				}
				if (facts.checkXpy(xpy)) {
					cnt1++;
					wr1.write(xpy + "\n");
				} else {
					wr2.write(xpy + "\n");
					cnt2++;
				}
			}
			br.close();
			System.out.println("Already in the learning data: " + cnt0);
			System.out.println("Good examples: " + cnt1);
			System.out.println("Need to check: " + cnt2);
			System.out.println("Done with file " + fileName);
			System.out.println("-----");
			wr1.close();
			wr2.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public int numEntity, numType;

	public void encode(String x, boolean isEntity) {
		if (toID.containsKey(x)) return;
		if (isEntity) {
			numEntity++;
			toID.put(x, "e" + numEntity);
		} else {
			numType++;
			toID.put(x, "t" + numType);
		}
	}

	// Cai nay xong roi, it goi thoi
	public void encode() {
		toID = new HashMap<String, String>();
		numEntity = numType = 0;
		facts = new FactIndexer(IDEAL_DATA);
		// sample2();
		for (String xpy : facts.getXpySet()) {
			String[] parts = xpy.split("\t");
			if (parts[1].equals("subClassOf")) {
				encode(parts[0], false);
				encode(parts[2], false);
			}
		}
		for (String x : facts.getXSet()) {
			Set<String> tSet = facts.getTSetFromX(x);
			encode(x, true);
			if (tSet == null)
				continue;
			for (String t : tSet) {
				// System.out.println(x + "\t" + t);
				encode(t, false);
			}
		}
		for (String xpy : facts.getXpySet()) {
			String[] parts = xpy.split("\t");
			encode(parts[0], true);
			encode(parts[2], true);
		}

		try {
			Writer wr = new PrintWriter(new File("data/experiment/IMDB/imdb.mapping.data.txt"));
			for (String e : toID.keySet()) {
				String id = toID.get(e);
				wr.write(e + "\t" + id + "\n");
			}
			wr.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void genExceptions() {
		String patternFile = null;
		if (MOD.equals("IMDB")) {
			learningFacts = new FactIndexer();
			readSample2();
			patternFile = Settings.IMDB_FORM2_PATTERN_FILE_NAME;
		} else {
			learningFacts = new FactIndexer(TRAIN_DATA);
			patternFile = Settings.AMIE_YAGO_FORM2_PATTERN_FILE_NAME;
		}
		date1 = new Date();
		ExceptionRanker er = new ExceptionRanker(patternFile, learningFacts);
		er.rankRulesWithExceptions();

		try {
			for (String type : Experiment.types) {
				for (int maxCnt : Experiment.maxCnts) {
					int cnt = 0;
					Writer wr = new PrintWriter(new File(Experiment.RULE_FILE + type + maxCnt));
					double convSum = 0;
					for (String negRule : er.getNegativeRules()) {
						cnt++;
						if (cnt > maxCnt) {
							break;
						}
						String[] parts = negRule.split("\t");
						String posRule = parts[0] + "(X, Z) :- " + parts[1] + "(X, Y), " + parts[2] + "(Y, Z)";
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
							double conviction = er.getNegativeRuleConviction(negRule);
							convSum += conviction;
						} else if (type.equals(".pos.")) {
							wr.write(posRule + ".\n");
							double conviction = er.getPositiveRuleConviction(parts[0] + "\t" + parts[1] + "\t" + parts[2]);
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
		System.out.println(date3.getTime());
	}

	public void loadEncode() throws Exception {
		toID = new HashMap<>();
		fromID = new HashMap<>();
		BufferedReader br = new BufferedReader(new FileReader(ENCODE_FILE));
		String line;
		while ((line = br.readLine()) != null) {
			String[] parts = line.split("\t");
			toID.put(parts[0], parts[1]);
			fromID.put(parts[1], parts[0]);
		}
		br.close();
	}

	public void convert2DlvKg() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(Experiment.TRAIN_DATA));
			Writer wr = new PrintWriter(new File(Experiment.TRAIN_DATA_DLV));
			String line;
			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				String[] parts = null;
				if (MOD.equals("IMDB")) {
					parts = line.split("\t");
				} else {
					line = line.substring(1, line.length() - 1);
					parts = line.split(">\t<");
				}
				if (toID.get(parts[0]) == null) {
					continue;
				}
				if (toID.get(parts[2]) == null) {
					continue;
				}
				if (!parts[1].equals("type")) {
					wr.write(parts[1] + "(" + toID.get(parts[0]) + ", " + toID.get(parts[2]) + ").\n");
				} else {
					wr.write(toID.get(parts[2]) + "(" + toID.get(parts[0]) + ").\n");
				}
			}
			br.close();
			if (!MOD.equals("IMDB")) {
				br = new BufferedReader(new FileReader(TYPE_DATA));
				while ((line = br.readLine()) != null) {
					line = line.substring(1, line.length() - 1);
					String[] parts = line.split(">\t<");
					if (toID.get(parts[0]) == null) {
						continue;
					}
					if (toID.get(parts[2]) == null) {
						continue;
					}
					wr.write(toID.get(parts[2]) + "(" + toID.get(parts[0]) + ").\n");
				}
				br.close();
			}
			wr.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

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
		if (MOD.equals("IMDB")) {
			List<String> lines = TextFileReader.readLines("/home/htran/Research_Work/Code/nonmonotonic-rule-mining/data/experiment/IMDB/imdb.manual.eval.txt");
			Map<String, String> check = new HashMap<String, String>();
			for (String line : lines) {
//				System.out.println(line);
				String[] parts = line.split("\t");
//				System.out.println(line);
				if (parts.length < 4) continue;
				check.put(parts[0] + "\t" + parts[1] + "\t" + parts[2], parts[3]);
			}
			for (int maxCnt : maxCnts) {
				String file1 = EXT_FILE + ".diff." + maxCnt + ".needcheck";
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
			return;
		}
		Map<String, String> ma = getYagoEvals(
				"/home/htran/Research_Work/Code/nonmonotonic-rule-mining/data/experiment/YAGO/DLV/evaluations/std-confidence.tsv");
		ma.putAll(getYagoEvals(
				"/home/htran/Research_Work/Code/nonmonotonic-rule-mining/data/experiment/YAGO/DLV/evaluations/pca-confidence.tsv"));
		ma.putAll(getYagoEvals(
				"/home/htran/Research_Work/Code/nonmonotonic-rule-mining/data/experiment/YAGO/DLV/evaluations/joint-prediction.tsv"));
		for (int maxCnt : maxCnts) {
			int cnt = 0;
			String file1 = EXT_FILE + ".pos." + maxCnt + ".needcheck";
			BufferedReader br = new BufferedReader(new FileReader(file1));
			String line;
			while ((line = br.readLine()) != null) {
				if (ma.containsKey(line)) {
					cnt++;
				}
			}
			br.close();
			System.out.println("With file: " + file1 + ", number of facts contained in AMIE evalution is " + cnt);
		}
	}

	public void runDlv() throws Exception {
		for (String type : types) {
			for (int maxCnt : maxCnts) {
				String ruleFile = RULE_FILE + type + maxCnt;
				String extFile = EXT_FILE + type + maxCnt;
				String command = DLV_BIN_FILE + " -nofacts " + TRAIN_DATA_DLV + " " + ruleFile;
				Writer wr = new PrintWriter(new File(extFile));
				Process p = Runtime.getRuntime().exec(command);
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					wr.write(line + "\n");
				}
				p.waitFor();
				System.out.println("exit: " + p.exitValue());
				p.destroy();
				wr.close();
				System.out.println("Done with " + extFile + " file");
			}
		}
		Experiment.date4 = new Date();
		System.out.println("Time for DLV (seconds): " + ((Experiment.date4.getTime() - Experiment.date3.getTime()) / 1000.0));
	}

	public void calConflict() throws Exception {
		for (int maxCnt : maxCnts) {
			int ret = 0;
			String extFile = EXT_FILE + types[2] + maxCnt;
			BufferedReader br = new BufferedReader(new FileReader(extFile));
			br.readLine();
			br.readLine();
			String line = br.readLine();
			String[] facts1 = line.split(", ");
			Set<String> sx = new HashSet<String>();
			Set<String> sy = new HashSet<String>();
			for (String fact : facts1) {
				if (fact.startsWith("{")) {
					fact = fact.substring(1);
				}
				if (fact.endsWith("}")) {
					fact = fact.substring(0, fact.length() - 1);
				}
				if (!fact.startsWith("not_")) {
					sx.add(fact);
				} else {
					sy.add(fact.substring("not_".length()));
				}
			}
			for (String y : sy) {
				if (sx.contains(y)) {
					ret++;
				}
			}
			System.out.println("With file: " + extFile);
			System.out.println("Total number of conflicts: " + ret);
			System.out.println("Total number of facts without 'not_' prefix: " + sx.size());
			System.out.println("Total number of facts with 'not_' prefix: " + sy.size());
			System.out.println("-----");
		}
	}

	public void findDiff() throws Exception {
		for (int maxCnt : maxCnts) {
			String file1 = EXT_FILE + ".neg." + maxCnt + ".needcheck";
			String file2 = EXT_FILE + ".pos." + maxCnt + ".needcheck";
			List<String> lx = TextFileReader.readLines(file1);
			Set<String> sx = new HashSet<String>(lx);
			Writer wr = new PrintWriter(new File(EXT_FILE + ".diff." + maxCnt + ".needcheck"));
			List<String> ly = TextFileReader.readLines(file2);
			for (String y : ly) {
				if (sx.contains(y))
					continue;
				wr.write(y + "\n");
			}
			wr.close();
		}
	}

	void checkSubsetDLV() throws Exception {
		for (int maxCnt : maxCnts) {
			String file1 = EXT_FILE + ".neg." + maxCnt + ".needcheck";
			String file2 = EXT_FILE + ".pos." + maxCnt + ".needcheck";
			List<String> lx = TextFileReader.readLines(file1);
			List<String> ly = TextFileReader.readLines(file2);
			Set<String> sy = new HashSet<String>(ly);
			for (String x : lx) {
				if (sy.contains(x))
					continue;
				System.out.println("sai roi");
				break;
			}
		}		
	}

	public void conduct() {
		try {
			loadEncode();
			genExceptions();
			runDlv();
			evaluate();
			calConflict();
			findDiff();
//			compareWithEvals();

			
//			checkSubsetDLV();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}

