package vttp.project.server.controllers;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
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
    ui.setPassword(ui.getPassword());
    ui.setGoogleLogin(false);
    logger.info("[User Controller] User info with id: " + ui);
    boolean isInserted = userSvc.insertNewUser(ui);
    if (!isInserted) {
      return ResponseEntity.status(422)
          .body(Json.createObjectBuilder()
              .add("message", "User data could not be processed")
              .build().toString());
    }
    // Send unique id to Angular
    return ResponseEntity.ok(Json.createObjectBuilder()
        .add("message", "authenticated")
        .add("id", userSvc.getUserId(email))
        .build().toString());
  }

  @GetMapping(path = "/get-user/{id}")
  public ResponseEntity<String> getUserInfo(@PathVariable String id) {
    Optional<UserInfo> result = userSvc.getUserInfo(id);
    UserInfo ui = result.get();
    JsonObject payload = UserInfo.toJson(ui);

    return ResponseEntity.ok(payload.toString());
  }

  @GetMapping(path = "/users/{id}")
  public ResponseEntity<String> getAllUsers(@PathVariable String id) {
    Optional<List<UserInfo>> result = userSvc.getAllUsers(id);
    JsonArrayBuilder builder = Json.createArrayBuilder();
    if(result.isPresent()) {
      for(UserInfo ui : result.get()) {
        builder.add(UserInfo.toJson(ui));
      }
    }
    return ResponseEntity.ok(builder.build().toString());
  }

  @PutMapping(path = "/user/edit-pic/{id}")
  public ResponseEntity<String> editProfilePic(
      @PathVariable String id,
      @RequestPart("file") MultipartFile file) {

    try {
      userSvc.updatePic(file, id);
      return ResponseEntity.ok(Json.createObjectBuilder()
          .add("message", "Profile picture updated successfully")
          .build().toString());

    } catch (RuntimeException | IOException ex) {
      return ResponseEntity.status(400)
          .body((Json.createObjectBuilder()
              .add("message", "Error updating profile picture")
              .build().toString()));
    }
  }

  @PutMapping(path = "/user/edit-name/{id}")
  public ResponseEntity<String> editUsername(
      @PathVariable String id,
      @RequestBody String payload) {
    logger.info("[User Ctrl] Payload: " + payload);
    JsonObject inObj = Json.createReader(new StringReader(payload))
        .readObject().getJsonObject("body");
    try {
      userSvc.updateName(inObj.getString("name"), id);
      return ResponseEntity.ok(Json.createObjectBuilder()
          .add("message", "Username updated successfully")
          .build().toString());

    } catch (RuntimeException ex) {
      return ResponseEntity.status(400)
          .body(Json.createObjectBuilder()
              .add("message", "Error updating username")
              .build().toString());
    }
  }

  @PutMapping(path = "/user/edit-password/{id}")
  public ResponseEntity<String> editUserPassword(
      @PathVariable String id,
      @RequestBody String payload) {

    logger.info("[User Ctrl] Payload: " + payload);
    JsonObject inObj = Json.createReader(new StringReader(payload))
        .readObject().getJsonObject("body");

    String oldPassword = inObj.getString("oldPassword");
    String newPassword = inObj.getString("newPassword");
    logger.info("Old: %s, New: %s".formatted(oldPassword, newPassword));
    String oldPw = userSvc.getUserPasswordById(id);
    try {
      if (oldPw == null && oldPassword.equals("")) {
        userSvc.updatePassword(newPassword, id);
        return ResponseEntity.ok(Json.createObjectBuilder()
            .add("message", "Password updated successfully")
            .build().toString());
      }
      if (oldPassword.equals(oldPw)) {
        userSvc.updatePassword(newPassword, id);
        return ResponseEntity.ok(Json.createObjectBuilder()
            .add("message", "Password updated successfully")
            .build().toString());
      }
      return ResponseEntity.status(400)
          .body(Json.createObjectBuilder()
              .add("message", "Error updating password")
              .build().toString());

    } catch (RuntimeException ex) {
      return ResponseEntity.status(400)
          .body(Json.createObjectBuilder()
              .add("message", "Error updating password")
              .build().toString());
    }
  }

  @PutMapping(path = "/auth/reset-password", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> resetPassword(@RequestBody String payload) {
    try {
      boolean reset = userSvc.resetPassword(payload);
      if(reset) {
        return ResponseEntity.ok(Json.createObjectBuilder()
            .add("message", "Password reset successful!")
            .build().toString());
      }
      return ResponseEntity.status(400)
          .body(Json.createObjectBuilder()
              .add("message", "Failed to reset password")
              .build().toString());
              
    } catch (Exception ex) {
      return ResponseEntity.status(400)
          .body(Json.createObjectBuilder()
              .add("message", ex.getMessage())
              .build().toString());
    }
  }
}
