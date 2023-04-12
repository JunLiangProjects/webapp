package edu.cloud_computing.webapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import edu.cloud_computing.webapp.dao.ImageDao;
import edu.cloud_computing.webapp.dao.ProductDao;
import edu.cloud_computing.webapp.dao.UserDao;
import edu.cloud_computing.webapp.entity.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

@RestController
public class ImageController {
    @Value("${bucketName}")
    private String bucketName;

    @Autowired
    private ResourceLoader resourceLoader;

    private static final S3Client s3Client = S3Client.builder().region(Region.US_EAST_1).credentialsProvider(InstanceProfileCredentialsProvider.builder().build()).build();
    private final Logger logger = LoggerFactory.getLogger(ImageController.class);
    private final StatsDClient statsDClient = new NonBlockingStatsDClient("csye6225", "localhost", 8125);

    @PostMapping("/v1/product/{productId}/image")
    public ResponseEntity<?> createImage(@RequestHeader HttpHeaders requestHeader, @RequestParam("image") MultipartFile file, @PathVariable("productId") int productId) throws IOException {
        statsDClient.incrementCounter("ImageController.PostMapping.createImage");
        logger.info("User requests to upload an image.");
        if (!UserController.isAuthorized(requestHeader)) {
            logger.warn("You are not authorized.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{error message: 'You are not authorized.'}");
        }
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
            logger.warn("Invalid file name.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'Invalid file name.'}");
        }
        if (!ProductDao.checkIdExists(productId)) {
            logger.warn("No product with this id exists.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'No product with this id exists.'}");
        }
        if (ProductDao.getProductById(productId).getOwnerUserId() != UserDao.getUserByUsername(UserController.tokenDecode(requestHeader.getFirst("Authorization"))[0]).getUserId()) {
            logger.warn("Restricted area! Access denied!");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{error message: 'Restricted area! Access denied!'}");
        }
        String suffix = file.getOriginalFilename().trim().substring(file.getOriginalFilename().lastIndexOf("."));
        if (!suffix.equals(".jpg") && !suffix.equals(".jpeg") && !suffix.equals(".png")) {
            logger.warn("Only jpg, jpeg and png allowed.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'Only jpg, jpeg and png allowed.'}");
        }
        String uuidFileName = UUID.randomUUID() + suffix;
        String filePath = resourceLoader.getResource("").getFile().getPath() + "/" + uuidFileName;
        System.out.println(filePath);
        file.transferTo(new File(filePath));
        Image image = new Image();
        image.setProductId(productId);
        image.setFileName(uuidFileName);
        //Save to bucket
        PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(uuidFileName).build();
        s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromFile(new File(filePath)));
        GetUrlRequest urlRequest = GetUrlRequest.builder().bucket(bucketName).key(uuidFileName).build();
        String bucketPath = s3Client.utilities().getUrl(urlRequest).toExternalForm();
        //Bucket operation done
        image.setS3BucketPath(bucketPath);
        ImageDao.createImage(image);
        image = ImageDao.getImageByFileNameAndProductId(uuidFileName, productId);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
        ObjectWriter objectWriter = new ObjectMapper().setDateFormat(dateFormat).writer().withDefaultPrettyPrinter();
        String jsonString = objectWriter.writeValueAsString(image);
        return ResponseEntity.status(HttpStatus.CREATED).body(jsonString);
    }

    @GetMapping("/v1/product/{productId}/image")
    public ResponseEntity<?> getImageList(@RequestHeader HttpHeaders requestHeader, @PathVariable("productId") int productId) throws JsonProcessingException {
        statsDClient.incrementCounter("ImageController.GetMapping.getImageList");
        logger.info("User requests information of a list of images.");
        if (!UserController.isAuthorized(requestHeader)) {
            logger.warn("You are not authorized.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{error message: 'You are not authorized.'}");
        }
        if (!ProductDao.checkIdExists(productId)) {
            logger.warn("No product with this id exists.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'No product with this id exists.'}");
        }
        if (ProductDao.getProductById(productId).getOwnerUserId() != UserDao.getUserByUsername(UserController.tokenDecode(requestHeader.getFirst("Authorization"))[0]).getUserId()) {
            logger.warn("Restricted area! Access denied!");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{error message: 'Restricted area! Access denied!'}");
        }
        List<Image> imageList = ImageDao.getImageListByProductId(productId);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
        ObjectWriter objectWriter = new ObjectMapper().setDateFormat(dateFormat).writer().withDefaultPrettyPrinter();
        StringBuilder jsonString = new StringBuilder();
        for (Image image : imageList) {
            jsonString.append(objectWriter.writeValueAsString(image));
            jsonString.append('\n');
        }
        return ResponseEntity.status(HttpStatus.OK).body(jsonString.toString());
    }

    @GetMapping("/v1/product/{productId}/image/{imageId}")
    public ResponseEntity<?> getImage(@RequestHeader HttpHeaders requestHeader, @PathVariable("productId") int productId, @PathVariable("imageId") int imageId) throws JsonProcessingException {
        statsDClient.incrementCounter("ImageController.GetMapping.getImage");
        logger.info("User requests information of an image.");
        if (!UserController.isAuthorized(requestHeader)) {
            logger.warn("You are not authorized.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{error message: 'You are not authorized.'}");
        }
        if (!ProductDao.checkIdExists(productId)) {
            logger.warn("No product with this id exists.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'No product with this id exists.'}");
        }
        if (ProductDao.getProductById(productId).getOwnerUserId() != UserDao.getUserByUsername(UserController.tokenDecode(requestHeader.getFirst("Authorization"))[0]).getUserId()) {
            logger.warn("Restricted area! Access denied!");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{error message: 'Restricted area! Access denied!'}");
        }
        if (!ImageDao.checkImageIdExistsUnderProductId(imageId, productId)) {
            logger.warn("No image with this id exists for the product you selected.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'No image with this id exists for the product you selected.'}");
        }
        Image image = ImageDao.getImageByImageId(imageId);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
        ObjectWriter objectWriter = new ObjectMapper().setDateFormat(dateFormat).writer().withDefaultPrettyPrinter();
        String jsonString = objectWriter.writeValueAsString(image);
        return ResponseEntity.status(HttpStatus.OK).body(jsonString);
    }

    @DeleteMapping("/v1/product/{productId}/image/{imageId}")
    public ResponseEntity<?> deleteImage(@RequestHeader HttpHeaders requestHeader, @PathVariable("productId") int productId, @PathVariable("imageId") int imageId) {
        statsDClient.incrementCounter("ImageController.DeleteMapping.deleteImage");
        logger.info("User requests to delete a specific image.");
        if (!UserController.isAuthorized(requestHeader)) {
            logger.warn("You are not authorized.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{error message: 'You are not authorized.'}");
        }
        if (!ProductDao.checkIdExists(productId)) {
            logger.warn("No product with this id exists.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'No product with this id exists.'}");
        }
        if (ProductDao.getProductById(productId).getOwnerUserId() != UserDao.getUserByUsername(UserController.tokenDecode(requestHeader.getFirst("Authorization"))[0]).getUserId()) {
            logger.warn("Restricted area! Access denied!");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{error message: 'Restricted area! Access denied!'}");
        }
        if (!ImageDao.checkImageIdExistsUnderProductId(imageId, productId)) {
            logger.warn("No image with this id exists for the product you selected.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'No image with this id exists for the product you selected.'}");
        }
        Image image = ImageDao.getImageByImageId(imageId);
        //delete from bucket
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName).key(image.getFileName()).build();
        s3Client.deleteObject(deleteObjectRequest);
        //Bucket operation done
        ImageDao.deleteImage(image);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
    }
}