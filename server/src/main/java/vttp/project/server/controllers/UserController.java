package vttp.project.server.controllers;

import java.util.logging.Logger;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.json.Json;
import vttp.project.server.models.UserInfo;

@Controller
@RequestMapping(path="/api", produces=MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private static final Logger logger = Logger.getLogger(UserController.class.getName());
    
    @PostMapping(path="/user-info", consumes=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> postUserInfo(@RequestBody UserInfo ui) {
        if(ui.getEmail() == null) {
            logger.info("Incorrect payload: " + ui);
            return ResponseEntity.status(401)
                .body(Json.createObjectBuilder()
                    .add("message", "unauthenticated access")
                    .build().toString());
        }
        logger.info("Received user info: " + ui);

        return ResponseEntity.ok(Json.createObjectBuilder()
            .add("message", "access authenticated")
            .build().toString());
    }
}
