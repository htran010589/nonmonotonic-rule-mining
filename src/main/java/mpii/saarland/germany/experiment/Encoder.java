package mpii.saarland.germany.experiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpii.saarland.germany.utils.TextFileReader;

/**
 * 
 * @author Hai Dang Tran
 * 
 */
public class Encoder {

	static Map<String, String> entity2Id, id2Entity;

	static int entityCount, typeCount, predicateCount;

	static void encode(String entity, int entityType) {
		entity = entity.trim().replaceAll("\\s+", " ");
		if (entity2Id.containsKey(entity))
			return;
		if (entityType == 0) {
			entityCount++;
			entity2Id.put(entity, "e" + entityCount);
		} else if (entityType == 1) {
			typeCount++;
			entity2Id.put(entity, "t" + typeCount);
		} else {
			predicateCount++;
			entity2Id.put(entity, "p" + predicateCount);
		}
	}

	static void encode() {
		System.out.println("Start with encoding entities and predicates.");
		entity2Id = new HashMap<String, String>();
		entityCount = typeCount = predicateCount = 0;
		List<String> lines = TextFileReader.readLines(Conductor.idealDataFileName);
		for (String line : lines) {
			line = line.substring(1, line.length() - 1);
			String[] parts = line.split(">\t<");
			encode(parts[1], 2);
		}

		for (String line : lines) {
			line = line.substring(1, line.length() - 1);
			String[] parts = line.split(">\t<");
			if (parts[1].equals("subClassOf")) {
				encode(parts[0], 1);
				encode(parts[2], 1);
				continue;
			}
			if (parts[1].equals("type")) {
				encode(parts[2], 1);
				continue;
			}
		}

		for (String line : lines) {
			line = line.substring(1, line.length() - 1);
			String[] parts = line.split(">\t<");
			encode(parts[0], 0);
			encode(parts[2], 0);
		}

		try {
			Writer encodeFileWriter = new BufferedWriter(new FileWriter(Conductor.encodeFileName));
			for (String entity : entity2Id.keySet()) {
				String id = entity2Id.get(entity);
				encodeFileWriter.write(entity + "\t" + id + "\n");
			}
			encodeFileWriter.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		System.out.println("Done with encoding entities and predicates.");
	}

	static void loadEncode() {
		entity2Id = new HashMap<>();
		id2Entity = new HashMap<>();
		List<String> lines = TextFileReader.readLines(Conductor.encodeFileName);
		for (String line : lines) {
			String[] parts = line.split("\t");
			entity2Id.put(parts[0], parts[1]);
			id2Entity.put(parts[1], parts[0]);
		}
		System.out.println("Done with load encoding.");
	}

	static void decodeDlvOutput() {
		for (String ruleType : Conductor.RULE_TYPES) {
			String extensionFileName = Conductor.extensionPrefixFileName + ruleType + Conductor.topRuleCount;
			String line = TextFileReader.readLines(extensionFileName).get(2);
			String[] facts = line.split(", ");
			try {
				Writer decodeFileWriter = new BufferedWriter(new FileWriter(extensionFileName + ".decode"));
				for (String fact : facts) {
					if (fact.startsWith("{")) {
						fact = fact.substring(1);
					}
					if (fact.endsWith("}")) {
						fact = fact.substring(0, fact.length() - 1);
					}
					String[] entities = fact.split("\\(|\\)|,");
					decodeFileWriter.write("<" + id2Entity.get(entities[1]) + ">\t<" + id2Entity.get(entities[0]) + ">\t<"
							+ id2Entity.get(entities[2]) + ">\n");
				}
				decodeFileWriter.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	static void convert2DlvKnowledgeGraph() {
		try {
			BufferedReader trainingDataReader = new BufferedReader(new FileReader(Conductor.trainingDataFileName));
			Writer trainingDataDlvWriter = new BufferedWriter(new FileWriter(Conductor.trainingDataDlvFileName));
			String line;
			while ((line = trainingDataReader.readLine()) != null) {
				line = line.substring(1, line.length() - 1);
				String[] parts = line.split(">\t<");
				if (entity2Id.get(parts[0]) == null) {
					continue;
				}
				if (entity2Id.get(parts[2]) == null) {
					continue;
				}
				if (!parts[1].equals("type")) {
					trainingDataDlvWriter.write(entity2Id.get(parts[1]) + "(" + entity2Id.get(parts[0]) + ", "
							+ entity2Id.get(parts[2]) + ").\n");
				} else {
					trainingDataDlvWriter.write(entity2Id.get(parts[2]) + "(" + entity2Id.get(parts[0]) + ").\n");
				}
			}
			trainingDataReader.close();
			trainingDataDlvWriter.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Done with creating training KG in DLV format.");
	}

}
