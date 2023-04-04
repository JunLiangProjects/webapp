package edu.cloud_computing.webapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
public class HealthzController {
    @GetMapping("/healthz")
    public ResponseEntity<?> HealthEndpoint() {
        try {
//            File file=new File();
//            String fileName = "D:\\data\\test\\newFile3.txt";

            // 转换成List<String>, 要注意java.lang.OutOfMemoryError: Java heap space
            List<String> lines = Files.readAllLines(Paths.get("/tmp/webapp/user_data"));
//            List<String> lines = Files.readAllLines(Paths.get("D:\\Prototypes\\Java\\Cloud_Computing\\webapp\\src\\main\\resources\\ReadingTest.txt"));
//            lines.forEach(System.out::println);
            StringBuilder stringBuilder = new StringBuilder();
            for (String str : lines) {
                stringBuilder.append(str);
            }
            return ResponseEntity.status(HttpStatus.OK).body(stringBuilder.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}