package edu.cloud_computing.webapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import edu.cloud_computing.webapp.dao.ImageDao;
import edu.cloud_computing.webapp.dao.ProductDao;
import edu.cloud_computing.webapp.dao.UserDao;
import edu.cloud_computing.webapp.entity.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

@RestController
public class ImageController {
    @Value("${bucketName}")
    private String bucketName;// = "terraform-20230401214827217000000002";

    @GetMapping("/health")
    public ResponseEntity<?> AnotherHealthEndpoint() {
        try {
//            File file=new File();
//            String fileName = "D:\\data\\test\\newFile3.txt";

            // 转换成List<String>, 要注意java.lang.OutOfMemoryError: Java heap space
//            List<String> lines = Files.readAllLines(Paths.get("/tmp/webapp/user_data"));
////            List<String> lines = Files.readAllLines(Paths.get("D:\\Prototypes\\Java\\Cloud_Computing\\webapp\\src\\main\\resources\\ReadingTest.txt"));
////            lines.forEach(System.out::println);
//            StringBuilder stringBuilder = new StringBuilder();
//            for (String str : lines) {
//                stringBuilder.append(str);
//            }
//            bucketName = stringBuilder.toString();
            return ResponseEntity.status(HttpStatus.OK).body(bucketName);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    //    @Autowired
//    S3Client s3client;

    private static final Region region = Region.US_EAST_1;
    private static final AwsBasicCredentials awsCreds = AwsBasicCredentials.create("AKIAQJ4SCHKIRFDX3CVD", "0Y3JLytg7BXvsv4x5hK4ZlfyMJtpQu9G7lkoNYFY");//改成IAM role验证方式
    private static final S3Client s3Client = S3Client.builder().region(region).credentialsProvider(StaticCredentialsProvider.create(awsCreds)).build();
    //    private static S3Client s3Client = S3Client.builder().region(region).credentialsProvider(InstanceProfileCredentialsProvider.builder().build()).build();

    @Autowired
    private ResourceLoader resourceLoader;


    @PostMapping("/v1/product/{productId}/image")
    public ResponseEntity<?> createImage(@RequestHeader HttpHeaders requestHeader, @RequestParam("image") MultipartFile file, @PathVariable("productId") int productId) throws IOException {
        if (!UserController.isAuthorized(requestHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{error message: 'You are not authorized.'}");
        }
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'Invalid file name.'}");
        }
        if (!ProductDao.checkIdExists(productId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'No product with this id exists.'}");
        }
        if (ProductDao.getProductById(productId).getOwnerUserId() != UserDao.getUserByUsername(UserController.tokenDecode(requestHeader.getFirst("Authorization"))[0]).getUserId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{error message: 'Restricted area! Access denied!'}");
        }
        String suffix = file.getOriginalFilename().trim().substring(file.getOriginalFilename().lastIndexOf("."));
        if (!suffix.equals(".jpg") && !suffix.equals(".jpeg") && !suffix.equals(".png")) {
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
        if (!UserController.isAuthorized(requestHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{error message: 'You are not authorized.'}");
        }
        if (!ProductDao.checkIdExists(productId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'No product with this id exists.'}");
        }
        if (ProductDao.getProductById(productId).getOwnerUserId() != UserDao.getUserByUsername(UserController.tokenDecode(requestHeader.getFirst("Authorization"))[0]).getUserId()) {
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
        return ResponseEntity.status(HttpStatus.CREATED).body(jsonString.toString());
    }

    @GetMapping("/v1/product/{productId}/image/{imageId}")
    public ResponseEntity<?> getImage(@RequestHeader HttpHeaders requestHeader, @PathVariable("productId") int productId, @PathVariable("imageId") int imageId) throws JsonProcessingException {
        if (!UserController.isAuthorized(requestHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{error message: 'You are not authorized.'}");
        }
        if (!ProductDao.checkIdExists(productId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'No product with this id exists.'}");
        }
        if (ProductDao.getProductById(productId).getOwnerUserId() != UserDao.getUserByUsername(UserController.tokenDecode(requestHeader.getFirst("Authorization"))[0]).getUserId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{error message: 'Restricted area! Access denied!'}");
        }
        if (!ImageDao.checkImageIdExistsUnderProductId(imageId, productId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'No image with this id exists for the product you selected.'}");
        }
        Image image = ImageDao.getImageByImageId(imageId);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
        ObjectWriter objectWriter = new ObjectMapper().setDateFormat(dateFormat).writer().withDefaultPrettyPrinter();
        String jsonString = objectWriter.writeValueAsString(image);
        return ResponseEntity.status(HttpStatus.CREATED).body(jsonString);
    }

    @DeleteMapping("/v1/product/{productId}/image/{imageId}")
    public ResponseEntity<?> deleteImage(@RequestHeader HttpHeaders requestHeader, @PathVariable("productId") int productId, @PathVariable("imageId") int imageId) {
        if (!UserController.isAuthorized(requestHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{error message: 'You are not authorized.'}");
        }
        if (!ProductDao.checkIdExists(productId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'No product with this id exists.'}");
        }
        if (ProductDao.getProductById(productId).getOwnerUserId() != UserDao.getUserByUsername(UserController.tokenDecode(requestHeader.getFirst("Authorization"))[0]).getUserId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{error message: 'Restricted area! Access denied!'}");
        }
        if (!ImageDao.checkImageIdExistsUnderProductId(imageId, productId)) {
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

//原因是这个地方的log_stream_name只能是cloudwatch_log_stream