package com.omar.loomdemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple liveness endpoint, also useful for confirming that requests are
 * being served on a Virtual Thread.
 */
@RestController
public class HealthController {

    /**
     * Returns a greeting including the name of the thread that served the
     * request — with {@code spring.threads.virtual.enabled=true}, this will
     * be a Virtual Thread.
     *
     * @return a greeting string
     */
    @GetMapping("/hello")
    public String hello() {
        return "Hello from " + Thread.currentThread();
    }
}
