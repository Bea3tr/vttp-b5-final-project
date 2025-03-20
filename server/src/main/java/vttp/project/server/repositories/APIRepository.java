package vttp.project.server.repositories;

import static vttp.project.server.models.Utils.*;

import java.time.Duration;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.mongodb.client.result.UpdateResult;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

@Repository
public class APIRepository {

    private static final Logger logger = Logger.getLogger(APIRepository.class.getName());

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

    // public void savePfResults(JsonArray results) {
    //     logger.info("[API Repo] Saving pf data to DB");
    //     List<Document> docsToSave = new LinkedList<>();
    //     for(int i = 0; i < results.size() - 1; i++) {
    //         JsonObject obj = results.getJsonObject(i);
    //         Document toSave = new Document("pf_id", obj.getInt("id"))
    //             .append("url", obj.getString("url"))
    //             .append("name", obj.getString("name"))
    //             .append("species", obj.getString("species"))
    //             .append("breed", obj.getString("breed"))
    //             .append("color", obj.getString("color"))
    //             .append("age", obj.getString("age"))
    //             .append("gender", obj.getString("gender"))
    //             .append("size", obj.getString("size"))
    //             .append("coat", obj.getString("coat"))
    //             .append("description", obj.getString("description"))
    //             .append("email", obj.getString("email"))
    //             .append("phone", obj.getString("phone"))
    //             .append("address", obj.getString("address"))
    //             .append("attributes", jsonArrToList(obj.getJsonArray("attributes")))
    //             .append("environment", jsonArrToList(obj.getJsonArray("environment")))
    //             .append("tags", jsonArrToList(obj.getJsonArray("tags")))
    //             .append("photos", jsonArrToList(obj.getJsonArray("photos")))
    //             .append("videos", jsonArrToList(obj.getJsonArray("videos")));

    //         docsToSave.add(toSave);
    //     }
    //     mgTemplate.insert(docsToSave, C_PF);
    //     logger.info("[API Repo] Inserted pf data in DB");
    //     // Refresh API results from endpoint every hour
    //     redisTemplate.opsForValue().set("load_pf", "true", Duration.ofHours(1));
    // }

    public void reloadsPfData(JsonArray results) {
        for(int i = 0; i < results.size() - 1; i++) {
            JsonObject obj = results.getJsonObject(i);
            Update updateOps = new Update()
                .set("url", obj.getString("url"))
                .set("name", obj.getString("name"))
                .set("species", obj.getString("species"))
                .set("breed", obj.getString("breed"))
                .set("color", obj.getString("color"))
                .set("age", obj.getString("age"))
                .set("gender", obj.getString("gender"))
                .set("size", obj.getString("size"))
                .set("coat", obj.getString("coat"))
                .set("description", obj.getString("description"))
                .set("email", obj.getString("email"))
                .set("phone", obj.getString("phone"))
                .set("address", obj.getString("address"))
                .set("attributes", jsonArrToList(obj.getJsonArray("attributes")))
                .set("environment", jsonArrToList(obj.getJsonArray("environment")))
                .set("tags", jsonArrToList(obj.getJsonArray("tags")))
                .set("photos", jsonArrToList(obj.getJsonArray("photos")))
                .set("videos", jsonArrToList(obj.getJsonArray("videos")));
            
            Query query = Query.query(Criteria.where(PFID).is(obj.getInt("id")));
            mgTemplate.upsert(query, updateOps, C_PF);
        }
        logger.info("[API Repo] Loaded / Reloaded pf data in DB");
        // Refresh API results from endpoint every hour
        redisTemplate.opsForValue().set("load_pf", "true", Duration.ofHours(1));
    }

    public JsonArray getAllPfResults() {
        Query query = new Query()
            .with(Sort.by(Sort.Direction.ASC, PFID))
            .limit(20);
        List<Document> results = mgTemplate.find(query, Document.class, C_PF);
        return docsToJsonArr(results);
    }

    public JsonArray loadMorePfResults(String[] loaded_ids) {
        Criteria criteria = Criteria.where(PFID).nin(loadedIds(loaded_ids));
        Query query = Query.query(criteria)
            .with(Sort.by(Sort.Direction.ASC, PFID))
            .limit(20);
        List<Document> results = mgTemplate.find(query, Document.class, C_PF);
        return docsToJsonArr(results);
    }

    public JsonArray loadMorePfResults(JsonObject params) {
        logger.info("[API Repo] Params: " + params);
        Collection<Criteria> criterias = new LinkedList<>();
        for(int j = 0; j < PF_PARAMS_FILTERED.length - 1; j++) {
            String p = PF_PARAMS_FILTERED[j];
            if (!params.getString(p).equals("")) {
                if(p.equals("type")) {
                    criterias.add(Criteria.where("species").regex(params.getString(p), "i"));
                } else if (p.equals("name")) {
                    criterias.add(Criteria.where(p).regex(params.getString(p), "i"));
                } else if (p.equals("location")) {
                    criterias.add(Criteria.where("address").regex(params.getString(p), "i"));
                } else {
                    logger.info("[API Repo] Multiple values: " + params.getString(p));
                    criterias.add(Criteria.where(p).in(loadedValues(params.getString(p).split(","))));
                }
            }
        }
        Criteria overallCriteria = Criteria.where(PFID)
            .nin(loadedIds(params.getString("pf_ids").split(",")))
            .orOperator(criterias);

        Query query = Query.query(overallCriteria)
            .with(Sort.by(Sort.Direction.ASC, PFID))
            .limit(20);
        List<Document> results = mgTemplate.find(query, Document.class, C_PF);
        logger.info("[API Repo] Results: " + results);
        return docsToJsonArr(results);
    }

