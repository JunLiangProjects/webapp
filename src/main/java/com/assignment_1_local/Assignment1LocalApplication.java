package com.assignment_1_local;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {"com.assignment_1_local.controller", "com.assignment_1_local.dao"}, exclude = {SecurityAutoConfiguration.class})
public class Assignment1LocalApplication {

    public static void main(String[] args) {
        SpringApplication.run(Assignment1LocalApplication.class, args);
    }

}
