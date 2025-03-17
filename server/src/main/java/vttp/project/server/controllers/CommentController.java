package vttp.project.server.controllers;

import java.io.StringReader;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import vttp.project.server.services.CommentService;

@RestController
@RequestMapping(path="/api/comment", produces=MediaType.APPLICATION_JSON_VALUE)
public class CommentController {

    private static final Logger logger = Logger.getLogger(CommentController.class.getName());

    @Autowired
    private CommentService commentSvc;

    @PostMapping(path = "/post")
    public ResponseEntity<String> postComment(@RequestBody String payload) {
        logger.info("[API Ctrl] Payload: " + payload);
        JsonObject inObj = Json.createReader(new StringReader(payload))
                .readObject().getJsonObject("body");
        boolean posted = commentSvc.postComment(inObj.getString("userId"), 
            inObj.getString("postId"), inObj.getString("comment"));
        if (posted) {
            return ResponseEntity.ok(Json.createObjectBuilder()
                    .add("message", "Posted comment")
                    .build().toString());
        }
        return ResponseEntity.status(400)
                .body(Json.createObjectBuilder()
                        .add("message", "Error posting comment")
                        .build().toString());
    }

    @PutMapping(path = "/edit")
    public ResponseEntity<String> editComment(@RequestBody String payload) {
        logger.info("[API Ctrl] Payload: " + payload);
        JsonObject inObj = Json.createReader(new StringReader(payload))
                .readObject().getJsonObject("body");
        boolean edited = commentSvc.editComment(inObj.getString("cId"), 
            inObj.getString("edited"));
        if (edited) {
            return ResponseEntity.ok(Json.createObjectBuilder()
                    .add("message", "Edited comment")
                    .build().toString());
        }
        return ResponseEntity.status(400)
                .body(Json.createObjectBuilder()
                        .add("message", "Error editing comment")
                        .build().toString());
    }

    @DeleteMapping(path = "/delete/{cId}")
    public ResponseEntity<String> deleteComment(@PathVariable String cId) {
        JsonObject result = commentSvc.deleteComment(cId);
        return ResponseEntity.ok(result.toString());
    }

    @GetMapping(path = "/get")
    public ResponseEntity<String> getComments(@RequestParam String postId) {
        JsonArray comments = commentSvc.getCommentsByPost(postId);
        return ResponseEntity.ok(Json.createObjectBuilder()
            .add("message", "Retrieved comments for: " + postId)
            .add("comments", comments)
            .build().toString());
    }
    
}
