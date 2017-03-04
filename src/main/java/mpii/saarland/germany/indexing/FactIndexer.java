package mpii.saarland.germany.indexing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mpii.saarland.germany.utils.Utils;

/**
 * 
 * @author Hai Dang Tran
 * 
 * This class is to index facts with the form: <X> <p> <Y>.
 */
public class FactIndexer {

	private static final Logger LOG = LoggerFactory.getLogger(FactIndexer.class);

	private String sourceFile;

	private Map<String, Set<String>> y2PxSet;

	private Map<String, Set<String>> x2TSet;

	private Map<String, Set<String>> p2XySet;

	private Map<String, Set<String>> p2XSet;

	private Map<String, Set<String>> p2YSet;

	private Map<String, Set<String>> xy2PSet;

	private Map<String, Set<String>> py2XSet;

	private Map<String, Long> xt2Frequency;

	private Map<String, Long> px2Frequency;

	private Map<String, Long> py2Frequency;

	private Map<String, Long> xpy2Frequency;

	private void allocate() {
		y2PxSet = new HashMap<>();
		x2TSet = new HashMap<>();
		p2XySet = new HashMap<>();
		p2XSet = new HashMap<>();
		p2YSet = new HashMap<>();
		xy2PSet = new HashMap<>();
		py2XSet = new HashMap<>();
		xt2Frequency = new HashMap<>();
		px2Frequency = new HashMap<>();
		py2Frequency = new HashMap<>();
		xpy2Frequency = new HashMap<>();
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
		newFacts.y2PxSet = Utils.cloneMap(y2PxSet);
		newFacts.x2TSet = Utils.cloneMap(x2TSet);
		newFacts.p2XySet = Utils.cloneMap(p2XySet);
		newFacts.p2XSet = Utils.cloneMap(p2XSet);
		newFacts.p2YSet = Utils.cloneMap(p2YSet);
		newFacts.xy2PSet = Utils.cloneMap(xy2PSet);
		newFacts.py2XSet = Utils.cloneMap(py2XSet);
		newFacts.xt2Frequency = new HashMap<>();
		newFacts.xt2Frequency.putAll(xt2Frequency);
		newFacts.px2Frequency = new HashMap<>();
		newFacts.px2Frequency.putAll(px2Frequency);
		newFacts.py2Frequency = new HashMap<>();
		newFacts.py2Frequency.putAll(py2Frequency);
		newFacts.xpy2Frequency = new HashMap<>();
		newFacts.xpy2Frequency.putAll(xpy2Frequency);
		LOG.info("Done with cloning instance");
		return newFacts;
	}

	public void indexFact(String[] parts, long frequency) {
		boolean add;
		if (!parts[1].equals("type")) {
			String xpy = parts[0] + "\t" + parts[1] + "\t" + parts[2];
			Utils.addKeyLong(xpy2Frequency, xpy, frequency);
			add = true;
			if (xpy2Frequency.get(xpy) == 0) {
				// Remove the triple.
				xpy2Frequency.remove(xpy);
				add = false;
			}
			Utils.updateKeyString(y2PxSet, parts[2], parts[1] + "\t" + parts[0], add);
			Utils.updateKeyString(p2XySet, parts[1], parts[0] + "\t" + parts[2], add);
			Utils.updateKeyString(xy2PSet, parts[0] + "\t" + parts[2], parts[1], add);
			Utils.updateKeyString(py2XSet, parts[1] + "\t" + parts[2], parts[0], add);

			String px = parts[1] + "\t" + parts[0];
			add = true;
			Utils.addKeyLong(px2Frequency, px, frequency);
			if (px2Frequency.get(px) == 0) {
				px2Frequency.remove(px);
				add = false;
			}
			Utils.updateKeyString(p2XSet, parts[1], parts[0], add);

			String py = parts[1] + "\t" + parts[2];
			add = true;
			Utils.addKeyLong(py2Frequency, py, frequency);
			if (py2Frequency.get(py) == 0) {
				py2Frequency.remove(py);
				add = false;
			}
			Utils.updateKeyString(p2YSet, parts[1], parts[2], add);
		} else {
			String xt = parts[0] + "\t" + parts[2];
			add = true;
			Utils.addKeyLong(xt2Frequency, xt, frequency);
			if (xt2Frequency.get(xt) == 0) {
				xt2Frequency.remove(xt);
				add = false;
			}
			Utils.updateKeyString(x2TSet, parts[0], parts[2], add);
		}
	}

	public void indexFacts(String fileName) {
		LOG.info("Start loading facts");
		int count = 0;
		BufferedReader factReader = null;
		try {
			factReader = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = factReader.readLine()) != null) {
				line = line.substring(1, line.length() - 1);
				String[] parts = line.split(">\t<");
				indexFact(parts, 1L);
				count++;
				if (count % 1000000 == 0) {
					LOG.info("Indexed " + count + " facts");
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

	public void index() {
		indexFacts(sourceFile);
	}

	public Set<String> getPxSetFromY(String y) {
		return y2PxSet.get(y);
	}

	public Set<String> getTSetFromX(String x) {
		return x2TSet.get(x);
	}

	public Set<String> getXpySet() {
		return xpy2Frequency.keySet();
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
		return x2TSet.keySet();
	}

	public boolean checkXpy(String xpy) {
		if (xpy2Frequency.containsKey(xpy)) {
			return true;
		}
		return false;
	}

}
