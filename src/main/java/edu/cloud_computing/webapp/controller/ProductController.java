package edu.cloud_computing.webapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import edu.cloud_computing.webapp.dao.ProductDao;
import edu.cloud_computing.webapp.dao.UserDao;
import edu.cloud_computing.webapp.entity.Product;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactoryLoader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Iterator;

@RestController
public class ProductController {
    @PostMapping("/v1/product")
    public ResponseEntity<?> createProduct(@RequestHeader HttpHeaders requestHeader, @RequestBody String requestBody) throws JsonProcessingException {
        if (!UserController.isAuthorized(requestHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{error message: 'You are not authorized.'}");
        }
        ObjectMapper mapper = new ObjectMapper();
        Product product = mapper.readValue(requestBody, Product.class);
        HashSet<String> hashSet = new HashSet<>();//Here's just a modified version of checking illegal field.
        hashSet.add("name");
        hashSet.add("description");
        hashSet.add("sku");
        hashSet.add("manufacturer");
        hashSet.add("quantity");
        Iterator<String> keys = new JSONObject(requestBody).keys();
        boolean hasQuantity = false;//Checking whether we're changing quantity.
        while (keys.hasNext()) {
            String key = keys.next();
            if (!hashSet.contains(key)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'only product name, description, SKU, manufacturer and an integer quantity between 0 and 100 are allowed during input'}");
            }
            if (key.equals("quantity")) {
                hasQuantity = true;
            }
        }
        if (product.getName() == null || product.getDescription() == null || product.getSku() == null || product.getManufacturer() == null || !hasQuantity || product.getQuantity() < 0 || product.getQuantity() > 100) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message:'Your must provide and only provide product name, description, SKU, manufacturer and an integer quantity between 0 and 100 to create!'}");
        }//What if quantity is not an int?
        if (ProductDao.checkSkuExists(product.getSku())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'This SKU for product is already occupied!'}");
        }
        product.setOwnerUserId(UserDao.getUserByUsername(UserController.tokenDecode(requestHeader.getFirst("Authorization"))[0]).getUserId());
        ProductDao.createProduct(product);
        product = ProductDao.getProductBySku(product.getSku());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSZ");
        ObjectWriter objectWriter = new ObjectMapper().setDateFormat(dateFormat).writer().withDefaultPrettyPrinter();
        String jsonString = objectWriter.writeValueAsString(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(jsonString);
    }

    @GetMapping("/v1/product/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable("productId") int productId) {
        try {
            if (!ProductDao.checkIdExists(productId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{error message: 'No product with this id exists.'}");
            }
            Product product = ProductDao.getProductById(productId);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            ObjectWriter ow = new ObjectMapper().setDateFormat(df).writer().withDefaultPrettyPrinter();
            String jsonStr = ow.writeValueAsString(product);
            return ResponseEntity.status(HttpStatus.OK).body(jsonStr);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/v1/product/{productId}")
    public ResponseEntity<?> updateEntireProduct(@RequestHeader HttpHeaders requestHeader, @RequestBody String requestBody, @PathVariable("productId") int productId) {
        try {
            if (!UserController.isAuthorized(requestHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{error message: 'You are not authorized.'}");
            }
            HashSet<String> hashSet = new HashSet<>();//Here's just a modified version of checking illegal field.
            hashSet.add("name");
            hashSet.add("description");
            hashSet.add("sku");
            hashSet.add("manufacturer");
            hashSet.add("quantity");
            Iterator<String> keys = new JSONObject(requestBody).keys();
            boolean hasQuantity = false;//Checking whether we're changing quantity.
            while (keys.hasNext()) {
                String key = keys.next();
                if (!hashSet.contains(key)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'only product name, description, SKU, manufacturer and an integer quantity between 0 and 100 are allowed during input'}");
                }
                if (key.equals("quantity")) {
                    hasQuantity = true;
                }
            }
            if (!ProductDao.checkIdExists(productId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{error message: 'No product with this id exists.'}");
            }
            Product product = new ObjectMapper().readValue(requestBody, Product.class);
            if (product.getName() == null || product.getDescription() == null || product.getSku() == null || product.getManufacturer() == null || !hasQuantity||product.getQuantity() < 0 || product.getQuantity() > 100) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message:'Your must provide and only provide product name, description, SKU, manufacturer and an integer quantity between 0 and 100 to update!'}");
            }//What if quantity is not an int?
            Product oldProduct = ProductDao.getProductById(productId);
            if (!product.getSku().equals(oldProduct.getSku()) && ProductDao.checkSkuExists(product.getSku())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'This SKU for product is already occupied!'}");
            }
            if (isForbidden(requestHeader, productId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{error message: 'Restricted area! Access denied!'}");
            }
            product.setProductId(productId);
            product.setDateAdded(oldProduct.getDateAdded());
            product.setOwnerUserId(oldProduct.getOwnerUserId());
            ProductDao.updateProduct(product);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PatchMapping("/v1/product/{productId}")
    public ResponseEntity<?> updateProduct(@RequestHeader HttpHeaders requestHeader, @RequestBody String requestBody, @PathVariable("productId") int productId) {
        try {
            if (!UserController.isAuthorized(requestHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{error message: 'You are not authorized.'}");
            }
            HashSet<String> hashSet = new HashSet<>();//Here's just a modified version of checking illegal field.
            hashSet.add("name");
            hashSet.add("description");
            hashSet.add("sku");
            hashSet.add("manufacturer");
            hashSet.add("quantity");
            Iterator<String> keys = new JSONObject(requestBody).keys();
            boolean changingQuantity = false;//Checking whether we're changing quantity.
            while (keys.hasNext()) {
                String key = keys.next();
                if (!hashSet.contains(key)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'only product name, description, SKU, manufacturer and an integer quantity between 0 and 100 are allowed during input'}");
                }
                if (key.equals("quantity")) {
                    changingQuantity = true;
                }
            }
            Product product = new ObjectMapper().readValue(requestBody, Product.class);
            if (changingQuantity && (product.getQuantity() < 0 || product.getQuantity() > 100)) {//What if quantity is not an int?
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'only an integer quantity between 0 and 100 are allowed during input'}");
            }
            Product oldProduct = ProductDao.getProductById(productId);
            if (product.getSku() != null && !product.getSku().equals(oldProduct.getSku()) && ProductDao.checkSkuExists(product.getSku())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'This SKU for product is already occupied!'}");
            }
            if (isForbidden(requestHeader, productId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{error message: 'Restricted area! Access denied!'}");
            }
            if (product.getName() != null) {
                oldProduct.setName(product.getName());
            }
            if (product.getDescription() != null) {
                oldProduct.setDescription(product.getDescription());
            }
            if (product.getSku() != null) {
                oldProduct.setSku(product.getSku());
            }
            if (product.getManufacturer() != null) {
                oldProduct.setManufacturer(product.getManufacturer());
            }
            if (changingQuantity) {//What if quantity is not an int?
                oldProduct.setQuantity(product.getQuantity());
            }
            ProductDao.updateProduct(oldProduct);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/v1/product/{productId}")
    public ResponseEntity<?> deleteProduct(@RequestHeader HttpHeaders requestHeader, @PathVariable("productId") int productId) {
        try {
            if (!UserController.isAuthorized(requestHeader)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{error message: 'You are not authorized.'}");
            }
            if (productId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'Product id must be a positive integer.'}");
            }
            if (!ProductDao.checkIdExists(productId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{error message: 'Product with this id does not exist.'}");
            }
            int userId = UserDao.getUserByUsername(UserController.tokenDecode(requestHeader.getFirst("Authorization"))[0]).getUserId();
            Product product = ProductDao.getProductById(productId);
            if (userId != product.getOwnerUserId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{error message: 'Restricted area! Access denied!'}");
            }
            ProductDao.deleteProduct(product);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    public Boolean isForbidden(HttpHeaders requestHeader, int productId) {
        if (ProductDao.checkIdExists(productId)) {//The user you are looking for should exist
            //userId match. You can't log in yourself to touch others'
            return ProductDao.getProductById(productId).getOwnerUserId() != UserDao.getUserByUsername(UserController.tokenDecode(requestHeader.getFirst("Authorization"))[0]).getUserId();
        }
        return true;
    }

    public Boolean hasIllegalField(String body) {
        HashSet<String> hashSet = new HashSet<>();
        hashSet.add("name");
        hashSet.add("description");
        hashSet.add("sku");
        hashSet.add("manufacturer");
        hashSet.add("quantity");
        Iterator<String> keys = new JSONObject(body).keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!hashSet.contains(key)) {
                return true;
            }
        }
        return false;
    }
}