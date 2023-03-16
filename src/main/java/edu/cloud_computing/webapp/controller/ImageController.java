//package edu.cloud_computing.webapp.controller;
//
//import antlr.collections.List;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.ObjectWriter;
//import edu.cloud_computing.webapp.dao.ImageDao;
//import edu.cloud_computing.webapp.dao.ProductDao;
//import edu.cloud_computing.webapp.dao.UserDao;
//import edu.cloud_computing.webapp.entity.Image;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
//import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
//import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
//import software.amazon.awssdk.services.s3.S3Client;
//
//import java.sql.Timestamp;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.time.Instant;
//import java.util.UUID;
//
//@RestController
//public class ImageController {
//    @Autowired
//    S3Client s3;
////    public static S3Client s3Client = S3Client.builder().credentialsProvider(InstanceProfileCredentialsProvider.builder().build()).build();
//
////    @PostMapping("/v1/product/{productId}/image")
////    public ResponseEntity<?> createImage(@RequestHeader HttpHeaders requestHeader, @RequestParam("image") MultipartFile file, @PathVariable("productId") int productId) throws JsonProcessingException {
////        if (!UserController.isAuthorized(requestHeader)) {
////            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{error message: 'You are not authorized.'}");
////        }
////        if (!ProductDao.checkIdExists(productId)) {
////            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'No product with this id exists.'}");
////        }
////        if (ProductDao.getProductById(productId).getOwnerUserId() != UserDao.getUserByUsername(UserController.tokenDecode(requestHeader.getFirst("Authorization"))[0]).getUserId()) {
////            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{error message: 'Restricted area! Access denied!'}");
////        }
////        Image image = new Image();
////        image.setProductId(productId);
////        image.setFileName(fileNameToUuid(file.getOriginalFilename()));
////        //
////        ImageDao.createImage(image);
////        image = ImageDao.getImageByFileName(file.getOriginalFilename());
////        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
////        ObjectWriter objectWriter = new ObjectMapper().setDateFormat(dateFormat).writer().withDefaultPrettyPrinter();
////        String jsonString = objectWriter.writeValueAsString(image);
////        return ResponseEntity.status(HttpStatus.CREATED).body(jsonString);
////    }
////
////    private String fileNameToUuid(String fileName) {
////        int splitPoint=fileName.lastIndexOf("\\.")
////        String[] splitString = fileName.split("\\.");
////        return UUID.fromString(splitString[0]) + splitString[1];
////    }
//}