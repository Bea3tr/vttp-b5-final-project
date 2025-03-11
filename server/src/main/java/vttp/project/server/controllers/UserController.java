package vttp.project.server.controllers;

import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import vttp.project.server.models.UserInfo;
import vttp.project.server.services.UserService;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private static final Logger logger = Logger.getLogger(UserController.class.getName());

    @Autowired
    private UserService userSvc;

    @PostMapping(path = "/google-user", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> postGoogleUser(@RequestBody UserInfo ui) {
        if (ui.getEmail() == null) {
            logger.info("Incorrect payload: " + ui);
            return ResponseEntity.status(401)
                    .body(Json.createObjectBuilder()
                            .add("message", "authentication error")
                            .build().toString());
        }
        logger.info("Received user info: " + ui);
        String id = "";

        // Save user data in MySQL if not existing user
        if (!userSvc.userExists(ui.getEmail())) {
            id = UUID.randomUUID().toString().substring(0, 8);
            ui.setId(id);
            ui.setPassword(ui.getPassword());
            ui.setGoogleLogin(true);
            logger.info("[User Controller] User info with id: " + ui);
            boolean isInserted = userSvc.insertNewUser(ui);
            if (!isInserted) {
                return ResponseEntity.status(422)
                        .body(Json.createObjectBuilder()
                                .add("message", "user data could not be processed")
                                .build().toString());
            }
        } else {
            // Retrieve user id from database
            id = userSvc.getUserId(ui.getEmail());
        }
        // Send unique id to Angular
        return ResponseEntity.ok(Json.createObjectBuilder()
                .add("message", "authenticated")
                .add("id", id)
                .build().toString());
    }

    @PostMapping(path = "/user-login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> postUserLogin(@RequestBody UserInfo ui) {
        logger.info("Received user info: " + ui);
        String email = ui.getEmail();

        // Return error if user does not exist
        if (!userSvc.userExists(email)) {
            return ResponseEntity.status(404)
                    .body(Json.createObjectBuilder()
                            .add("message", "User does not exist")
                            .build().toString());

            // Incorrect password
        } else if (!ui.getPassword().equals(userSvc.getUserPassword(email))) {
            // For login with Google
            if (userSvc.isGoogleLogin(email)) {
                return ResponseEntity.status(401)
                        .body(Json.createObjectBuilder()
                                .add("message", "Login with Google")
                                .build().toString());
            } else {
                return ResponseEntity.status(401)
                        .body(Json.createObjectBuilder()
                                .add("message", "Incorrect password")
                                .build().toString());
            }
        }
        // Send unique id to Angular
        return ResponseEntity.ok(Json.createObjectBuilder()
                .add("message", "authenticated")
                .add("id", userSvc.getUserId(email))
                .build().toString());
    }

    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> postRegister(@RequestBody UserInfo ui) {
        logger.info("Received user info: " + ui);
        String email = ui.getEmail();

        // Return error if user exists
        if (userSvc.userExists(email)) {
            return ResponseEntity.status(403)
                    .body(Json.createObjectBuilder()
                            .add("message", "User exists")
                            .build().toString());
        }
        String id = UUID.randomUUID().toString().substring(0, 8);
        ui.setId(id);
        ui.setPicture(ui.getPicture());
        ui.setPassword(ui.getPassword());
        ui.setGoogleLogin(false);
        logger.info("[User Controller] User info with id: " + ui);
        boolean isInserted = userSvc.insertNewUser(ui);
        if (!isInserted) {
            return ResponseEntity.status(422)
                    .body(Json.createObjectBuilder()
                            .add("message", "user data could not be processed")
                            .build().toString());
        }
        // Send unique id to Angular
        return ResponseEntity.ok(Json.createObjectBuilder()
                .add("message", "authenticated")
                .add("id", userSvc.getUserId(email))
                .build().toString());
    }
}
