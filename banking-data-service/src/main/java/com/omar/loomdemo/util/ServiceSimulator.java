package com.omar.loomdemo.util;


import com.omar.loomdemo.config.SimulatedLatencyProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Simulates the latency of a real backend call by logging and sleeping for a
 * random duration within the configured range.
 *
 * <p>This replaces the previous static {@code ServicesUtil} helper: the
 * delay range now comes from {@link SimulatedLatencyProperties} instead of
 * being hard-coded, and it's a Spring bean so it can be swapped or mocked in
 * tests.
 */
@Component
public class ServiceSimulator {

    private static final Logger logger = LoggerFactory.getLogger(ServiceSimulator.class);

    private final SimulatedLatencyProperties latencyProperties;

    public ServiceSimulator(SimulatedLatencyProperties latencyProperties) {
        this.latencyProperties = latencyProperties;
    }

    /**
     * Blocks the current thread for a random duration within the configured
     * min/max range, logging before and after, to simulate the given
     * backend task taking real time to complete.
     *
     * @param task a short, human-readable name for the task being simulated
     */
    public void simulate(String task) {
        var min = latencyProperties.minSeconds();
        var max = latencyProperties.maxSeconds();
        long delaySeconds = min + (long) (Math.random() * (max - min + 1));

        logger.info("Performing task: {}(). Time to complete: {} seconds", task, delaySeconds);
        try {
            Thread.sleep(delaySeconds * 1_000);
            logger.info("Done task: {}()", task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
