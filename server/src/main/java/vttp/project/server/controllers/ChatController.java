package vttp.project.server.controllers;

import java.io.StringReader;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import vttp.project.server.services.ChatService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

  private static final Logger logger = Logger.getLogger(ChatController.class.getName());

    @Autowired
    private ChatService chatSvc;

    @GetMapping("/history/{user1}/{user2}")
    public ResponseEntity<String> getChatHistory(
      @PathVariable String user1, @PathVariable String user2,
      @RequestParam String type) {
        logger.info("[Chat Ctrl] Retrieving chat history");
        JsonArray result = chatSvc.getMessages(user1, user2, type);
        return ResponseEntity.ok(result.toString());
    }

    @PutMapping("/save/{userId}")
    public ResponseEntity<String> saveChat(@PathVariable String userId, @RequestBody String payload) {
      logger.info("[Chat Ctrl] Saving chat to " + userId);
      String partyId = Json.createReader(new StringReader(payload))
        .readObject().getJsonObject("body").getString("partyId");
      boolean saved = chatSvc.saveChat(userId, partyId);
      if(saved) {
        return ResponseEntity.ok(Json.createObjectBuilder()
          .add("message", "Chat saved")
          .build().toString());
      }
      return ResponseEntity.status(400)
        .body(Json.createObjectBuilder()
          .add("message", "Error saving chat")
          .build().toString());
    }

    @DeleteMapping("/remove/{userId}")
    public ResponseEntity<String> removeChat(@PathVariable String userId, @RequestBody String payload) {
      logger.info("[Chat Ctrl] Removing chat from " + userId);
      String partyId = Json.createReader(new StringReader(payload))
        .readObject().getJsonObject("body").getString("partyId");
      boolean removed = chatSvc.removeChat(userId, partyId);
      if(removed) {
        return ResponseEntity.ok(Json.createObjectBuilder()
          .add("message", "Chat removed")
          .build().toString());
      }
      return ResponseEntity.status(400)
        .body(Json.createObjectBuilder()
          .add("message", "Error removing chat")
          .build().toString());
    }

    @GetMapping("/get/{userId}")
    public ResponseEntity<String> getChats(@PathVariable String userId) {
      logger.info("[Chat Ctrl] Getting chats from " + userId);
      JsonArray chats = chatSvc.getChats(userId);
      return ResponseEntity.ok(chats.toString());
    }
}
