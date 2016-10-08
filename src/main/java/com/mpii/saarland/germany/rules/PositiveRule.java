package com.mpii.saarland.germany.rules;

import com.mpii.saarland.germany.indexing.FactIndexer;
import com.mpii.saarland.germany.rulemining.nonmonotonicrule.InstanceSetMiner;

/**
 * 
 * @author Hai Dang Tran
 * 
 */
public class PositiveRule {

	private String head;

	private String body;

	private PositiveRuleType type;

	private double headSupport, confidence, conviction;

	public long headCount, bodyCount;

	public PositiveRule(String textRule, PositiveRuleType type) {
		int i = textRule.indexOf("\t");
		this.head = textRule.substring(0, i);
		this.body = textRule.substring(i + 1);
		this.type = type;
	}

	@Override
	public int hashCode() {
		return (head + "\t" + body + "\t" + type).hashCode();
	}

	@Override
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		if (that == null) {
			return false;
		}
		if (getClass() != that.getClass()) {
			return false;
		}
		PositiveRule other = (PositiveRule) that;
		if (this.type != other.type || !this.head.equals(other.head) || !this.body.equals(other.body)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(head);
		String[] parts = body.split("\t");
		if (type == PositiveRuleType.FORM2) {
			result.append("(X, Z) <- ");
			result.append(parts[0] + "(X, Y) ^ ");
			result.append(parts[1] + "(Y, Z)");
		}
		return result.toString();
	}

	public String toStringWithStatistics() {
		return toString() + "\t" + conviction + "\t" + confidence + "\t" + headCount + "\t" + bodyCount;
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
		headCount = 0;
		if (form2Instances.getNormalSet(this) != null) {
			headCount = form2Instances.getNormalSet(this).size();
		}
	}

	public void setBodyCount(InstanceSetMiner form2Instances) {
		bodyCount = headCount;
		if (form2Instances.getAbnormalSet(this) != null) {
			bodyCount += form2Instances.getAbnormalSet(this).size();
		}
	}

	public void setConfidence() {
		confidence = 1.0 * headCount / bodyCount;
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

	public String getHead() {
		return head;
	}

	public String getBody() {
		return body;
	}

}
