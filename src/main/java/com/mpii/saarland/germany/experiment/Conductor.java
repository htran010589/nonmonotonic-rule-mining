package com.mpii.saarland.germany.experiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mpii.saarland.germany.indexing.FactIndexer;
import com.mpii.saarland.germany.rulemining.nonmonotonicrule.ExceptionRanker;
import com.mpii.saarland.germany.rulemining.nonmonotonicrule.RankingType;
import com.mpii.saarland.germany.rules.ExceptionType;
import com.mpii.saarland.germany.rules.NegativeRule;
import com.mpii.saarland.germany.utils.TextFileReader;

/**
 * 
 * @author Hai Dang Tran
 * 
 */
public class Conductor {

	static final int[] TOP_RULE_COUNTS = { 5, 10, 15 };

	static final String[] RULE_TYPES = { ".pos.", ".neg.", ".neg.x2." };

	static String idealDataFileName;

	static String encodeFileName;

	static String patternFileName;

	static String trainingDataFileName;

	static String trainingDataDlvFileName;

	static String choosenRuleFileName;

	static String extensionPrefixFileName;

	static String dlvBinaryFileName;

	static Date time1, time2, time3, time4;

	static FactIndexer idealFacts, learningFacts;

	static void evaluate() {
		idealFacts = new FactIndexer(idealDataFileName);
		for (String predicate : idealFacts.getPSet()) {
			System.out.println(predicate);
		}
		System.out.println("Number of predicates in ideal graph: " + idealFacts.getPSet().size());

		for (int i = 0; i < 3; ++i) {
			for (int topRuleCount : TOP_RULE_COUNTS) {
				String extensionFileName = extensionPrefixFileName + RULE_TYPES[i] + topRuleCount;
				evaluate(extensionFileName);
			}
		}
	}

