package vttp.project.server.repositories;

import java.util.List;
import java.util.logging.Logger;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.client.result.UpdateResult;

import vttp.project.server.models.ChatMessage;
import static vttp.project.server.models.Utils.*;

@Repository
public class ChatRepository {

    private static final Logger logger = Logger.getLogger(ChatRepository.class.getName());
    @Autowired
    private MongoTemplate mgTemplate;

    public Document saveMessage(ChatMessage message) {
        Document toSave = new Document("_id", message.getId())
                .append(F_SENDER_ID, message.getSenderId())
                .append(F_RECEIVER_ID, message.getReceiverId())
                .append(F_CONTENT, message.getContent())
                .append(F_TYPE, message.getType())
                .append(TIMESTAMP, message.getTimestamp())
                .append(F_READ, message.isRead());
        Document inserted = mgTemplate.insert(toSave, C_CHAT);
        return inserted;
    }

    public List<Document> getMessages(String user1, String user2, String type) {
        Criteria criteria = Criteria.where(F_SENDER_ID).in(user1, user2)
                .orOperator(Criteria.where(F_RECEIVER_ID).in(user1, user2))
                .andOperator(Criteria.where(F_TYPE).is(type));

        Query query = Query.query(criteria)
                .with(Sort.by(Sort.Direction.ASC, TIMESTAMP));

        return mgTemplate.find(query, Document.class, C_CHAT);
    }

    public boolean saveChat(String userId, String partyId, String type) {
        Document toSave = new Document("partyId", partyId)
            .append("type", type);
        Update updateOps = new Update()
                .addToSet(F_CHATS, toSave);
        Query query = Query.query(Criteria.where(USERID).is(userId));
        UpdateResult result = mgTemplate.upsert(query, updateOps, C_USER);
        return result.getModifiedCount() > 0;
    }

    public boolean removeChat(String userId, String partyId, String type) {
        logger.info("[Chat Repo] Removing chat");
        Query query = new Query(Criteria.where(USERID).is(userId));
        Document toRemove = new Document("partyId", partyId)
            .append("type", type);
        Update updateOps = new Update()
                .pull(F_CHATS, toRemove);

        UpdateResult result = mgTemplate.upsert(query, updateOps, C_USER);
        return result.getModifiedCount() > 0;
    }

    public Document getChats(String userId) {
        Criteria criteria = Criteria.where(USERID).is(userId);
        Query query = Query.query(criteria);
        return mgTemplate.findOne(query, Document.class, C_USER);
    }

}
