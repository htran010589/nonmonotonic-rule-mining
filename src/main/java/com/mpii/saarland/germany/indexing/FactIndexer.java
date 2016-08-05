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
import com.mpii.saarland.germany.utils.Utils;

/**
 * 
 * @author Hai Dang Tran
 * 
 * This class is to index facts with the form: <x> <p> <y>.
 */
public class FactIndexer {

	private static final Logger LOG = LoggerFactory.getLogger(FactIndexer.class);

	private String sourceFile;

	private Map<String, Set<String>> x2PySet;

	private Map<String, Set<String>> y2PxSet;

	private Map<String, Set<String>> x2TSet;

	private Map<String, Set<String>> t2XSet;

	private Map<String, Set<String>> pt2XSet;

	private Map<String, Set<String>> p2XySet;

	private Map<String, Set<String>> p2XSet;

	private Map<String, Set<String>> p2YSet;

	private Map<String, Set<String>> xy2PSet;

	private Map<String, Set<String>> py2XSet;

	private Set<String> xpySet;

	private void allocate() {
		x2PySet = new HashMap<>();
		y2PxSet = new HashMap<>();
		x2TSet = new HashMap<>();
		t2XSet = new HashMap<>();
		pt2XSet = new HashMap<>();
		p2XySet = new HashMap<>();
		p2XSet = new HashMap<>();
		p2YSet = new HashMap<>();
		xy2PSet = new HashMap<>();
		py2XSet = new HashMap<>();
		xpySet = new HashSet<>();
	}

	public FactIndexer() {
		allocate();
	}

	public FactIndexer(String sourceFile) {
		this.sourceFile = sourceFile;
		allocate();
		index();
		LOG.info("Done with creating a new FactIndexer instance");
	}

	/**
	 * This constructor is to clone an instance.
	 * 
	 */
	public FactIndexer cloneFact() {
		FactIndexer newFacts = new FactIndexer();
		newFacts.x2PySet = Utils.cloneMap(x2PySet);
		newFacts.y2PxSet = Utils.cloneMap(y2PxSet);
		newFacts.x2TSet = Utils.cloneMap(x2TSet);
		newFacts.t2XSet = Utils.cloneMap(t2XSet);
		newFacts.pt2XSet = Utils.cloneMap(pt2XSet);
		newFacts.p2XySet = Utils.cloneMap(p2XySet);
		newFacts.p2XSet = Utils.cloneMap(p2XSet);
		newFacts.p2YSet = Utils.cloneMap(p2YSet);
		newFacts.xy2PSet = Utils.cloneMap(xy2PSet);
		newFacts.py2XSet = Utils.cloneMap(py2XSet);
		newFacts.xpySet = new HashSet<String>();
		newFacts.xpySet.addAll(xpySet);
		LOG.info("Done with cloning instance");
		return newFacts;
	}

	public void indexFact(String[] parts) {
		if (!parts[1].equals("type")) {
			Utils.addKeyString(x2PySet, parts[0], parts[1] + "\t" + parts[2]);
			Utils.addKeyString(y2PxSet, parts[2], parts[1] + "\t" + parts[0]);
			Utils.addKeyString(p2XySet, parts[1], parts[0] + "\t" + parts[2]);
			Utils.addKeyString(p2XSet, parts[1], parts[0]);
			Utils.addKeyString(p2YSet, parts[1], parts[2]);
			Utils.addKeyString(xy2PSet, parts[0] + "\t" + parts[2], parts[1]);
			Utils.addKeyString(py2XSet, parts[1] + "\t" + parts[2], parts[0]);
			xpySet.add(parts[0] + "\t" + parts[1] + "\t" + parts[2]);
		} else {
			Utils.addKeyString(x2TSet, parts[0], parts[2]);
			Utils.addKeyString(t2XSet, parts[2], parts[0]);
		}
	}

	public void indexFacts(String fileName) {
		LOG.info("Start loading facts");
		BufferedReader factReader = null;
		try {
			factReader = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = factReader.readLine()) != null) {
				line = line.substring(1, line.length() - 1);
				String[] parts = line.split(">\t<");
				indexFact(parts);
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

	public void indexPattern(String[] parts) {
		Set<String> tSet = x2TSet.get(parts[2]);
		if (tSet == null) {
			return;
		}
		for (String t : tSet) {
			Utils.addKeyString(pt2XSet, parts[1] + "\t" + t, parts[0]);
		}
	}

	public void indexPatterns() {
		for (String fact : xpySet) {
			String[] parts = fact.split("\t");
			indexPattern(parts);
		}
		LOG.info("Done with loading patterns pt2X");
	}

	public void index() {
		indexFacts(sourceFile);
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

	public Set<String> getXSetFromP(String p) {
		return p2XSet.get(p);
	}

	public Set<String> getYSetFromP(String p) {
		return p2YSet.get(p);
	}

	public Set<String> getPSetFromXy(String xy) {
		return xy2PSet.get(xy);
	}

	public Set<String> getXSetFromPy(String py) {
		return py2XSet.get(py);
	}

	public Set<String> getPSet() {
		return p2XySet.keySet();
	}

	public Set<String> getXSet() {
		return x2PySet.keySet();
	}

	public Set<String> getYSet() {
		return y2PxSet.keySet();
	}

	public boolean checkXpy(String xpy) {
		if (xpySet.contains(xpy)) {
			return true;
		}
		return false;
	}

}
