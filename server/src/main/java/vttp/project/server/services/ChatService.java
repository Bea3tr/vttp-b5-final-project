package vttp.project.server.services;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import vttp.project.server.models.ChatMessage;
import vttp.project.server.repositories.ChatRepository;
import static vttp.project.server.models.Utils.*;

@Service
public class ChatService {

  @Autowired
  private ChatRepository chatRepo;

  public Document saveMessage(ChatMessage message) {
    return chatRepo.saveMessage(message);
  }

  public JsonArray getMessages(String user1, String user2, String type) {
    List<Document> chatDocs = chatRepo.getMessages(user1, user2, type);
    return docsToJsonArray(chatDocs);
  }

  private JsonArray docsToJsonArray(List<Document> chatDocs) {
    JsonArrayBuilder builder = Json.createArrayBuilder();
    for(Document doc : chatDocs) {
      JsonObject msg = Json.createObjectBuilder()
        .add(ID, doc.getString("_id"))
        .add("senderId", doc.getString(F_SENDER_ID))
        .add("receiverId", doc.getString(F_RECEIVER_ID))
        .add(F_CONTENT, doc.getString(F_CONTENT))
        .add(TIMESTAMP, doc.getDate(TIMESTAMP).getTime())
        .add(F_READ, doc.getBoolean(F_READ))
        .build();
      builder.add(msg);
    }
    return builder.build();
  }

  public boolean saveChat(String userId, String partyId) {
    return chatRepo.saveChat(userId, partyId);
  }

  public boolean removeChat(String userId, String partyId) {
    return chatRepo.removeChat(userId, partyId);
  }

  public JsonArray getChats(String userId) {
    Document result = chatRepo.getChats(userId);
    JsonArrayBuilder chatArr = Json.createArrayBuilder();
    try {
        List<String> chats = result.getList(F_CHATS, String.class);
        if (!chats.isEmpty()) {
            for (String id : chats)
                chatArr.add(id);
        }
    } catch (Exception ex) {
        // ignore null return
    }
    return chatArr.build();
}
  
}
