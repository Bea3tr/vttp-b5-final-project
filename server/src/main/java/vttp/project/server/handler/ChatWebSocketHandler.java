package vttp.project.server.handler;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import vttp.project.server.models.ChatMessage;
import vttp.project.server.repositories.ChatRepository;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ChatRepository chatRepo;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws IOException {
        System.out.println(message.getPayload());
        String payload = message.getPayload();
        JsonObject result = Json.createReader(new StringReader(payload))
            .readObject();
        // ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
    
        if (result.getString("content").equals("READ_RECEIPT")) {
            chatRepo.markAsRead(result.getString("id"), result.getString("sender"));
        } else {
            ChatMessage chatMessage = ChatMessage.jsonToChat(result);
            chatRepo.saveMessage(chatMessage);
            for (WebSocketSession s : sessions) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        sessions.remove(session);
    }
}
