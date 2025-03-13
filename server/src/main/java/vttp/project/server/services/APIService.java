package vttp.project.server.services;

import java.io.StringReader;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import vttp.project.server.repositories.APIRepository;

import static vttp.project.server.models.Utils.*;

@Service
public class APIService {

    private static final String PF_URL = "https://api.petfinder.com/v2/oauth2/token";
    private static final String PF_GET_ANIMALS = "https://api.petfinder.com/v2/animals";
    private static final Logger logger = Logger.getLogger(APIService.class.getName());

    @Autowired
    private APIRepository apiRepo;

    @Value("${petfinder.id}")
    private String id;

    @Value("${petfinder.secret}")
    private String secret;

    public String getPfToken() {
        try {
            String token = "";
            if (apiRepo.pfTokenExists()) {
                logger.info("Retrieving token from db");
                token = apiRepo.getPfToken();

            } else {
                JsonObject body = Json.createObjectBuilder()
                        .add("grant_type", "client_credentials")
                        .add("client_id", id)
                        .add("client_secret", secret)
                        .build();

                RequestEntity<String> req = RequestEntity.post(PF_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body.toString(), String.class);

                RestTemplate template = new RestTemplate();
                ResponseEntity<String> resp = template.exchange(req, String.class);

                String payload = resp.getBody();
                logger.info("[APISvc] Petfinder resp: " + payload);
                JsonObject obj = Json.createReader(new StringReader(payload))
                        .readObject();

                token = obj.getString("access_token");

                apiRepo.savePfToken(token, obj.getInt("expires_in"));
            }
            return token;

        } catch (Exception ex) {
            logger.warning(ex.getMessage());
            return "";
        }
    }

    public JsonObject getPfAnimalsAll(String token) {
        try {
            RequestEntity<Void> req = RequestEntity.get(PF_GET_ANIMALS)
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON)
                        .build();

            RestTemplate template = new RestTemplate();
            ResponseEntity<String> resp = template.exchange(req, String.class);

            String payload = resp.getBody();
            return Json.createObjectBuilder()
                    .add("message", "success")
                    .add("results", filterResult(Json.createReader(new StringReader(payload))
                        .readObject()))
                    .build();

        } catch (Exception ex) {
            ex.printStackTrace();
            return Json.createObjectBuilder()
                .add("message", "Error in all animals")
                .build();
        }
    }

    public JsonObject getPfAnimals(String token, MultiValueMap<String, String> form) {
        JsonObject params = formToJson(form);
        try {
            String url = UriComponentsBuilder.fromUriString(PF_GET_ANIMALS)
                .queryParam("type", params.getString("type"))
                .queryParam("breed", params.getString("breed"))
                .queryParam("size", params.getString("size"))
                .queryParam("gender", params.getString("gender"))
                .queryParam("age", params.getString("age"))
                .queryParam("name", params.getString("name"))
                .queryParam("city", params.getString("city"))
                .toUriString();

            RequestEntity<Void> req = RequestEntity.get(url)
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON)
                        .build();

            RestTemplate template = new RestTemplate();
            ResponseEntity<String> resp = template.exchange(req, String.class);

            String payload = resp.getBody();
            
            return Json.createObjectBuilder()
                    .add("message", "success")
                    .add("results", filterResult(Json.createReader(new StringReader(payload))
                        .readObject()))
                    .build();

        } catch (Exception ex) {
            return Json.createObjectBuilder()
                .add("message", ex.getMessage())
                .build();
        }
    }

    private JsonObject formToJson(MultiValueMap<String, String> form) {
        JsonObjectBuilder paramObj = Json.createObjectBuilder();
        for(int i = 0; i < PF_PARAMS.length; i++) {
            String param = PF_PARAMS[i];
            if(form.getFirst(param) == null) {
                paramObj.add(param, "");
            } else {
                paramObj.add(param, form.getFirst(param));
            }
        }
        return paramObj.build();
    }

    private JsonArray filterResult(JsonObject fromApi) {
        JsonArrayBuilder filteredArr = Json.createArrayBuilder();
        JsonArray results = fromApi.getJsonArray("animals");

        logger.info("[API Svc] Result size: " + results.size());
        for(int i = 0; i < results.size(); i++) {
            JsonObjectBuilder filteredObj = Json.createObjectBuilder();
            JsonObject animal = results.getJsonObject(i);

            // Fill in String attributes
            for(int j = 0; j < PF_STRING_ATTRIBUTES.length; j++) {
                String key = PF_STRING_ATTRIBUTES[j];
                String resKey = PF_RETURN_STRING_ATTRIBUTES[j];
                try {
                    if(key.contains("\\.")) {
                        String[] keys = key.split("\\.");
                        filteredObj.add(resKey, animal.getJsonObject(keys[0]).getString(keys[1]));
                    } else {
                        // logger.info("Key: " + key);
                        filteredObj.add(resKey, animal.getString(key));
                    }
                } catch (NullPointerException ex) {
                    filteredObj.add(resKey, "");
                } catch (ClassCastException ex) {
                    // logger.info("[Class Ex] Key: " + key + ", " + animal.get(key));
                    filteredObj.add(resKey, "");
                }
            }
            // Fill in JsonObject and JsonArray attributes
            filteredObj.add("attributes", getTrueValues(animal.getJsonObject("attributes")));
            filteredObj.add("environment", getTrueValues(animal.getJsonObject("environment")));
            filteredObj.add("tags", animal.getJsonArray("tags"));
            filteredObj.add("photos", getPhotos(animal.getJsonArray("photos")));
            filteredObj.add("videos", animal.getJsonArray("videos"));
            filteredObj.add("address", 
                concatAddress(animal.getJsonObject("contact").getJsonObject("address")));
            
            filteredArr.add(filteredObj.build());
        }
        JsonArray filteredResults = filteredArr.build();
        apiRepo.savePfResults(filteredResults);
        return filteredResults;
    }

    private JsonArray getTrueValues(JsonObject attr) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for(String key : attr.keySet()) {
            try {
                if(attr.getBoolean(key) == true) {
                    builder.add(key);
                }
            } catch (NullPointerException ex) {
                // Skip null attribute
                continue;
            } catch (ClassCastException ex) {
                // logger.info("[Class Ex] Key: " + key + ", " + attr.get(key));
                continue;
            }
        }
        return builder.build();
    }

    private String concatAddress(JsonObject address) {
        String addr = "";
        for(String key : address.keySet()) {
            try {
                addr = addr + address.getString(key) + ", ";
            } catch (NullPointerException ex) {
                continue;
            } catch (ClassCastException ex) {
                // logger.info("[Class Ex] Key: " + key + ", " + address.get(key));
                continue;
            }
        }
        return addr.substring(0, addr.length()-2);
    }

    private JsonArray getPhotos(JsonArray photos) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for(int i = 0; i < photos.size() - 1; i++) {
            try {
                JsonObject photo = photos.getJsonObject(i);
                builder.add(photo.getString("full"));
            } catch (NullPointerException ex) {
                continue;
            } catch (ClassCastException ex) {
                continue;
            }
        }
        return builder.build();
    }
}
