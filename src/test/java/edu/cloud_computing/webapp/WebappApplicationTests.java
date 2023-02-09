package edu.cloud_computing.webapp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@SpringBootTest
class WebappApplicationTests {

    @Test
    void healthzTest() throws IOException {
        URL url = new URL("http://localhost:8080/healthz");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int status = connection.getResponseCode();
        connection.disconnect();
        Assertions.assertEquals(200, status);
    }
}
