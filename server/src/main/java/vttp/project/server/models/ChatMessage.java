package vttp.project.server.models;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.json.JsonObject;

@Document(collection = "messages")
public class ChatMessage {

    @Id
    private String id;
    private String sender;
    private String content;
    private String chatRoom;
    private Date timestamp;

    public ChatMessage(String sender, String content, String chatRoom) {
        this.sender = sender;
        this.content = content;
        this.chatRoom = chatRoom;
        this.timestamp = new Date();
    }

    public static ChatMessage jsonToChat(JsonObject payload) {
      return new ChatMessage(
        payload.getString("sender"), 
        payload.getString("content"), 
        payload.getString("chatRoom"));
    }

    public String getId() {return id;}
    public void setId(String id) {this.id = id;}

    public String getSender() {return sender;}
    public void setSender(String sender) {this.sender = sender;}

    public String getContent() {return content;}
    public void setContent(String content) {this.content = content;}

    public String getChatRoom() {return chatRoom;}
    public void setChatRoom(String chatRoom) {this.chatRoom = chatRoom;}

    public Date getTimestamp() {return timestamp;}
    public void setTimestamp(Date timestamp) {this.timestamp = timestamp;}
    
}
