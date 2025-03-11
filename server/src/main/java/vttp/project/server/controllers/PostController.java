package vttp.project.server.controllers;

import java.io.IOException;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import vttp.project.server.services.PostService;

@RestController
@RequestMapping(path="/api", produces=MediaType.APPLICATION_JSON_VALUE)
public class PostController {

    private static final Logger logger = Logger.getLogger(PostController.class.getName());
    @Autowired
    private PostService postSvc;
    
    @PostMapping(path="/post/{id}", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadPost(
        @PathVariable String id,
        @RequestPart("file") MultipartFile file,
        @RequestPart("post") String post
    ) {
        String postId = "";
        try {
            postId = postSvc.upload(file, post, id);
            logger.info("[Post Controller] " + postId);
                
        } catch (DataAccessException | IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Json.createObjectBuilder()
                    .add("message", ex.getMessage())
                    .build().toString());
        }            
        JsonObject payload = Json.createObjectBuilder()
            .add("message", "posted")
            .add("postId", postId)
            .build();

        return ResponseEntity.ok(payload.toString());
    }
}
