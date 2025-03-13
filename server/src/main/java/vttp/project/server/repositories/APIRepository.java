package vttp.project.server.repositories;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

@Repository
public class APIRepository {

    @Autowired @Qualifier("myredis")
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private MongoTemplate mgTemplate;

    public boolean pfTokenExists() {
        return redisTemplate.hasKey("pf_access_token");
    }

    public String getPfToken() {
        return redisTemplate.opsForValue().get("pf_access_token");
    }

    public void savePfToken(String token, int expire) {
        redisTemplate.opsForValue().set("pf_access_token", token);
        // Remove from db 1 min before token expires
        redisTemplate.expire("pf_access_token", Duration.ofSeconds(expire - 60));
    }

    public void savePfResults(JsonArray results) {
        List<Document> docsToSave = new LinkedList<>();
        for(int i = 0; i < results.size() - 1; i++) {
            JsonObject obj = results.getJsonObject(i);
            Document toSave = new Document("url", obj.getString("url"))
                .append("name", obj.getString("name"))
                .append("species", obj.getString("species"))
                .append("breed", obj.getString("breed"))
                .append("color", obj.getString("color"))
                .append("age", obj.getString("age"))
                .append("gender", obj.getString("gender"))
                .append("size", obj.getString("size"))
                .append("coat", obj.getString("coat"))
                .append("description", obj.getString("description"))
                .append("email", obj.getString("email"))
                .append("phone", obj.getString("phone"))
                .append("address", obj.getString("address"))
                .append("attributes", jsonArrToList(obj.getJsonArray("attributes")))
                .append("environment", jsonArrToList(obj.getJsonArray("environment")))
                .append("tags", jsonArrToList(obj.getJsonArray("tags")))
                .append("photos", jsonArrToList(obj.getJsonArray("photos")))
                .append("videos", jsonArrToList(obj.getJsonArray("videos")));

            docsToSave.add(toSave);
        }
        mgTemplate.insert(docsToSave, "pf_results");
    }

    private List<String> jsonArrToList(JsonArray array) {
        List<String> values = new LinkedList<>();
        for(int i = 0; i < array.size(); i++) {
            values.add(array.getString(i));
        }
        return values;
    }
    
}
