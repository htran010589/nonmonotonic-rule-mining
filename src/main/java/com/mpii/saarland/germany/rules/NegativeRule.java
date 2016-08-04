package com.mpii.saarland.germany.rules;

public class NegativeRule {

	private PositiveRule rule;

	private String exception;

	private NegativeRuleType type;

	private long negativeExceptionPositiveHeadRuleCount, negativeExceptionBodyCount,
			positiveExceptionNegativeHeadRuleCount, positiveExceptionBodyCount;

	private double negativeExceptionPositiveHeadConfidence, positiveExceptionNegativeHeadConfidence, standardConviction,
			auxiliaryConviction, positiveNegativeConviction;

	public void getConviction() {
		negativeExceptionPositiveHeadConfidence = 1.0 * negativeExceptionPositiveHeadRuleCount
				/ negativeExceptionBodyCount;
		positiveExceptionNegativeHeadConfidence = 1.0 * positiveExceptionNegativeHeadRuleCount
				/ positiveExceptionBodyCount;
		standardConviction = (1 - rule.getHeadSupport()) / (1 - negativeExceptionPositiveHeadConfidence);
		auxiliaryConviction = rule.getHeadSupport() / (1 - positiveExceptionNegativeHeadConfidence);
		positiveNegativeConviction = (standardConviction + auxiliaryConviction) / 2;
	}

}
