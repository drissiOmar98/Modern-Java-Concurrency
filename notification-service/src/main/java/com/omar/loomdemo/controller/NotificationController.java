package com.omar.loomdemo.controller;


import com.omar.loomdemo.domain.EmailNotificationRequest;
import com.omar.loomdemo.domain.NotificationResult;
import com.omar.loomdemo.domain.SmsNotificationRequest;
import com.omar.loomdemo.util.NotificationSimulator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes dummy email and SMS sending endpoints, each with simulated
 * latency and an independent chance of failure.
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationSimulator simulator;

    public NotificationController(NotificationSimulator simulator) {
        this.simulator = simulator;
    }

    /**
     * Simulates sending an email notification.
     */
    @PostMapping("/email")
    public NotificationResult sendEmail(@RequestBody EmailNotificationRequest request) {
        simulator.send("email");
        return new NotificationResult("email", true);
    }

    /**
     * Simulates sending an SMS notification.
     */
    @PostMapping("/sms")
    public NotificationResult sendSms(@RequestBody SmsNotificationRequest request) {
        simulator.send("sms");
        return new NotificationResult("sms", true);
    }
}
