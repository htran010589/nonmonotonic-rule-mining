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
import com.mpii.saarland.germany.rules.ExceptionType;
import com.mpii.saarland.germany.rules.NegativeRule;
import com.mpii.saarland.germany.utils.Settings;
import com.mpii.saarland.germany.utils.TextFileReader;
import com.mpii.saarland.germany.utils.Utils;

public class Experiment {

	public static final String DOMAIN = "IMDB";

	public static final int[] MAXIMUM_COUNTS = { 5, 10 };

	public static final String[] TYPES = { ".neg.", ".pos.", ".neg.x2." };

	public static String idealData;

	public static String typeData;

	public static String encodeFile;

	public static String patternFileField;

	public static String trainData;

	public static String trainDataDlv;

	public static String choosenRuleFile;

	public static String extentionFile;

	public static String dlvBinFile;

	public static Date date1, date2, date3, date4;

	static {
		if (DOMAIN.equals("IMDB")) {
			// cai nay la danh cho IMDB
			idealData = "data/imdb.facts.tsv";
			typeData = null;
			encodeFile = "data/experiment/IMDB/imdb.mapping.data.txt";
			patternFileField = Settings.IMDB_FORM2_PATTERN_FILE_NAME;
			trainData = "data/experiment/IMDB/imdb.learning.data.txt";
			trainDataDlv = "data/experiment/IMDB/DLV/imdb.train.kg";
			choosenRuleFile = "data/experiment/IMDB/DLV/imdb.rule";
			dlvBinFile = "data/experiment/IMDB/DLV/dlv.bin";
			extentionFile = "data/experiment/IMDB/DLV/imdb.ext.kg";

			// cai nay danh cho freebase - chinh lai mot chut
//			idealData = "data/imdb.facts.tsv";
//			typeData = null;
//			encodeFile = "data/experiment/FreeBase/fb.mapping.data.txt";
//			patternFileField = "data/experiment/FreeBase/fb-train-pattern.txt";
//			trainData = "data/experiment/FreeBase/fb.learning.data.txt";
//			trainDataDlv = "data/experiment/FreeBase/DLV/fb.train.kg";
//			choosenRuleFile = "data/experiment/FreeBase/DLV/fb.rule";
//			dlvBinFile = "data/experiment/FreeBase/DLV/dlv.bin";
//			extentionFile = "data/experiment/FreeBase/DLV/fb.ext.kg";
		} else {
			idealData = "data/experiment/YAGO/yago2s.ideal.data.txt";
			typeData = "data/experiment/YAGO/yago2.type.txt";
			encodeFile = "data/experiment/YAGO/DLV/yago2s.mapping.data.txt";
			patternFileField = Settings.AMIE_YAGO_FORM2_PATTERN_FILE_NAME;
			trainData = "data/experiment/YAGO/yago2.training.data.txt";
			trainDataDlv = "data/experiment/YAGO/DLV/yago2.training.kg";
			choosenRuleFile = "data/experiment/YAGO/DLV/yago.rule";
			dlvBinFile = "data/experiment/YAGO/DLV/dlv.bin";
			extentionFile = "data/experiment/YAGO/DLV/yago.ext.kg";
		}
	}

	public FactIndexer facts, learningFacts;

