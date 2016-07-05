package com.mpii.saarland.germany.indexing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.mpii.saarland.germany.utils.Settings;
import com.mpii.saarland.germany.utils.Utils;

/*
 * This class is to index facts with the form: <x> <p> <y>
 */
public class FactIndexer {

	private static final Logger LOG = LoggerFactory.getLogger(FactIndexer.class);

	public static FactIndexer INSTANCE;

	private Map<String, Set<String>> x2PySet;

	private Map<String, Set<String>> y2PxSet;

	private Map<String, Set<String>> x2TSet;

	private Map<String, Set<String>> t2XSet;

	private Map<String, Set<String>> pt2XSet;

	private Map<String, Set<String>> p2XySet;

	private Map<String, Set<String>> xy2PSet;

	private Set<String> xpySet;

	public static FactIndexer getInstace() {
		if (INSTANCE == null) {
			INSTANCE = new FactIndexer();
		}
		return INSTANCE;
	}

	private FactIndexer() {
		x2PySet = new HashMap<String, Set<String>>();
		y2PxSet = new HashMap<String, Set<String>>();
		x2TSet = new HashMap<String, Set<String>>();
		t2XSet = new HashMap<String, Set<String>>();
		pt2XSet = new HashMap<String, Set<String>>();
		p2XySet = new HashMap<String, Set<String>>();
		xy2PSet = new HashMap<String, Set<String>>();
		xpySet = new HashSet<String>();
		index(Settings.USING_YAGO_DATA);
	}

	private void indexTypes(String fileName) {
		LOG.info("Start loading types");
		BufferedReader typeReader = null;
		try {
			typeReader = new BufferedReader(new FileReader(fileName));
			typeReader.readLine();
			String line;
			while ((line = typeReader.readLine()) != null) {
				line = line.trim();
				line = line.replace("rdf:type", "<type>");
				line = line.substring(1, line.length() - 1);
				String[] parts = line.split(">\t<");

				Utils.addKeyString(x2TSet, parts[1], parts[3]);
				Utils.addKeyString(t2XSet, parts[3], parts[1]);
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
		LOG.info("Done with loading types");
	}

	private void indexFacts(String fileName) {
		LOG.info("Start loading facts");
		BufferedReader factReader = null;
		try {
			factReader = new BufferedReader(new FileReader(fileName));
			xpySet = new HashSet<String>();
			String line;
			if (Settings.USING_YAGO_DATA) {
				factReader.readLine();
			}
			while ((line = factReader.readLine()) != null) {
				if (Settings.USING_YAGO_DATA) {
					line = line.substring(line.indexOf("\t"));
				}
				line = line.trim();
				line = line.substring(1, line.length() - 1);
				String[] parts = line.split(">\t<");
				if (parts.length < 3) {
					continue;
				}
				if (!parts[1].equals("type")) {
					Utils.addKeyString(x2PySet, parts[0], parts[1] + "\t" + parts[2]);
					Utils.addKeyString(y2PxSet, parts[2], parts[1] + "\t" + parts[0]);
					Utils.addKeyString(p2XySet, parts[1], parts[0] + "\t" + parts[2]);
					Utils.addKeyString(xy2PSet, parts[0] + "\t" + parts[2], parts[1]);
					xpySet.add(parts[0] + "\t" + parts[1] + "\t" + parts[2]);
				} else {
					Utils.addKeyString(x2TSet, parts[0], parts[2]);
					Utils.addKeyString(t2XSet, parts[2], parts[0]);
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
		LOG.info("Done with loading facts");
	}

	private void indexPatterns() {
		for (String fact : xpySet) {
			String[] parts = fact.split("\t");
			Set<String> tSet = x2TSet.get(parts[2]);
			if (tSet == null) {
				continue;
			}
			for (String t : tSet) {
				Utils.addKeyString(pt2XSet, parts[1] + "\t" + t, parts[0]);
			}
		}
		LOG.info("Done with loading patterns pt2X");
	}

	private void index(boolean usingYagoData) {
		if (usingYagoData) {
			LOG.info("Index YAGO data");
			indexTypes(Settings.YAGO_TYPE_FILE_NAME);
			indexFacts(Settings.YAGO_FACT_FILE_NAME);
		} else {
			LOG.info("Index IMDB data");
			indexFacts(Settings.IMDB_FACT_FILE_NAME);
		}
		indexPatterns();
	}

	public Set<String> getPySetFromX(String x) {
		return x2PySet.get(x);
	}

	public Set<String> getPxSetFromY(String y) {
		return y2PxSet.get(y);
	}

	public Set<String> getTSetFromX(String x) {
		return x2TSet.get(x);
	}

	public Set<String> getXpySet() {
		return xpySet;
	}

	public Set<String> getTSet() {
		return t2XSet.keySet();
	}

	public Set<String> getXSetFromPt(String pt) {
		return pt2XSet.get(pt);
	}

	public Set<String> getXSetFromT(String t) {
		return t2XSet.get(t);
	}

	public Set<String> getXySetFromP(String p) {
		return p2XySet.get(p);
	}

	public Set<String> getPSetFromXy(String xy) {
		return xy2PSet.get(xy);
	}

	public Set<String> getPSet() {
		return p2XySet.keySet();
	}

}
