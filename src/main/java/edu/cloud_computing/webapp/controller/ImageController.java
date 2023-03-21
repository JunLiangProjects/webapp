package edu.cloud_computing.webapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import edu.cloud_computing.webapp.dao.ImageDao;
import edu.cloud_computing.webapp.dao.ProductDao;
import edu.cloud_computing.webapp.dao.UserDao;
import edu.cloud_computing.webapp.entity.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

@RestController
public class ImageController {
//    @Autowired
//    S3Client s3client;
//    public static S3Client s3Client = S3Client.builder().credentialsProvider(InstanceProfileCredentialsProvider.builder().build()).build();

    @PostMapping("/v1/product/{productId}/image")
    public ResponseEntity<?> createImage(@RequestHeader HttpHeaders requestHeader, @RequestParam("image") MultipartFile file, @PathVariable("productId") int productId) throws JsonProcessingException {
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
        Image image = new Image();
        image.setProductId(productId);
        image.setFileName(file.getOriginalFilename());
        //Save to bucket
        image.setS3BucketPath("default path");
        ImageDao.createImage(image);
        image = ImageDao.getImageByFileNameAndProductId(file.getOriginalFilename(), productId);
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
        ImageDao.deleteImage(image);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
    }

//    @Value("${aws.s3.bucket-name}")
//    private String bucketName;
//
//    //调用withCredentials()方法将一个InstanceProfileCredentialsProvider对象传递给AmazonS3ClientBuilder对象。
//    //InstanceProfileCredentialsProvider从Amazon EC2实例元数据服务中获取IAM角色的凭证
//
//
//    @PostMapping(value = "/product/{product_id}/image")
//    public Object uploadProductImage(@PathVariable("product_id") Long productId,
//                                     @RequestParam("file") MultipartFile file) throws IOException {
//        SecurityContext context = SecurityContextHolder.getContext();
//        Authentication authentication = context.getAuthentication();
//        Object principal = authentication.getPrincipal();
//        String userName = ((UserDetails) principal).getUsername();
//        long id = userService.getId(userName);
//        if (id != productService.findOwnerId(productId)) {
//            return new ExceptionMessage().fail();
//        }
//        // Get file extension
//        String extension = getFileExtension(file);
//        // Generate unique file name
//        String fileName = UUID.randomUUID().toString() + "." + extension;
//        // Create file in temporary directory
//        File tempFile = Files.createTempFile(fileName, extension).toFile();
//        file.transferTo(tempFile);
//        // Upload file to S3 bucket
//        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, productId + "/" + fileName, tempFile)
//                .withCannedAcl(CannedAccessControlList.PublicRead);
//        s3client.putObject(putObjectRequest);
//        // Delete temporary file
//        tempFile.delete();
//        // Generate URL for uploaded file
//        String fileUrl = s3client.getUrl(bucketName, productId + "/" + fileName).toExternalForm();
//        // store the image information in RDS
//        Image image = new Image(productId, fileName, fileUrl);
//        imageDao.save(image);
//        return ResponseEntity.status(HttpStatus.OK).body(image);
//    }
}