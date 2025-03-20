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
@RequestMapping(path="/api/shelter", produces=MediaType.APPLICATION_JSON_VALUE)
public class APIController {

    private static final Logger logger = Logger.getLogger(APIController.class.getName());
    @Autowired
    private APIService apiSvc;

    @GetMapping(path="/filtered")
    public ResponseEntity<String> getShelterData(@RequestParam MultiValueMap<String, String> form) {
        logger.info("Form: " + form);
        String token = apiSvc.getPfToken();
        if(token.equals("")) {
            JsonObject result = Json.createObjectBuilder()
                .add("message", "error retrieving access token")
                .build();
            return ResponseEntity.status(401)
                .body(result.toString());
        }
        JsonObject result = apiSvc.getPfAnimals(token, form);
        if(!result.getString("message").equals("success")) {
            return ResponseEntity.status(400)
                .body(result.toString());
        }
        return ResponseEntity.ok(result.toString());
    }

    @GetMapping(path="/load") 
    public ResponseEntity<String> getMoreData(@RequestParam String loaded) {
        logger.info("[API Ctrl] Loaded: " + loaded);
        JsonObject result = apiSvc.loadMorePf(loaded);
        if(!result.getString("message").equals("success")) {
            return ResponseEntity.status(400)
                .body(result.toString());
        }
        return ResponseEntity.ok(result.toString());
    }
    
    @GetMapping(path="/load-filtered")
    public ResponseEntity<String> getMoreDataFiltered(
        @RequestParam MultiValueMap<String, String> form) {

        logger.info("[API Ctrl] Form: %s".formatted(form));
        JsonObject result = apiSvc.loadMorePf(form);
        if(!result.getString("message").equals("success")) {
            return ResponseEntity.status(400)
                .body(result.toString());
        }
        return ResponseEntity.ok(result.toString());
    }

    @GetMapping()
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
        return ResponseEntity.ok(result.toString());
    }

    @PutMapping(path="/save")
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

    @PutMapping(path="/remove")
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

    @GetMapping(path="/get-saved/{userId}")
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

    @GetMapping(path="/types") 
    public ResponseEntity<String> getTypes() {
        logger.info("[API Ctrl] Retrieving types");
        String token = apiSvc.getPfToken();
        JsonObject result = apiSvc.getTypes(token);
        if(!result.getString("message").equals("success")) {
            return ResponseEntity.status(400)
                .body(result.toString());
        }
        return ResponseEntity.ok(result.toString());
    }

    @GetMapping(path="/breeds/{type}")
    public ResponseEntity<String> getBreeds(@PathVariable String type) {
        String token = apiSvc.getPfToken();
        JsonObject result = apiSvc.getBreeds(token, type);
        if(!result.getString("message").equals("success")) {
            return ResponseEntity.status(400)
                .body(result.toString());
        }
        return ResponseEntity.ok(result.toString());
    }

    @GetMapping("/get-data-saved/{userId}")
    public ResponseEntity<String> getDataSaved(@PathVariable String userId) {
        JsonObject result = apiSvc.getDataByUserId(userId);
        return ResponseEntity.ok(result.toString());
    }

}
