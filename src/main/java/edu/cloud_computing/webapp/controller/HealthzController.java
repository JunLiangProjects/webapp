package edu.cloud_computing.webapp.controller;

import com.timgroup.statsd.StatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthzController {
    private final Logger logger = LoggerFactory.getLogger(HealthzController.class);
    private final StatsDClient statsDClient = new NonBlockingStatsDClient("csye6225", "localhost", 8125);

    @GetMapping("/healthz")
    public ResponseEntity<?> HealthEndpoint() {
        try {
            statsDClient.incrementCounter("HealthzController.GetMapping.HealthEndpoint");
            logger.info("User requests to check \"healthz\" condition of the server.");
            return ResponseEntity.status(HttpStatus.OK).body("");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}