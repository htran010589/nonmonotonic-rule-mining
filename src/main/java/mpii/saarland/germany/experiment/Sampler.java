package mpii.saarland.germany.experiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import mpii.saarland.germany.indexing.FactIndexer;
import mpii.saarland.germany.utils.Utils;

/**
 * 
 * @author Hai Dang Tran
 * 
 */
public class Sampler {

	static void createLearningData() {
		try {
			Conductor.idealFacts = new FactIndexer(Conductor.idealDataFileName);
			Writer learningDataWriter = new BufferedWriter(new FileWriter(Conductor.trainingDataFileName));
			Map<String, Long> predicateCount = new HashMap<String, Long>();
			Map<String, Long> remainPredicateCount = new HashMap<String, Long>();
			Map<String, Long> entityDegree = new HashMap<String, Long>();
			for (String xpy : Conductor.idealFacts.getXpySet()) {
				String[] parts = xpy.split("\t");
				Utils.addKeyLong(entityDegree, parts[0], 1);
				Utils.addKeyLong(entityDegree, parts[2], 1);
				Utils.addKeyLong(predicateCount, parts[1], 1);
				Utils.addKeyLong(remainPredicateCount, parts[1], 1);
			}
			for (String xpy : Conductor.idealFacts.getXpySet()) {
				String[] parts = xpy.split("\t");
				boolean delete = true;
				if (entityDegree.get(parts[0]) <= 1)
					delete = false;
				if (entityDegree.get(parts[2]) <= 1)
					delete = false;
				if ((1.0 * (remainPredicateCount.get(parts[1]) - 1) / predicateCount.get(parts[1])) < 0.8)
					delete = false;
				if (!delete) {
					learningDataWriter.write("<" + parts[0] + ">\t<" + parts[1] + ">\t<" + parts[2] + ">\n");
				} else {
					Utils.addKeyLong(entityDegree, parts[0], -1);
					Utils.addKeyLong(entityDegree, parts[2], -1);
					Utils.addKeyLong(remainPredicateCount, parts[1], -1);
				}
			}
			for (String x : Conductor.idealFacts.getXSet()) {
				if (Conductor.idealFacts.getTSetFromX(x) == null)
					continue;
				for (String t : Conductor.idealFacts.getTSetFromX(x)) {
					learningDataWriter.write("<" + x + ">\t<type>\t<" + t + ">\n");
				}
			}
			learningDataWriter.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	static FactIndexer indexLearningData() {
		FactIndexer learningFacts = new FactIndexer();
		try {
			BufferedReader learningDataReader = new BufferedReader(new FileReader(Conductor.trainingDataFileName));
			String line;
			while ((line = learningDataReader.readLine()) != null) {
				line = line.substring(1, line.length() - 1);
				String[] parts = line.split(">\t<");
				learningFacts.indexFact(parts, 1L);
			}
			learningDataReader.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return learningFacts;
	}

	static Map<String, Long> getFactCountPerPredicate(String fileName) {
		Map<String, Long> predicate2Count = new HashMap<>();
		try {
			BufferedReader learningDataReader = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = learningDataReader.readLine()) != null) {
				line = line.substring(1, line.length() - 1);
				String[] parts = line.split(">\t<");
				Utils.addKeyLong(predicate2Count, parts[1], 1L);
			}
			learningDataReader.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return predicate2Count;
	}

	static void countFactsPerPredicate() {
		Map<String, Long> trainingPredicate2Count = getFactCountPerPredicate(Conductor.trainingDataFileName);
		Map<String, Long> idealPredicate2Count = getFactCountPerPredicate(Conductor.idealDataFileName);
		Set<String> predicates = new TreeSet<>();
		predicates.addAll(trainingPredicate2Count.keySet());
		for (String predicate : predicates) {
			long learningFactCount = -1;
			try {
				learningFactCount = trainingPredicate2Count.get(predicate);
			} catch (NullPointerException ex) {
				learningFactCount = 0;
			}
			long idealFactCount = -1;
			try {
				idealFactCount = idealPredicate2Count.get(predicate);
			} catch (NullPointerException ex) {
				idealFactCount = 0;
			}
			System.out.println(predicate + " & " + learningFactCount + " & " + idealFactCount + "\\\\");
		}
	}

}
