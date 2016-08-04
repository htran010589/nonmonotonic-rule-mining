package com.mpii.saarland.germany.rules;

public class NegativeRule {

	private PositiveRule positiveRule;

	private Exception exception;

	private long negativeExceptionPositiveHeadRuleCount, negativeExceptionBodyCount,
			positiveExceptionNegativeHeadRuleCount, positiveExceptionBodyCount;

	private double negativeExceptionPositiveHeadConfidence, positiveExceptionNegativeHeadConfidence, standardConviction,
			auxiliaryConviction, positiveNegativeConviction;

	public NegativeRule(PositiveRule positiveRule, Exception exception) {
		this.positiveRule = positiveRule;
		this.exception = exception;
	}

	public void calculateConviction() {
		negativeExceptionPositiveHeadConfidence = 1.0 * negativeExceptionPositiveHeadRuleCount
				/ negativeExceptionBodyCount;
		positiveExceptionNegativeHeadConfidence = 1.0 * positiveExceptionNegativeHeadRuleCount
				/ positiveExceptionBodyCount;
		standardConviction = (1 - positiveRule.getHeadSupport()) / (1 - negativeExceptionPositiveHeadConfidence);
		auxiliaryConviction = positiveRule.getHeadSupport() / (1 - positiveExceptionNegativeHeadConfidence);
		positiveNegativeConviction = (standardConviction + auxiliaryConviction) / 2;
	}

	@Override
	public int hashCode() {
		return (positiveRule.toString() + "\t" + exception.toString()).hashCode();
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
		NegativeRule other = (NegativeRule) that;
		if (!positiveRule.equals(other.positiveRule) || !exception.equals(other.exception)) {
			return false;
		}
		return true;
	}

	public String toStringWithStatistics() {
		return "not " + exception.toString() + "\t" + positiveNegativeConviction + "\t" + standardConviction + "\t"
				+ auxiliaryConviction + "\t" + negativeExceptionPositiveHeadConfidence + "\t"
				+ positiveExceptionNegativeHeadConfidence + "\t" + negativeExceptionPositiveHeadRuleCount + "\t"
				+ negativeExceptionBodyCount + "\t" + positiveExceptionNegativeHeadRuleCount + "\t"
				+ positiveExceptionBodyCount;
	}

	public long getNegativeExceptionPositiveHeadRuleCount() {
		return negativeExceptionPositiveHeadRuleCount;
	}

	public void setNegativeExceptionPositiveHeadRuleCount(long negativeExceptionPositiveHeadRuleCount) {
		this.negativeExceptionPositiveHeadRuleCount = negativeExceptionPositiveHeadRuleCount;
	}

	public long getNegativeExceptionBodyCount() {
		return negativeExceptionBodyCount;
	}

	public void setNegativeExceptionBodyCount(long negativeExceptionBodyCount) {
		this.negativeExceptionBodyCount = negativeExceptionBodyCount;
	}

	public long getPositiveExceptionNegativeHeadRuleCount() {
		return positiveExceptionNegativeHeadRuleCount;
	}

	public void setPositiveExceptionNegativeHeadRuleCount(long positiveExceptionNegativeHeadRuleCount) {
		this.positiveExceptionNegativeHeadRuleCount = positiveExceptionNegativeHeadRuleCount;
	}

	public long getPositiveExceptionBodyCount() {
		return positiveExceptionBodyCount;
	}

	public void setPositiveExceptionBodyCount(long positiveExceptionBodyCount) {
		this.positiveExceptionBodyCount = positiveExceptionBodyCount;
	}

	public PositiveRule getPositiveRule() {
		return positiveRule;
	}

	public void setPositiveRule(PositiveRule positiveRule) {
		this.positiveRule = positiveRule;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public double getNegativeExceptionPositiveHeadConfidence() {
		return negativeExceptionPositiveHeadConfidence;
	}

	public void setNegativeExceptionPositiveHeadConfidence(double negativeExceptionPositiveHeadConfidence) {
		this.negativeExceptionPositiveHeadConfidence = negativeExceptionPositiveHeadConfidence;
	}

	public double getPositiveExceptionNegativeHeadConfidence() {
		return positiveExceptionNegativeHeadConfidence;
	}

	public void setPositiveExceptionNegativeHeadConfidence(double positiveExceptionNegativeHeadConfidence) {
		this.positiveExceptionNegativeHeadConfidence = positiveExceptionNegativeHeadConfidence;
	}

	public double getStandardConviction() {
		return standardConviction;
	}

	public void setStandardConviction(double standardConviction) {
		this.standardConviction = standardConviction;
	}

	public double getAuxiliaryConviction() {
		return auxiliaryConviction;
	}

	public void setAuxiliaryConviction(double auxiliaryConviction) {
		this.auxiliaryConviction = auxiliaryConviction;
	}

	public double getPositiveNegativeConviction() {
		return positiveNegativeConviction;
	}

	public void setPositiveNegativeConviction(double positiveNegativeConviction) {
		this.positiveNegativeConviction = positiveNegativeConviction;
	}

}
