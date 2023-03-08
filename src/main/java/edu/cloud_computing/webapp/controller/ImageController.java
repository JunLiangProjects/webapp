package edu.cloud_computing.webapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import edu.cloud_computing.webapp.dao.ImageDao;
import edu.cloud_computing.webapp.dao.ProductDao;
import edu.cloud_computing.webapp.dao.UserDao;
import edu.cloud_computing.webapp.entity.Image;
import edu.cloud_computing.webapp.entity.Product;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;

public class ImageController {
    @PostMapping("/v1/product")
    public ResponseEntity<?> createImage(@RequestHeader HttpHeaders requestHeader, @RequestBody String requestBody, @PathVariable("productId") int productId) throws JsonProcessingException {
        if (!UserController.isAuthorized(requestHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{error message: 'You are not authorized.'}");
        }
        if (!ProductDao.checkIdExists(productId) ){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'No product with this id exists.'}");
        }
        if (ProductDao.getProductById(productId).getOwnerUserId() != UserDao.getUserByUsername(UserController.tokenDecode(requestHeader.getFirst("Authorization"))[0]).getUserId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{error message: 'Restricted area! Access denied!'}");
        }
        ObjectMapper mapper = new ObjectMapper();
        Image image = mapper.readValue(requestBody, Image.class);
        image.setProductId(productId);
        image.setDateCreated(Timestamp.from(Instant.now()));
        ImageDao.createImage(image);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSZ");
        ObjectWriter objectWriter = new ObjectMapper().setDateFormat(dateFormat).writer().withDefaultPrettyPrinter();
        String jsonString = objectWriter.writeValueAsString(image);
        return ResponseEntity.status(HttpStatus.CREATED).body(jsonString);
    }


}
