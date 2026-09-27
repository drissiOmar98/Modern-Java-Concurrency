package com.omar.loomdemo.util;


import com.omar.loomdemo.config.RiskCheckProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Simulates the processing time of a risk-check backend, occasionally
 * running deliberately slow so that callers with a deadline-bound
 * {@code StructuredTaskScope} join have something real to time out on.
 */
@Component
public class RiskCheckSimulator {

    private static final Logger logger = LoggerFactory.getLogger(RiskCheckSimulator.class);

    private final RiskCheckProperties properties;

    public RiskCheckSimulator(RiskCheckProperties properties) {
        this.properties = properties;
    }

    /**
     * Blocks the current thread to simulate the given check taking real
     * time, occasionally running well past the configured maximum.
     *
     * @param checkName a short, human-readable name for the check being simulated
     */
    public void simulate(String checkName) {
        boolean runSlow = Math.random() * 100 < properties.slowCheckChancePercent();
        long min = properties.minSeconds();
        long max = properties.maxSeconds();

        long delaySeconds = runSlow
                ? max * 3
                : min + (long) (Math.random() * (max - min + 1));

        logger.info("Performing check: {}(). Simulated as {}. Time to complete: {} seconds",
                checkName, runSlow ? "SLOW" : "normal", delaySeconds);
        try {
            Thread.sleep(delaySeconds * 1_000);
            logger.info("Done check: {}()", checkName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
