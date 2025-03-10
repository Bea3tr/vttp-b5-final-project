package vttp.project.server.controllers;

import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.json.Json;
import vttp.project.server.models.UserInfo;
import vttp.project.server.services.UserService;

@Controller
@RequestMapping(path="/api", produces=MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private static final Logger logger = Logger.getLogger(UserController.class.getName());

    @Autowired
    private UserService userSvc;
    
    @PostMapping(path="/user-info", consumes=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> postUserInfo(@RequestBody UserInfo ui) {
        if(ui.getEmail() == null) {
            logger.info("Incorrect payload: " + ui);
            return ResponseEntity.status(401)
                .body(Json.createObjectBuilder()
                    .add("message", "authentication error")
                    .build().toString());
        }
        logger.info("Received user info: " + ui);
        String id = "";
        
        // Save user data in MySQL if not existing user
        if(!userSvc.userExists(ui.getEmail())) {
            id = UUID.randomUUID().toString().substring(0, 8);
            boolean isInserted = userSvc.insertNewUser(ui, id);
            if(!isInserted) {
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
}
