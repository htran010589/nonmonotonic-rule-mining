package com.mpii.saarland.germany.indexing;

public class FactIndexerFactory {

	public static FactIndexer originalFacts, predictedFacts, newFacts;

	static {
		originalFacts = new FactIndexer(false);
		predictedFacts = new FactIndexer(originalFacts);
		newFacts = new FactIndexer(originalFacts);
	}

}
