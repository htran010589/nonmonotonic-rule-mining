package com.mpii.saarland.germany.rules;

import com.mpii.saarland.germany.indexing.FactIndexer;
import com.mpii.saarland.germany.rulemining.nonmonotonicrule.InstanceSetMiner;

public class PositiveRule {

	private String head;

	private String body;

	private PositiveRuleType type;

	private double headSupport, confidence, conviction;

	private long headCount, bodyCount;

	public PositiveRule(String head, String body, PositiveRuleType type) {
		this.head = head;
		this.body = body;
		this.type = type;
	}

	@Override
	public int hashCode() {
		return (head + "\t" + body + "\t" + type).hashCode();
	}

	@Override
	public boolean equals(Object that) {
		if (that.getClass().equals(this.getClass())) {
			return false;
		}
		PositiveRule thatRule = (PositiveRule) that;
		if (this.type != thatRule.type || !this.head.equals(thatRule.head) || !this.body.equals(thatRule.body)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(head);
		String[] parts = body.split("\t");
		if (type == PositiveRuleType.FORM2) {
			result.append("(x, y) <- ");
			result.append(parts[0] + "(x, z) ^ ");
			result.append(parts[1] + "(z, y)");
		}
		return result.toString();
	}

	public void setHeadSupport(FactIndexer facts) {
		double support = 0;
		if (facts.getXySetFromP(head) != null) {
			support = facts.getXySetFromP(head).size();
		}
		double xSupport = 0;
		if (facts.getXSetFromP(head) != null) {
			xSupport = facts.getXSetFromP(head).size();
		}
		double ySupport = 0;
		if (facts.getYSetFromP(head) != null) {
			ySupport = facts.getYSetFromP(head).size();
		}
		headSupport = support / (xSupport * ySupport);
	}

	public void setHeadCount(InstanceSetMiner form2Instances) {
		String positiveRule = body + "\t" + head;
		headCount = 0;
		if (form2Instances.getNormalSet(positiveRule) != null) {
			headCount = form2Instances.getNormalSet(positiveRule).size();
		}
	}

	public void setBodyCount(InstanceSetMiner form2Instances) {
		String positiveRule = body + "\t" + head;
		bodyCount = headCount;
		if (form2Instances.getAbnormalSet(positiveRule) != null) {
			bodyCount += form2Instances.getAbnormalSet(positiveRule).size();
		}
	}

	public void setConfidence() {
		confidence = headCount / bodyCount;
	}

	public void setConviction() {
		conviction = (1 - headSupport) / (1 - confidence);
	}

	public double getHeadSupport() {
		return headSupport;
	}

	public long getHeadCount() {
		return headCount;
	}

	public long getBodyCount() {
		return bodyCount;
	}

	public double getConfidence() {
		return confidence;
	}

	public double getConviction() {
		return conviction;
	}

}
