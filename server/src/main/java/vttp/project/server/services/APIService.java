package vttp.project.server.services;

import java.io.StringReader;
import java.util.List;
import java.util.logging.Logger;

import org.bson.Document;
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
    private static final String PF_GET_TYPES = "https://api.petfinder.com/v2/types";
    private static final String PF_GET_BREEDS = "https://api.petfinder.com/v2/types/{type}/breeds";
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
                logger.info("Retrieving token from api");
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
                // logger.info("[APISvc] Petfinder resp: " + payload);
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
            JsonArray results = Json.createArrayBuilder().build();
            if (apiRepo.pfLoaded()) {
                logger.info("[API Svc] Retrieving data from DB");
                results = apiRepo.getAllPfResults();
            } else {
                logger.info("[API Svc] Retrieving data from API");
                // Set to max limit
                String url = UriComponentsBuilder.fromUriString(PF_GET_ANIMALS)
                        .queryParam("limit", 100)
                        .toUriString();
                RequestEntity<Void> req = RequestEntity.get(url)
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON)
                        .build();

                RestTemplate template = new RestTemplate();
                ResponseEntity<String> resp = template.exchange(req, String.class);

                String payload = resp.getBody();
                results = filterResult(Json.createReader(new StringReader(payload))
                        .readObject());
            }
            return Json.createObjectBuilder()
                    .add("message", "success")
                    .add("results", results)
                    .build();

        } catch (Exception ex) {
            ex.printStackTrace();
            return Json.createObjectBuilder()
                    .add("message", "[ALL] Error retrieving animal data")
                    .build();
        }
    }

    public JsonObject getPfAnimals(String token, MultiValueMap<String, String> form) {
        try {
            String url = formToUrlParams(form);
            logger.info("[API Svc] Url: " + url);

            RequestEntity<Void> req = RequestEntity.get(url)
                    .header("Authorization", "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .build();

            RestTemplate template = new RestTemplate();
            ResponseEntity<String> resp = template.exchange(req, String.class);

            String payload = resp.getBody();
            JsonArray results = filterResult(Json.createReader(new StringReader(payload))
                    .readObject());

            return Json.createObjectBuilder()
                    .add("message", "success")
                    .add("results", results)
                    .build();

        } catch (Exception ex) {
            ex.printStackTrace();
            return Json.createObjectBuilder()
                    .add("message", ex.getMessage())
                    .build();
        }
    }

    public JsonObject loadMorePf(String loaded) {
        try {
            String[] loaded_ids = loaded.split(",");
            JsonArray results = apiRepo.loadMorePfResults(loaded_ids);
            return Json.createObjectBuilder()
                    .add("message", "success")
                    .add("results", results)
                    .build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Json.createObjectBuilder()
                    .add("message", ex.getMessage())
                    .build();
        }
    }

    public JsonObject loadMorePf(MultiValueMap<String, String> form) {
        try {
            JsonObject params = formToJson(form);
            JsonArray results = apiRepo.loadMorePfResults(params);
            return Json.createObjectBuilder()
                    .add("message", "success")
                    .add("results", results)
                    .build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Json.createObjectBuilder()
                    .add("message", ex.getMessage())
                    .build();
        }
    }

    public JsonObject getTypes(String token) {
        try {
            JsonArray results = Json.createArrayBuilder().build();
            if (apiRepo.typesLoaded()) {
                logger.info("[API Svc] Retrieving types from DB");
                results = apiRepo.getTypes();
            } else {
                logger.info("[API Svc] Retrieving data from API");
                RequestEntity<Void> req = RequestEntity.get(PF_GET_TYPES)
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON)
                        .build();

                RestTemplate template = new RestTemplate();
                ResponseEntity<String> resp = template.exchange(req, String.class);

                String payload = resp.getBody();
                JsonArray types = Json.createReader(new StringReader(payload))
                        .readObject().getJsonArray("types");

                JsonArrayBuilder builder = Json.createArrayBuilder();
                for (int i = 0; i < types.size(); i++) {
                    JsonObject details = types.getJsonObject(i);
                    builder.add(details.getString("name"));
                }
                results = builder.build();
                logger.info("[API Svc] Saving types to db");
                apiRepo.saveTypes(results);
            }
            return Json.createObjectBuilder()
                    .add("message", "success")
                    .add("results", results)
                    .build();

        } catch (Exception ex) {
            ex.printStackTrace();
            return Json.createObjectBuilder()
                    .add("message", "[TYPE] Error retrieving animal data")
                    .build();
        }
    }

    public JsonObject getBreeds(String token, String type) {
        try {
            JsonArray results = Json.createArrayBuilder().build();
            if (apiRepo.breedsLoaded(type)) {
                logger.info("[API Svc] Retrieving " + type + " breeds from DB");
                results = apiRepo.getBreeds(type);
            } else {
                logger.info("[API Svc] Retrieving data from API");
                RequestEntity<Void> req = RequestEntity.get(PF_GET_BREEDS.replace("{type}", type))
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON)
                        .build();

                RestTemplate template = new RestTemplate();
                ResponseEntity<String> resp = template.exchange(req, String.class);

                String payload = resp.getBody();
                JsonArray breeds = Json.createReader(new StringReader(payload))
                        .readObject().getJsonArray("breeds");

                JsonArrayBuilder builder = Json.createArrayBuilder();
                for (int i = 0; i < breeds.size(); i++) {
                    JsonObject details = breeds.getJsonObject(i);
                    builder.add(details.getString("name"));
                }
                results = builder.build();
                logger.info("[API Svc] Saving " + type + " breeds to db");
                apiRepo.saveBreeds(type, results);
            }
            return Json.createObjectBuilder()
                    .add("message", "success")
                    .add("results", results)
                    .build();

        } catch (Exception ex) {
            ex.printStackTrace();
            return Json.createObjectBuilder()
                    .add("message", "[TYPE] Error retrieving animal data")
                    .build();
        }
    }

    public boolean savePfToUser(String userId, int pfId) {
        return apiRepo.savePfToUser(userId, pfId);
    }

    public boolean removeSavedPf(String userId, int pfId) {
        return apiRepo.removeSavedPf(userId, pfId);
    }

    public JsonArray getSavedPf(String userId) {
        Document result = apiRepo.getSavedPf(userId);
        List<Integer> pfIds = result.getList(F_SAVED_PF, Integer.class);
        JsonArrayBuilder pfArr = Json.createArrayBuilder();
        if (pfIds != null) {
            for (int id : pfIds)
                pfArr.add(id);
        }
        return pfArr.build();
    }

    public JsonObject getDataByUserId(String userId) {
        Document result = apiRepo.getSavedPf(userId);
        List<Integer> pfIds = result.getList(F_SAVED_PF, Integer.class);
        logger.info("pfIds: " + pfIds);
        JsonArray results = Json.createArrayBuilder().build();
        if(pfIds != null) {
            results = apiRepo.getDataByIds(pfIds);
        }
        return Json.createObjectBuilder()
                .add("message", "success")
                .add("results", results)
                .build();
    }

    // ==========PRIVATE METHODS========

    private JsonObject formToJson(MultiValueMap<String, String> form) {
        JsonObjectBuilder paramObj = Json.createObjectBuilder();
        for (int i = 0; i < PF_PARAMS_FILTERED.length; i++) {
            String param = PF_PARAMS_FILTERED[i];
            String value = form.getFirst(param);
            if (value != null) {
                paramObj.add(param, value);
            } else {
                paramObj.add(param, "");
            }
        }
        return paramObj.build();
    }

    private String formToUrlParams(MultiValueMap<String, String> form) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(PF_GET_ANIMALS);
        for (int i = 0; i < PF_PARAMS.length; i++) {
            String param = PF_PARAMS[i];
            String value = form.getFirst(param);
            if (value != null) {
                if (value.contains("&")) {
                    logger.info("Form: " + value);
                    builder.queryParam(param, value.replaceAll(" & ", "-")
                            .replaceAll(", ", "-"));
                } else {
                    builder.queryParam(param, value);
                }
            }
        }
        String url = builder.toUriString();
        logger.info("[API Svc] Url Before: " + url);
        return url.replaceAll("%20", " ")
                .replaceAll("%26", "&");
    }

    private JsonArray filterResult(JsonObject fromApi) {
        JsonArrayBuilder filteredArr = Json.createArrayBuilder();
        JsonArray results = fromApi.getJsonArray("animals");

        logger.info("[API Svc] Result size: " + results.size());
        for (int i = 0; i < results.size(); i++) {
            JsonObjectBuilder filteredObj = Json.createObjectBuilder();
            JsonObject animal = results.getJsonObject(i);

            // Fill in String attributes
            for (int j = 0; j < PF_STRING_ATTRIBUTES.length; j++) {
                String key = PF_STRING_ATTRIBUTES[j];
                String resKey = PF_RETURN_STRING_ATTRIBUTES[j];
                try {
                    if (key.contains(".")) {
                        String[] keys = key.split("\\.");
                        filteredObj.add(resKey, animal.getJsonObject(keys[0]).getString(keys[1]));

                    } else if (key.equals("id")) {
                        filteredObj.add(resKey, animal.getInt(key));

                    } else if (key.equals("description")) {
                        filteredObj.add(resKey, animal.getString(key)
                                .replaceAll("&amp;#39;", "'")
                                .replaceAll("&#039;", "'")
                                .replaceAll("\n", " "));
                    } else {
                        // logger.info("Key: " + key);
                        filteredObj.add(resKey, animal.getString(key));
                    }
                } catch (NullPointerException ex) {
                    logger.info("[Null] Key: " + key + ", " + animal.get(key));
                    filteredObj.add(resKey, "N.A.");
                } catch (ClassCastException ex) {
                    logger.info("[Class Ex] Key: " + key + ", " + animal.get(key));
                    filteredObj.add(resKey, "N.A.");
                }
            }
            // Fill in JsonObject and JsonArray attributes
            filteredObj.add("attributes", getTrueValues(animal.getJsonObject("attributes")));
            filteredObj.add("environment", getTrueValues(animal.getJsonObject("environment")));
            filteredObj.add("tags", animal.getJsonArray("tags"));
            filteredObj.add("photos", getPhotos(animal.getJsonArray("photos")));
            filteredObj.add("videos", getVideos(animal.getJsonArray("videos")));
            filteredObj.add("address",
                    concatAddress(animal.getJsonObject("contact").getJsonObject("address")));

            filteredArr.add(filteredObj.build());
        }
        JsonArray filteredResults = filteredArr.build();
        apiRepo.reloadsPfData(filteredResults);
        return filteredResults;
    }

    private JsonArray getTrueValues(JsonObject attr) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (String key : attr.keySet()) {
            try {
                if (attr.getBoolean(key) == true) {
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
        for (String key : address.keySet()) {
            try {
                addr = addr + address.getString(key) + ", ";
            } catch (NullPointerException ex) {
                continue;
            } catch (ClassCastException ex) {
                // logger.info("[Class Ex] Key: " + key + ", " + address.get(key));
                continue;
            }
        }
        return addr.substring(0, addr.length() - 2);
    }

    private JsonArray getPhotos(JsonArray photos) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (int i = 0; i < photos.size() - 1; i++) {
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

    private JsonArray getVideos(JsonArray videos) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (int i = 0; i < videos.size(); i++) {
            JsonObject vidObj = videos.getJsonObject(i);
            String fullSrc = vidObj.getString("embed");
            // logger.info("[API Svc] Vid (full): " + fullSrc);
            int startIdx = fullSrc.indexOf("src=") + 5;
            int endIdx = fullSrc.indexOf("\"", startIdx);
            String src = fullSrc.substring(startIdx, endIdx);
            // logger.info("[API Svc] Vid: " + src);
            builder.add(src);
        }
        return builder.build();
    }
}
