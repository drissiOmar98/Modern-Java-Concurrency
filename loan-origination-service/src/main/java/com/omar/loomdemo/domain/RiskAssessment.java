package com.omar.loomdemo.domain;

/**
 * Bundles the outcome of the fraud and eligibility checks performed
 * concurrently by {@code RiskAssessmentService}.
 *
 * @param fraudCheckResult      the fraud screening outcome
 * @param eligibilityResult     the eligibility screening outcome
 */
public record RiskAssessment(FraudCheckResult fraudCheckResult, EligibilityResult eligibilityResult) {

    /**
     * @return {@code true} if the applicant passed both checks
     */
    public boolean isApproved() {
        return fraudCheckResult.passed() && eligibilityResult.eligible();
    }
}