	public static Map<String, String> entity2Id, id2Entity;

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
			BufferedReader br = new BufferedReader(new FileReader(trainData));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("<")) {
					line = line.substring(1, line.length() - 1);
					line = line.replaceAll(">\t<", "\t");
				}
				String[] parts = line.split("\t");
				learningFacts.indexFact(parts, 1L);
				learningFacts.indexPattern(parts, 1L);
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
		if (DOMAIN.equals("IMDB")) {
			facts = new FactIndexer(idealData);
		} else {
			facts = new FactIndexer(idealData);
		}
		for (String str : facts.getPSet()) {
			System.out.println(str);
		}
		System.out.println("Number of predicates in ideal graph: " + facts.getPSet().size());
		for (int i = 0; i < 2; ++i) {
			for (int maxCnt : MAXIMUM_COUNTS) {
				String extFile = extentionFile + TYPES[i] + maxCnt;
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
				String x = id2Entity.get(parts[1]);
				String y = id2Entity.get(parts[2]);
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

	public int numEntity, numType, numPredicate;

	public void encode(String x, int entityType) {
		if (entity2Id.containsKey(x))
			return;
		if (entityType == 0) {
			numEntity++;
			entity2Id.put(x, "e" + numEntity);
		} else if (entityType == 1) {
			numType++;
			entity2Id.put(x, "t" + numType);
		} else {
			numPredicate++;
			entity2Id.put(x, "p" + numPredicate);
		}
	}

	// Cai nay xong roi, it goi thoi
	public void encode() {
		entity2Id = new HashMap<String, String>();
		numEntity = numType = numPredicate = 0;
		// cai nay la danh cho imdb
//		facts = new FactIndexer(idealData);

		// cai nay la danh cho freebase
		facts = new FactIndexer(trainData);
		// sample2();
		for (String xpy : facts.getXpySet()) {
			String[] parts = xpy.split("\t");
			if (parts[1].equals("subClassOf")) {
				encode(parts[0], 1);
				encode(parts[2], 1);
			}
		}
		for (String x : facts.getXSet()) {
			Set<String> tSet = facts.getTSetFromX(x);
			encode(x, 0);
			if (tSet == null)
				continue;
			for (String t : tSet) {
				// System.out.println(x + "\t" + t);
				encode(t, 1);
			}
		}
		for (String xpy : facts.getXpySet()) {
			String[] parts = xpy.split("\t");
			encode(parts[0], 0);
			encode(parts[2], 0);
		}

		try {
			Writer wr = new PrintWriter(new File(encodeFile));
			for (String e : entity2Id.keySet()) {
				String id = entity2Id.get(e);
				wr.write(e + "\t" + id + "\n");
			}
			wr.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void encodeFreeBase() {
		entity2Id = new HashMap<String, String>();
		numEntity = numType = numPredicate = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(trainData));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("<")) {
					line = line.substring(1, line.length() - 1);
					line = line.replaceAll(">\t<", "\t");
				}
				String[] parts = line.split("\t");
				encode(parts[0], 0);
				encode(parts[1], 2);
				encode(parts[2], 0);
			}
			br.close();

			Writer wr = new PrintWriter(new File(encodeFile));
			for (String e : entity2Id.keySet()) {
				String id = entity2Id.get(e);
				wr.write(e + "\t" + id + "\n");
			}
			wr.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void genExceptions() {
		String patternFile = null;
		if (DOMAIN.equals("IMDB")) {
			learningFacts = new FactIndexer();
			readSample2();
			patternFile = patternFileField;
		} else {
			learningFacts = new FactIndexer(trainData);
			patternFile = patternFileField;
		}
		date1 = new Date();
		ExceptionRanker er = new ExceptionRanker(patternFile, learningFacts);
		er.rankRulesWithExceptions(false);

		/*
		try {
			for (String type : Experiment.TYPES) {
				for (int maxCnt : Experiment.MAXIMUM_COUNTS) {
					int cnt = 0;
					Writer wr = new PrintWriter(new File(Experiment.choosenRuleFile + type + maxCnt));
					double convSum = 0;
					for (NegativeRule negRule : er.getChoosenNegativeRules()) {
						cnt++;
						if (cnt > maxCnt) {
							break;
						}
						String[] parts = negRule.getPositiveRule().getBody().split("\t");
						String head = negRule.getPositiveRule().getHead();
						String posRule = entity2Id.get(head) + "(X, Z) :- " + entity2Id.get(parts[0]) + "(X, Y), " + entity2Id.get(parts[1]) + "(Y, Z)";
						String negation = "";
						if (negRule.getException().getType() == ExceptionType.FIRST) {
							negation = Experiment.entity2Id.get(negRule.getException().getException()) + "(X).";
						} else if (negRule.getException().getType() == ExceptionType.SECOND) {
							negation = Experiment.entity2Id.get(negRule.getException().getException()) + "(Z).";
						} else {
							negation = entity2Id.get(negRule.getException().getException()) + "(X, Z).";
						}
						if (type.equals(".neg.")) {
							wr.write(posRule + ", not " + negation + "\n");
							double conviction = negRule.getStandardConviction();
							convSum += conviction;
						} else if (type.equals(".pos.")) {
							wr.write(posRule + ".\n");
							double conviction = negRule.getPositiveRule().getConviction();
							convSum += conviction;
						} else {
							wr.write(posRule + ", not " + negation + "\n");
							wr.write("not_" + posRule + ", " + negation + "\n");
						}
					}
					wr.close();
					System.out.println("Done with " + Experiment.choosenRuleFile + type + maxCnt + " file");
					System.out.println("avg conv = " + (convSum / maxCnt));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		*/
		Experiment.date3 = new Date();
		System.out.println(date3.getTime());
	}

	public void loadEncode() throws Exception {
		entity2Id = new HashMap<>();
		id2Entity = new HashMap<>();
		BufferedReader br = new BufferedReader(new FileReader(encodeFile));
		String line;
		while ((line = br.readLine()) != null) {
			String[] parts = line.split("\t");
			entity2Id.put(parts[0], parts[1]);
			id2Entity.put(parts[1], parts[0]);
		}
		br.close();
	}

	public void convert2DlvKg() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(Experiment.trainData));
			Writer wr = new PrintWriter(new File(Experiment.trainDataDlv));
			String line;
			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				if (line.startsWith("<")) {
					line = line.substring(1, line.length() - 1);
					line = line.replaceAll(">\t<", "\t");
				}
				String[] parts = null;
				parts = line.split("\t");
				if (entity2Id.get(parts[0]) == null) {
					continue;
				}
				if (entity2Id.get(parts[2]) == null) {
					continue;
				}
				if (!parts[1].equals("type")) {
					wr.write(entity2Id.get(parts[1]) + "(" + entity2Id.get(parts[0]) + ", " + entity2Id.get(parts[2]) + ").\n");
				} else {
					wr.write(entity2Id.get(parts[2]) + "(" + entity2Id.get(parts[0]) + ").\n");
				}
			}
			br.close();

			if (!DOMAIN.equals("IMDB")) {
				br = new BufferedReader(new FileReader(typeData));
				while ((line = br.readLine()) != null) {
					line = line.substring(1, line.length() - 1);
					String[] parts = line.split(">\t<");
					if (entity2Id.get(parts[0]) == null) {
						continue;
					}
					if (entity2Id.get(parts[2]) == null) {
						continue;
					}
					wr.write(entity2Id.get(parts[2]) + "(" + entity2Id.get(parts[0]) + ").\n");
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
		if (DOMAIN.equals("IMDB")) {
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
			for (int maxCnt : MAXIMUM_COUNTS) {
				String file1 = extentionFile + ".diff." + maxCnt + ".needcheck";
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
		Map<String, String> ma = getYagoEvals("data/experiment/YAGO/DLV/evaluations/std-confidence.tsv");
		ma.putAll(getYagoEvals("data/experiment/YAGO/DLV/evaluations/pca-confidence.tsv"));
		ma.putAll(getYagoEvals("data/experiment/YAGO/DLV/evaluations/joint-prediction.tsv"));
		for (int maxCnt : MAXIMUM_COUNTS) {
			int cnt = 0;
			String file1 = extentionFile + ".pos." + maxCnt + ".needcheck";
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
		for (String type : TYPES) {
			for (int maxCnt : MAXIMUM_COUNTS) {
				String ruleFile = choosenRuleFile + type + maxCnt;
				String extFile = extentionFile + type + maxCnt;
				String command = dlvBinFile + " -nofacts " + trainDataDlv + " " + ruleFile;
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
		System.out.println(
				"Time for DLV (seconds): " + ((Experiment.date4.getTime() - Experiment.date3.getTime()) / 1000.0));
	}

	public void decodeDlvOutput() throws Exception {
//		for (int maxCnt : MAXIMUM_COUNTS) {
		int maxCnt = 10;
			int ret = 0;
			String extFile = extentionFile + TYPES[2] + maxCnt;
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
				String[] entities = fact.split("\\(|\\)|,");
				System.out.println("<" + id2Entity.get(entities[1]) + ">\t<" + id2Entity.get(entities[0]) + ">\t<" + id2Entity.get(entities[2]) + ">");
			}
//		}
	}

	public void calConflict() throws Exception {
		List<String> lines = TextFileReader.readLines(trainDataDlv);
		Set<String> oriFacts = new HashSet<>();
		for (String line : lines) {
			line = line.substring(0, line.length() - 1).replaceAll("\\s+", "");
//			System.out.println(line);
			oriFacts.add(line);
		}

		Writer wr = new PrintWriter(new File("data/experiment/IMDB/DLV/imdb.ext.kg.neg.x2.10.decode"));
//		for (int maxCnt : MAXIMUM_COUNTS) {
			int ret = 0;
//			String extFile = extentionFile + TYPES[2] + maxCnt;
			String extFile = "data/experiment/IMDB/DLV/imdb.ext.kg.neg.x2.10";
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
				if (oriFacts.contains(fact)) {
					System.out.println("***" + fact);
					continue;
				}
				if (!fact.startsWith("not_")) {
					sx.add(fact);
				} else {
					sy.add(fact.substring("not_".length()));
				}
			}
			for (String x : sx) {
				if (sy.contains(x)) {
//					System.out.println(x);
					String[] entities = x.split("\\(|\\)|,");
					wr.write("<" + id2Entity.get(entities[1]) + ">\t<" + entities[0] + ">\t<" + id2Entity.get(entities[2]) + ">\n");
					wr.write("<" + id2Entity.get(entities[1]) + ">\tNOT_<" + entities[0] + ">\t<" + id2Entity.get(entities[2]) + ">\n");
					ret++;
				}
			}
			System.out.println("With file: " + extFile);
			System.out.println("Total number of conflicts: " + ret);
			System.out.println("Total number of facts without 'not_' prefix: " + sx.size());
			System.out.println("Total number of facts with 'not_' prefix: " + sy.size());
			System.out.println("-----");
//		}
		wr.close();
	}

	public void findDiff() throws Exception {
		for (int maxCnt : MAXIMUM_COUNTS) {
			String file1 = extentionFile + ".neg." + maxCnt + ".needcheck";
			String file2 = extentionFile + ".pos." + maxCnt + ".needcheck";
			List<String> lx = TextFileReader.readLines(file1);
			Set<String> sx = new HashSet<String>(lx);
			Writer wr = new PrintWriter(new File(extentionFile + ".diff." + maxCnt + ".needcheck"));
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
		for (int maxCnt : MAXIMUM_COUNTS) {
			String file1 = extentionFile + ".neg." + maxCnt + ".needcheck";
			String file2 = extentionFile + ".pos." + maxCnt + ".needcheck";
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
//			encodeFreeBase();
//			convert2DlvKg();
//			encode();
			loadEncode();
//			decodeDlvOutput();
//			genExceptions();
//			runDlv();
//			evaluate();
			calConflict();
//			findDiff();
			// compareWithEvals();
			// checkSubsetDLV();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		new Experiment().conduct();
	}

}
