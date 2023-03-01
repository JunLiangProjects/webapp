package edu.cloud_computing.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

//@SpringBootApplication(scanBasePackages = {"edu.cloud_computing.webapp.controller", "edu.cloud_computing.webapp.dao", "edu.cloud_computing.webapp.entity"}, exclude = {SecurityAutoConfiguration.class})
//public class WebappApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebappApplication.class, args);
    }

}
