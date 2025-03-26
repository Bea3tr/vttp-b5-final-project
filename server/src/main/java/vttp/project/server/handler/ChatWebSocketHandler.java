package vttp.project.server.handler;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

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
import vttp.project.server.services.ChatService;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger logger = Logger.getLogger(ChatWebSocketHandler.class.getName());

    @Autowired
    private ChatService chatSvc;

    @SuppressWarnings("null")
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        String userId = session.getUri().getQuery(); // Assume userId is passed in URL as query param
        activeSessions.put(userId, session);
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws IOException {
        String payload = message.getPayload();
        JsonObject result = Json.createReader(new StringReader(payload)).readObject();
        ChatMessage chatMessage = ChatMessage.jsonToChat(result);

        chatSvc.saveMessage(chatMessage); // Save message to MongoDB

        // Send message to the receiver if they are online
        WebSocketSession receiverSession = activeSessions.get(chatMessage.getReceiverId());
        if (receiverSession != null && receiverSession.isOpen()) {
            logger.info("[WS] Sending message to receiver");
            receiverSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
        }

         // Send message back to the sender as well
        WebSocketSession senderSession = activeSessions.get(chatMessage.getSenderId());
        if (senderSession != null && senderSession.isOpen()) {
            logger.info("[WS] Sending message to sender");
            senderSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        activeSessions.values().remove(session);
    }
}
