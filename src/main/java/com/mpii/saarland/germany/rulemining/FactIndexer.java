package com.mpii.saarland.germany.rulemining;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.mpii.saarland.germany.utils.Constants;

public class FactIndexer {

	private static final Logger LOG = LoggerFactory.getLogger(FactIndexer.class);

	public static FactIndexer INSTANCE;

	private Map<String, Set<String>> subject2PredicateObject;

	private Map<String, Set<String>> object2PredicateSubject;

	private Map<String, Set<String>> subject2Types;

	private Set<String> facts;

	public static FactIndexer getInstace() {
		if (INSTANCE == null) {
			INSTANCE = new FactIndexer();
		}
		return INSTANCE;
	}

	private FactIndexer() {
		subject2PredicateObject = new HashMap<String, Set<String>>();
		subject2Types = new HashMap<String, Set<String>>();
		index(Constants.USING_YAGO_DATA);
	}

	private void indexTypes(String fileName) {
		LOG.info("Start loading types");
		BufferedReader typeReader = null;
		int count = 0;
		try {
			typeReader = new BufferedReader(new FileReader(fileName));
			typeReader.readLine();
			String line;
			while ((line = typeReader.readLine()) != null) {
				count++;
				if (count % Constants.NUMBER_OF_CONSECUTIVE_LINES == 0) {
					LOG.info(".");
				}
				line = line.trim();
				line = line.replace("rdf:type", "<type>");
				line = line.substring(1, line.length() - 1);
				String[] parts = line.split(">\t<");

				String type = parts[3];
				Set<String> typeSet = subject2Types.get(parts[1]);
				if (typeSet == null) {
					typeSet = new HashSet<String>();
				}
				typeSet.add(type);
				subject2Types.put(parts[1], typeSet);
			}
		} catch (IOException ex) {
			LOG.error(ex.getMessage());
		} finally {
			try {
				typeReader.close();
			} catch (IOException ex) {
				LOG.error(ex.getMessage());
			}
		}
		LOG.info("\nDone with loading types");
	}

	private void indexFacts(String fileName) {
		LOG.info("Start loading facts");
		BufferedReader factReader = null;
		int count = 0;
		try {
			factReader = new BufferedReader(new FileReader(fileName));
			facts = new HashSet<String>();
			String line;
			while ((line = factReader.readLine()) != null) {
				count++;
				if (count % Constants.NUMBER_OF_CONSECUTIVE_LINES == 0) {
					LOG.info(".");
				}
				line = line.substring(1, line.length() - 1);
				String[] parts = line.split(">\t<");
				if (!parts[1].equals("type")) {
					if (parts.length < 3) {
						continue;
					}
					String predicateObject = parts[1] + "\t" + parts[2];
					Set<String> predicateObjectSet = subject2PredicateObject.get(parts[0]);
					if (predicateObjectSet == null) {
						predicateObjectSet = new HashSet<String>();
					}
					predicateObjectSet.add(predicateObject);
					subject2PredicateObject.put(parts[0], predicateObjectSet);

					String predicateSubject = parts[1] + "\t" + parts[0];
					Set<String> predicateSubjectSet = object2PredicateSubject.get(parts[2]);
					if (predicateSubjectSet == null) {
						predicateSubjectSet = new HashSet<String>();
					}
					predicateSubjectSet.add(predicateSubject);
					object2PredicateSubject.put(parts[2], predicateSubjectSet);

					facts.add(line);
				} else {
					String type = parts[2];
					Set<String> typeSet = subject2Types.get(parts[0]);
					if (typeSet == null) {
						typeSet = new HashSet<String>();
					}
					typeSet.add(type);
					subject2Types.put(parts[0], typeSet);
				}
			}
		} catch (IOException ex) {
			LOG.error(ex.getMessage());
		} finally {
			try {
				factReader.close();
			} catch (IOException ex) {
				LOG.error(ex.getMessage());
			}
		}
		LOG.info("\nDone with loading facts");
	}

	private void index(boolean isYagoData) {
		if (isYagoData) {
			indexTypes(Constants.YAGO_TYPE_FILE_NAME);
			indexFacts(Constants.YAGO_FACT_FILE_NAME);
		} else {
			indexFacts(Constants.IMDB_FACT_FILE_NAME);
		}
	}

	public Set<String> getPredicateObjectSet(String subject) {
		return subject2PredicateObject.get(subject);
	}

	public Set<String> getPredicateSubjectSet(String object) {
		return object2PredicateSubject.get(object);
	}

	public Set<String> getTypeSet(String subject) {
		return subject2Types.get(subject);
	}

	public Set<String> getFacts() {
		return facts;
	}

}
