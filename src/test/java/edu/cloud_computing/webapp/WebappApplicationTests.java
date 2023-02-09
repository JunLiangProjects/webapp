package edu.cloud_computing.webapp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SpringBootTest
class WebappApplicationTests {


    @Test
    void healthzTest() throws IOException, URISyntaxException {
//        RestTemplate restTemplate = new RestTemplate();
//        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
//        mockServer.expect(ExpectedCount.once(),
//                        requestTo(new URI("http://localhost:8080/healthz")))
//                .andExpect(method(HttpMethod.GET))
//                .andRespond(withStatus(HttpStatus.OK)
//                        .body("")
//                );
//
//        restTemplate.get
//
//        URL url = new URL("http://localhost:8080/healthz");
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("GET");
//        connection.connect();
//        int status = connection.getResponseCode();
//        connection.disconnect();
//        Assertions.assertEquals(200, status);
    }
}