package edu.cloud_computing.webapp;

import edu.cloud_computing.webapp.controller.HealthzController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

@SpringBootTest
@AutoConfigureMockMvc
class WebappApplicationTests {
    @Autowired
    HealthzController healthzController;

    @Test
    void contextLoads() {
        Assertions.assertEquals(HttpStatus.OK, healthzController.HealthEndpoint().getStatusCode());
    }
}