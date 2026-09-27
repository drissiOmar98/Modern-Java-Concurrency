package com.omar.loomdemo.controller;


import com.omar.loomdemo.domain.EligibilityResult;
import com.omar.loomdemo.domain.FraudCheckResult;
import com.omar.loomdemo.util.RiskCheckSimulator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes dummy fraud and eligibility screening endpoints, each with a
 * simulated (and occasionally slow) processing delay.
 */
@RestController
public class RiskCheckController {

    private final RiskCheckSimulator simulator;

    public RiskCheckController(RiskCheckSimulator simulator) {
        this.simulator = simulator;
    }

    /**
     * Runs a dummy fraud screen for the given customer and requested amount.
     */
    @GetMapping("/customers/{id}/fraud-check")
    public FraudCheckResult fraudCheck(@PathVariable("id") String customerId,
                                        @RequestParam("amount") String amount) {
        simulator.simulate("fraudCheck");
        // Deterministic dummy scoring so the demo is reproducible per amount.
        int riskScore = Math.floorMod(customerId.hashCode() + amount.hashCode(), 100);
        return new FraudCheckResult(riskScore < 90, riskScore);
    }

    /**
     * Runs a dummy eligibility screen for the given customer and requested
     * amount/purpose.
     */
    @GetMapping("/customers/{id}/eligibility-check")
    public EligibilityResult eligibilityCheck(@PathVariable("id") String customerId,
                                              @RequestParam("amount") String amount) {
        simulator.simulate("eligibilityCheck");
        return new EligibilityResult(true, "Meets minimum eligibility criteria");
    }
}
