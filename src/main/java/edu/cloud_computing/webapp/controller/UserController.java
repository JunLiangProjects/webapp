package edu.cloud_computing.webapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import edu.cloud_computing.webapp.dao.UserDao;
import edu.cloud_computing.webapp.entity.User;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;

@RestController
public class UserController {
    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final StatsDClient statsDClient = new NonBlockingStatsDClient("csye6225", "localhost", 8125);

    @PostMapping("/v1/user")
    public ResponseEntity<?> createUser(@RequestBody String requestBody) throws JsonProcessingException {
        statsDClient.incrementCounter("UserController.PostMapping.createUser");
        logger.info("User requests to create a user.");
        if (hasIllegalField(requestBody)) {
            logger.warn("Only user name, password, first name and last name allowed during input.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'Only user name, password, first name and last name allowed during input.'}");
        }
        User user = new ObjectMapper().readValue(requestBody, User.class);
        String username = user.getUsername();
        if (user.getUsername() == null || user.getPassword() == null || user.getFirstName() == null || user.getLastName() == null) {
            logger.warn("Your must provide user name, password, first name and last name to register.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message:'Your must provide user name, password, first name and last name to register.'}");
        }
        if (!username.matches("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,6}$")) {
            logger.warn("Your username has to be valid email address.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message:'Your username has to be valid email address.'}");
        } else if (UserDao.checkUsernameExists(username)) {
            logger.warn("This username already occupied.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'This username already occupied.'}");
        }
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        UserDao.createUser(user);
        user = UserDao.getUserByUsername(username);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        ObjectWriter objectWriter = new ObjectMapper().setDateFormat(dateFormat).writer().withDefaultPrettyPrinter();
        String jsonString = objectWriter.writeValueAsString(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(jsonString);
    }

    @GetMapping("/v1/user/{userId}")
    public ResponseEntity<?> getUser(@RequestHeader HttpHeaders requestHeader, @PathVariable("userId") int userId) {
        try {
            statsDClient.incrementCounter("UserController.GetMapping.getUser");
            logger.info("User requests information of  a user.");
            if (!isAuthorized(requestHeader)) {
                logger.warn("You are not authorized.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{error message: 'You are not authorized.'}");
            }
            if (isForbidden(requestHeader, userId)) {
                logger.warn("Restricted area! Access denied!");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{error message: 'Restricted area! Access denied!'}");
            }
            User user = UserDao.getUserById(userId);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            ObjectWriter ow = new ObjectMapper().setDateFormat(df).writer().withDefaultPrettyPrinter();
            String jsonStr = ow.writeValueAsString(user);
            return ResponseEntity.status(HttpStatus.OK).body(jsonStr);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/v1/user/{userId}")
    public ResponseEntity<?> updateUser(@RequestHeader HttpHeaders requestHeader, @RequestBody String body, @PathVariable("userId") int userId) {
        try {
            statsDClient.incrementCounter("UserController.PutMapping.updateUser");
            logger.info("User requests to update a user.");
            if (hasIllegalField(body)) {
                logger.warn("Only user name, password, first name and last name are allowed during input.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'Only user name, password, first name and last name are allowed during input.'}");
            }
            if (!isAuthorized(requestHeader)) {
                logger.warn("You are not authorized.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{error message: 'You are not authorized.'}");
            }
            if (isForbidden(requestHeader, userId)) {
                logger.warn("Restricted area! Access denied!");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{error message: 'Restricted area! Access denied!'}");
            }
            User oldUser = UserDao.getUserById(userId);
            User user = new ObjectMapper().readValue(body, User.class);
            if (!oldUser.getUsername().equals(user.getUsername())) {
                logger.warn("You can not change your username.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'You can not change your username.'}");
            }
            if (user.getPassword() != null) {
                if (user.getPassword().equals("")) {
                    logger.warn("Password can not be empty.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{error message: 'Password can not be empty.'}");
                } else {
                    oldUser.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
                }
            }
            if (user.getFirstName() != null) {
                oldUser.setFirstName(user.getFirstName());
            }
            if (user.getLastName() != null) {
                oldUser.setLastName(user.getLastName());
            }
            UserDao.updateUser(oldUser);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    public static Boolean isAuthorized(HttpHeaders requestHeader) {
        if (requestHeader.containsKey("Authorization") && requestHeader.getFirst("Authorization") != null) {//Has authentication
            return tokenAuthorized(requestHeader.getFirst("Authorization"));//username & password correct
        }
        return false;
    }

    public Boolean isForbidden(HttpHeaders requestHeader, int userId) {
        if (UserDao.checkIdExists(userId)) {//The user you are looking for should exist
            //userId match. You can't log in yourself to touch others'
            return userId != UserDao.getUserByUsername(tokenDecode(requestHeader.getFirst("Authorization"))[0]).getUserId();
        }
        return true;
    }

    public Boolean hasIllegalField(String body) {
        HashSet<String> hashSet = new HashSet<>();
        hashSet.add("username");
        hashSet.add("password");
        hashSet.add("firstName");
        hashSet.add("lastName");
        Iterator<String> keys = new JSONObject(body).keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!hashSet.contains(key)) {
                return true;
            }
        }
        return false;
    }

    public static Boolean tokenAuthorized(String token) {//comparing input username & password match the record
        String[] userInfo = tokenDecode(token);
        if (userInfo.length != 2 || userInfo[0].equals("")) {
            return false;
        }
        if (!UserDao.checkUsernameExists(userInfo[0])) {
            return false;
        }
        return BCrypt.checkpw(userInfo[1], UserDao.getUserByUsername(userInfo[0]).getPassword());
    }

    public static String[] tokenDecode(String token) {//Convert token into username & password
        String baseToken = token.substring("Basic".length() + 1);
        byte[] decode = Base64.getDecoder().decode(baseToken);
        return new String(decode, StandardCharsets.UTF_8).split(":");
    }
}