    public boolean pfLoaded() {
        return mgTemplate.collectionExists(C_PF) && redisTemplate.hasKey("load_pf");
    }

    public boolean savePfToUser(String userId, int pfId) {
        Update updateOps = new Update()
            .push(F_SAVED_PF, pfId);
        Query query = Query.query(Criteria.where(USERID).is(userId));
        UpdateResult result = mgTemplate.upsert(query, updateOps, C_USER);
        return result.getModifiedCount() > 0;
    }

    public Document getSavedPf(String userId) {
        Criteria criteria = Criteria.where(USERID).is(userId);
        Query query = Query.query(criteria);
        return mgTemplate.findOne(query, Document.class, C_USER);
    }

    public boolean removeSavedPf(String userId, int pfId) {
        Query query = new Query(Criteria.where(USERID).is(userId));
        Update updateOps = new Update()
            .pull(F_SAVED_PF, pfId);
    
        UpdateResult result = mgTemplate.upsert(query, updateOps, C_USER);
        return result.getModifiedCount() > 0;
    }

    public void saveTypes(JsonArray types) {
        Collection<String> toInsert = jsonArrToList(types);
        redisTemplate.opsForList().rightPushAll(R_PF_TYPES, toInsert);
    }

    public JsonArray getTypes() {
        List<String> types = redisTemplate.opsForList().range(R_PF_TYPES, 0, -1);
        return listTojsonArr(types);
    }

    public boolean typesLoaded() {
        return redisTemplate.hasKey(R_PF_TYPES);
    }

    public void saveBreeds(String type, JsonArray breeds) {
        Collection<String> toInsert = jsonArrToList(breeds);
        redisTemplate.opsForList().rightPushAll(keyForBreeds(type), toInsert);
    }

    public JsonArray getBreeds(String type) {
        List<String> breeds = redisTemplate.opsForList().range(keyForBreeds(type), 0, -1);
        return listTojsonArr(breeds);
    }

    public boolean breedsLoaded(String type) {
        return redisTemplate.hasKey(keyForBreeds(type));
    }

    public JsonArray getDataByIds(List<Integer> ids) {
        Criteria criteria = Criteria.where(PFID)
            .in(ids);
        Query query = Query.query(criteria);
        List<Document> results = mgTemplate.find(query, Document.class, C_PF);
        return docsToJsonArr(results);
    }

    // @Async
    // public void dropPfCollection() {
    //     if(!redisTemplate.hasKey("load_pf") && mgTemplate.collectionExists(C_PF)) {
    //         mgTemplate.dropCollection(C_PF);
    //     }
    // }

    //==========PRIVATE METHODS========

    private List<String> jsonArrToList(JsonArray array) {
        List<String> values = new LinkedList<>();
        for(int i = 0; i < array.size(); i++) {
            values.add(array.getString(i));            
        }
        return values;
    }

    private JsonArray listTojsonArr(List<String> values) {
        JsonArrayBuilder arrBuild = Json.createArrayBuilder();
        for(String v : values) {
            arrBuild.add(v);
        }
        return arrBuild.build();
    }

    private JsonArray docsToJsonArr(List<Document> docs) {
        JsonArrayBuilder pfArr = Json.createArrayBuilder();
        for(Document doc : docs) {
            JsonObjectBuilder pfBuild = Json.createObjectBuilder();
            pfBuild.add(ID, doc.getInteger("pf_id"));
            for(int i = 1; i < PF_RETURN_STRING_ATTRIBUTES.length ; i++) {
                String key = PF_RETURN_STRING_ATTRIBUTES[i];
                pfBuild.add(key, doc.getString(key));
            }
            pfBuild.add("address", doc.getString("address"))
                .add("attributes", listTojsonArr(doc.getList("attributes", String.class)))
                .add("environment", listTojsonArr(doc.getList("environment", String.class)))
                .add("tags", listTojsonArr(doc.getList("tags", String.class)))
                .add("photos", listTojsonArr(doc.getList("photos", String.class)))
                .add("videos", listTojsonArr(doc.getList("videos", String.class)));
            
            pfArr.add(pfBuild.build());
        }
        return pfArr.build();
    }

    private String keyForBreeds(String type) {
        return R_PF_BREEDS + "_" + type;
    }

    private List<Integer> loadedIds(String[] ids) {
        List<Integer> loaded = new LinkedList<>();
        for(int i = 0; i < ids.length; i++) {
            loaded.add(Integer.parseInt(ids[i]));
        }
        return loaded;
    }

    private List<String> loadedValues(String[] values) {
        List<String> list = new LinkedList<>();
        for(int i = 0; i < values.length; i++) {
            list.add(values[i]);
        }
        return list;
    }
   
    
}