	static void evaluate(String fileName) {
		try {
			Writer goodFactWriter = new BufferedWriter(new FileWriter(fileName + ".good"));
			Writer needCheckFactWriter = new BufferedWriter(new FileWriter(fileName + ".needcheck"));
			Writer conflictWriter = new BufferedWriter(new FileWriter(fileName + ".conflict"));
			int inLearningPositiveFactCount = 0;
			int inLearningNegativeFactCount = 0;
			int goodFactCount = 0;
			int needCheckFactCount = 0;
			Set<String> positiveNewFacts = new HashSet<>();
			Set<String> negativeNewFacts = new HashSet<>();
			System.out.println("Start evaluating file: " + fileName);
			String line = TextFileReader.readLines(fileName).get(2);
			String[] facts = line.split(", ");
			for (String fact : facts) {
				if (fact.startsWith("{")) {
					fact = fact.substring(1);
				}
				if (fact.endsWith("}")) {
					fact = fact.substring(0, fact.length() - 1);
				}
				String[] parts = fact.split("\\(|\\)|,");
				if (!parts[0].startsWith("not_")) {
					String p = Encoder.id2Entity.get(parts[0]);
					String x = Encoder.id2Entity.get(parts[1]);
					String y = Encoder.id2Entity.get(parts[2]);
					String xpy = x + "\t" + p + "\t" + y;
					if (learningFacts.checkXpy(xpy)) {
						inLearningPositiveFactCount++;
						continue;
					}
					positiveNewFacts.add(xpy);
					if (idealFacts.checkXpy(xpy)) {
						goodFactCount++;
						goodFactWriter.write(xpy + "\n");
					} else {
						needCheckFactWriter.write(xpy + "\n");
						needCheckFactCount++;
					}
				} else {
					String p = Encoder.id2Entity.get(parts[0].substring("not_".length()));
					String x = Encoder.id2Entity.get(parts[1]);
					String y = Encoder.id2Entity.get(parts[2]);
					String xpy = x + "\t" + p + "\t" + y;
					if (learningFacts.checkXpy(xpy)) {
						inLearningNegativeFactCount++;
						continue;
					}
					negativeNewFacts.add(xpy);
				}
			}
			int conflictCount = 0;
			for (String fact : positiveNewFacts) {
				if (negativeNewFacts.contains(fact)) {
					String[] parts = fact.split("\t");
					conflictWriter.write("<" + parts[0] + ">\t<" + parts[1] + ">\t<" + parts[2] + ">\n");
					conflictWriter.write("<" + parts[0] + ">\t<not_" + parts[1] + ">\t<" + parts[2] + ">\n");
					conflictCount++;
				}
			}
			System.out.println("Already in the learning data (positive facts): " + inLearningPositiveFactCount);
			System.out.println("Already in the learning data (negative facts): " + inLearningNegativeFactCount);
			System.out.println("Total new predicted facts: " + (goodFactCount + needCheckFactCount));
			System.out.println("Good predicted facts in ideal graph: " + goodFactCount);
			System.out.println("Facts that need to check: " + needCheckFactCount);
			System.out.println("Positive new facts: " + positiveNewFacts.size());
			System.out.println("Negative new facts: " + negativeNewFacts.size());
			System.out.println("Number of conflicts: " + conflictCount);
			System.out.println("Done with file: " + fileName);
			System.out.println("-----");
			goodFactWriter.close();
			needCheckFactWriter.close();
			conflictWriter.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	static void generateExceptions(RankingType type) {
		learningFacts = Sampler.indexLearningData();
		time1 = new Date();
		ExceptionRanker ranker = new ExceptionRanker(patternFileName, learningFacts);
		ranker.rankRulesWithExceptions(type);

		// This is to convert rule set to DLV format.
		try {
			for (String ruleType : Conductor.RULE_TYPES) {
				for (int topRuleCount : Conductor.TOP_RULE_COUNTS) {
					int count = 0;
					Writer ruleWriter = new BufferedWriter(
							new FileWriter(Conductor.choosenRuleFileName + ruleType + topRuleCount));
					Writer decodedRuleWriter = new BufferedWriter(
							new FileWriter(Conductor.choosenRuleFileName + ruleType + topRuleCount + ".decode"));
					double convictionSum = 0;
					for (NegativeRule negativeRule : ranker.getChoosenNegativeRules()) {
						count++;
						if (count > topRuleCount) {
							break;
						}
						String[] parts = negativeRule.getPositiveRule().getBody().split("\t");
						String head = negativeRule.getPositiveRule().getHead();
						String positiveRule = Encoder.entity2Id.get(head) + "(X, Z) :- "
								+ Encoder.entity2Id.get(parts[0]) + "(X, Y), " + Encoder.entity2Id.get(parts[1])
								+ "(Y, Z)";
						String decodedPositiveRule = head + "(X, Z) <- " + parts[0] + "(X, Y) ^ " + parts[1] + "(Y, Z)";
						String negation = "";
						String decodedNegation = "";
						if (negativeRule.getException().getType() == ExceptionType.FIRST) {
							negation = Encoder.entity2Id.get(negativeRule.getException().getException()) + "(X).";
							decodedNegation = negativeRule.getException().getException() + "(X).";
						} else if (negativeRule.getException().getType() == ExceptionType.SECOND) {
							negation = Encoder.entity2Id.get(negativeRule.getException().getException()) + "(Z).";
							decodedNegation = negativeRule.getException().getException() + "(Z).";
						} else {
							negation = Encoder.entity2Id.get(negativeRule.getException().getException()) + "(X, Z).";
							decodedNegation = negativeRule.getException().getException() + "(X, Z).";
						}
						if (ruleType.equals(".neg.")) {
							ruleWriter.write(positiveRule + ", not " + negation + "\n");
							decodedRuleWriter.write(decodedPositiveRule + " ^ not " + decodedNegation + "\n");
							double conviction = negativeRule.getStandardConviction();
							convictionSum += conviction;
						} else if (ruleType.equals(".pos.")) {
							ruleWriter.write(positiveRule + ".\n");
							decodedRuleWriter.write(decodedPositiveRule + ".\n");
							double conviction = negativeRule.getPositiveRule().getConviction();
							convictionSum += conviction;
						} else {
							ruleWriter.write(positiveRule + ", not " + negation + "\n");
							decodedRuleWriter.write(decodedPositiveRule + " ^ not " + decodedNegation + "\n");
							ruleWriter.write("not_" + positiveRule + ", " + negation + "\n");
							decodedRuleWriter.write("not_" + decodedPositiveRule + " ^ " + decodedNegation + "\n");
						}
					}
					ruleWriter.close();
					decodedRuleWriter.close();
					System.out
							.println("Done with " + Conductor.choosenRuleFileName + ruleType + topRuleCount + " file");
					System.out.println("Average conviction = " + (convictionSum / topRuleCount));
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		Conductor.time3 = new Date();
		System.out.println(time3.getTime());
	}

	static void runDlv() {
		try {
			for (String ruleType : RULE_TYPES) {
				for (int topRuleCount : TOP_RULE_COUNTS) {
					String ruleFileName = choosenRuleFileName + ruleType + topRuleCount;
					String extensionFileName = extensionPrefixFileName + ruleType + topRuleCount;
					String command = dlvBinaryFileName + " -nofacts " + trainingDataDlvFileName + " " + ruleFileName;
					Writer dlvWriter = new BufferedWriter(new FileWriter(extensionFileName));
					Process dlvExecutor = Runtime.getRuntime().exec(command);
					BufferedReader dlvReader = new BufferedReader(new InputStreamReader(dlvExecutor.getInputStream()));
					String line;
					while ((line = dlvReader.readLine()) != null) {
						dlvWriter.write(line + "\n");
					}
					dlvExecutor.waitFor();
					dlvExecutor.destroy();
					dlvWriter.close();
					System.out.println("Done with " + extensionFileName + " file");
				}
			}
			Conductor.time4 = new Date();
			System.out.println(
					"Time for DLV (seconds): " + ((Conductor.time4.getTime() - Conductor.time3.getTime()) / 1000.0));
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	static void findDifference() {
		try {
			for (int topRuleCount : TOP_RULE_COUNTS) {
				String negativeNeedCheckFileName = extensionPrefixFileName + ".neg." + topRuleCount + ".needcheck";
				String positiveNeedCheckFileName = extensionPrefixFileName + ".pos." + topRuleCount + ".needcheck";
				List<String> negativeNeedCheckLines = TextFileReader.readLines(negativeNeedCheckFileName);
				Set<String> negativeNeedCheckLineSet = new HashSet<String>(negativeNeedCheckLines);
				Writer differebceWriter = new PrintWriter(
						new File(extensionPrefixFileName + ".diff." + topRuleCount + ".needcheck"));
				List<String> positiveNeedCheckLines = TextFileReader.readLines(positiveNeedCheckFileName);
				for (String positiveNeedCheckLine : positiveNeedCheckLines) {
					if (negativeNeedCheckLineSet.contains(positiveNeedCheckLine)) {
						continue;
					}
					differebceWriter.write(positiveNeedCheckLine + "\n");
				}
				differebceWriter.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void execute(RankingType type) {
		if (!(new File(encodeFileName).exists())) {
			Encoder.encode();
		}
		Encoder.loadEncode();
		if (!(new File(trainingDataDlvFileName).exists())) {
			Encoder.convert2DlvKnowledgeGraph();
		}
		generateExceptions(type);
		runDlv();
		evaluate();
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Wrong number of parameters.");
			return;
		}
		File workingFolder = new File(args[0]);
		if (!workingFolder.exists()) {
			System.out.println("Working folder does not exist.");
			return;
		}
		int type = 2;
		try {
			type = Integer.parseInt(args[1]);
		} catch (NumberFormatException ex) {
			System.out.println("Parameter 2 should be from 0 to 2.");
			return;
		}
		if (type < 0 || type > 2) {
			System.out.println("Parameter 2 should be from 0 to 2.");
			return;
		}
		String workingPath = workingFolder.getAbsolutePath();
		File dlvFolder = new File(workingPath + "/DLV");
		if (!dlvFolder.exists()) {
			dlvFolder.mkdir();
		}
		String dlvPath = dlvFolder.getAbsolutePath();
		String typeName = RankingType.values()[type].toString().toLowerCase();
		idealDataFileName = workingPath + "/ideal.data.txt";
		encodeFileName = workingPath + "/encode.txt";
		patternFileName = workingPath + "/patterns.txt";
		trainingDataFileName = workingPath + "/training.data.txt";
		trainingDataDlvFileName = dlvPath + "/training.data.kg";
		choosenRuleFileName = dlvPath + "/choosen.rules." + typeName + ".txt";
		dlvBinaryFileName = workingPath + "/dlv.bin";
		extensionPrefixFileName = dlvPath + "/extension." + typeName + ".kg";
		execute(RankingType.values()[type]);
	}

}
