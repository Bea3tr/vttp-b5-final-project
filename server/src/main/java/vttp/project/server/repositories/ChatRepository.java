package vttp.project.server.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import vttp.project.server.models.ChatMessage;

@Repository
public class ChatRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void saveMessage(ChatMessage message) {
        mongoTemplate.insert(message, "chats");
    }

    public List<ChatMessage> getMessages(String chatRoom) {
        Query query = new Query(Criteria.where("chatRoom").is(chatRoom));
        return mongoTemplate.find(query, ChatMessage.class, "chats");
    }

    public void markAsRead(String messageId, String user) {
        Query query = new Query(Criteria.where("id").is(messageId));
        Update update = new Update().addToSet("readBy", user);
        mongoTemplate.updateFirst(query, update, ChatMessage.class, "chats");
    }

}
