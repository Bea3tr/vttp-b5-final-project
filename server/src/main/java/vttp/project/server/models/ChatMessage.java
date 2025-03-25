package vttp.project.server.models;

import java.util.Date;
import java.util.UUID;

import jakarta.json.JsonObject;

public class ChatMessage {

    private String id;
    private String senderId;
    private String receiverId;
    private String content;
    private String type;
    private Date timestamp;
    private boolean read;
    
    public String getId() {return id;}
    public void setId(String id) {this.id = id;}
    
    public String getSenderId() {return senderId;}
    public void setSenderId(String senderId) {this.senderId = senderId;}
    
    public String getReceiverId() {return receiverId;}
    public void setReceiverId(String receiverId) {this.receiverId = receiverId;}
    
    public String getContent() {return content;}
    public void setContent(String content) {this.content = content;}
    
    public String getType() {return type;}
    public void setType(String type) {this.type = type;}
    
    public Date getTimestamp() {return timestamp;}
    public void setTimestamp(Date timestamp) {this.timestamp = timestamp;}
    
    public boolean isRead() {return read;}
    public void setRead(boolean read) {this.read = read;}

    public ChatMessage(String senderId, String receiverId, String content, String type) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.type = type;
        this.timestamp = new Date();
        this.read = false;
    }

    public static ChatMessage jsonToChat(JsonObject payload) {
      return new ChatMessage(
        payload.getString("senderId"), 
        payload.getString("receiverId"),
        payload.getString("content"), 
        payload.getString("type"));
    }


}
