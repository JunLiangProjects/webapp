package edu.cloud_computing.webapp.controller;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthzController {
//    private final Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    private final Logger logger = LoggerFactory.getLogger(HealthzController.class);

    @GetMapping("/healthz")
    public ResponseEntity<?> HealthEndpoint() {
        try {
//            Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
//            logger.trace("trace level");
//            logger.debug("debug level");
//            logger.info("info level");
//            logger.warn("warn level");
//            logger.error("error level");
//            logger.fatal("fatal level");
            logger.info("User requests to check \"healthz\" condition of the server.");
            return ResponseEntity.status(HttpStatus.OK).body("");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}