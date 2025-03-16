package vttp.project.server.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import vttp.project.server.models.Post;
import vttp.project.server.models.UserInfo;
import vttp.project.server.services.PostService;
import vttp.project.server.services.UserService;

@RestController
@RequestMapping(path="/api", produces=MediaType.APPLICATION_JSON_VALUE)
public class PostController {

    private static final Logger logger = Logger.getLogger(PostController.class.getName());
    
    @Autowired
    private PostService postSvc;

    @Autowired
    private UserService userSvc;
    
    @PostMapping(path="/post/{id}", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadPost(
        @PathVariable String id,
        @RequestParam(value="files", required=false) List<MultipartFile> files,
        @RequestPart(value="post", required=false) String post,
        @RequestPart("status") String status
    ) {
        String postId = "";
        try {
            UserInfo ui = userSvc.getUserInfo(id).get();
            postId = postSvc.upload(files, post, status, ui);
            logger.info("[Post Controller] Post id: " + postId);
                
        } catch (IOException | RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Json.createObjectBuilder()
                    .add("message", ex.getMessage())
                    .build().toString());
        }            
        JsonObject payload = Json.createObjectBuilder()
            .add("message", "Upload successful")
            .add("postId", postId)
            .add("success", true)
            .build();

        return ResponseEntity.ok(payload.toString());
    }

    @GetMapping(path="/public-posts")
    public ResponseEntity<String> getPosts() {
        try {
            logger.info("[Post Controller] Retrieving posts...");
            Optional<List<Post>> result = postSvc.getPublicPosts();
            List<Post> posts = result.get();
            JsonArrayBuilder postArr = Json.createArrayBuilder();
            for(Post p : posts) {
                postArr.add(Post.toJson(p));
            }
            return ResponseEntity.ok(postArr.build().toString());
        } catch (DataAccessException ex) {
            return ResponseEntity.status(400)
                .body(Json.createObjectBuilder()
                    .add("message", ex.getMessage())
                    .build().toString());
        }
    }

    @GetMapping(path="/get-posts")
    public ResponseEntity<String> getPosts(@RequestParam String userId) {
        try {
            logger.info("[Post Controller] Retrieving posts...");
            Optional<List<Post>> result = postSvc.getPostsByUserId(userId);
            List<Post> posts = result.get();
            JsonArrayBuilder postArr = Json.createArrayBuilder();
            for(Post p : posts) {
                postArr.add(Post.toJson(p));
            }
            return ResponseEntity.ok(postArr.build().toString());
        } catch (DataAccessException ex) {
            return ResponseEntity.status(400)
                .body(Json.createObjectBuilder()
                    .add("message", ex.getMessage())
                    .build().toString());
        }
    }

    @DeleteMapping(path="/delete-post/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable String postId) {
        try {
            logger.info("[Post Ctrl] Deleting post: " + postId);
            boolean deleted = postSvc.deletePost(postId);
            return ResponseEntity.ok(Json.createObjectBuilder()
                    .add("message", "Deleted " + postId + ": " + deleted)
                    .build().toString());
        } catch (DataAccessException ex) {
            return ResponseEntity.status(400)
                .body(Json.createObjectBuilder()
                    .add("message", ex.getMessage())
                    .build().toString());
        }
    }
}
