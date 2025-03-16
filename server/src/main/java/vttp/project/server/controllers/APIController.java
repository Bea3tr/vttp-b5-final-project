package vttp.project.server.controllers;

import java.io.StringReader;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import vttp.project.server.services.APIService;

@RestController
@RequestMapping(path="/api", produces=MediaType.APPLICATION_JSON_VALUE)
public class APIController {

    private static final Logger logger = Logger.getLogger(APIController.class.getName());
    @Autowired
    private APIService apiSvc;

    @GetMapping(path="/shelter-filtered", consumes=MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> getShelterData(@RequestParam MultiValueMap<String, String> form) {
        String token = apiSvc.getPfToken();
        if(token.equals("")) {
            JsonObject result = Json.createObjectBuilder()
                .add("message", "error retrieving access token")
                .build();
            return ResponseEntity.status(401)
                .body(result.toString());
        }
        JsonObject result = apiSvc.getPfAnimals(token, form);
        return ResponseEntity.ok(result.toString());
    }

    @GetMapping(path="/shelter")
    public ResponseEntity<String> getShelterDataDefault() {
        String token = apiSvc.getPfToken();
        if(token.equals("")) {
            JsonObject result = Json.createObjectBuilder()
                .add("message", "error retrieving access token")
                .build();
            return ResponseEntity.status(401)
                .body(result.toString());
        }
        JsonObject result = apiSvc.getPfAnimalsAll(token);
        // System.out.println("Results: " + result);
        return ResponseEntity.ok(result.toString());
    }

    @PutMapping(path="/save-pf")
    public ResponseEntity<String> savePf(@RequestBody String payload) {
        logger.info("[API Ctrl] Payload: " + payload);
        JsonObject inObj = Json.createReader(new StringReader(payload))
            .readObject().getJsonObject("body");
        boolean saved = apiSvc.savePfToUser(inObj.getString("userId"), inObj.getInt("pfId"));
        if(saved) {
            return ResponseEntity.ok(Json.createObjectBuilder()
                .add("message", "Saved data to user")
                .build().toString());
        }
        return ResponseEntity.status(400)
                .body(Json.createObjectBuilder()
                    .add("message", "Error saving data to user")
                    .build().toString());
    }

    @PutMapping(path="/remove-pf")
    public ResponseEntity<String> removeSavedPf(@RequestBody String payload) {
        logger.info("[API Ctrl] Payload: " + payload);
        JsonObject inObj = Json.createReader(new StringReader(payload))
            .readObject().getJsonObject("body");
        boolean removed = apiSvc.removeSavedPf(inObj.getString("userId"), inObj.getInt("pfId"));
        if(removed) {
            return ResponseEntity.ok(Json.createObjectBuilder()
                .add("message", "Removed data from user")
                .build().toString());
        }
        return ResponseEntity.status(400)
                .body(Json.createObjectBuilder()
                    .add("message", "Error removing data from user")
                    .build().toString());
    }

    @GetMapping(path="/get-saved-pf/{userId}")
    public ResponseEntity<String> getSavedPf(@PathVariable String userId) {
        JsonArray result = apiSvc.getSavedPf(userId);
        if(result.isEmpty()) {
            return ResponseEntity.ok(Json.createObjectBuilder()
                .add("message", "No saved data")
                .add("result", result)
                .build().toString());
        }
        return ResponseEntity.ok(Json.createObjectBuilder()
            .add("message", "Data retrieved")
            .add("result", result)
            .build().toString());
    }
}
