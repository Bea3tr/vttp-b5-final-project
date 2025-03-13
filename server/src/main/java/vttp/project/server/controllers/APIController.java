package vttp.project.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import vttp.project.server.services.APIService;

@RestController
@RequestMapping(path="/api", produces=MediaType.APPLICATION_JSON_VALUE)
public class APIController {

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
        return ResponseEntity.ok(result.toString());
    }
    
}
