package vttp.project.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import vttp.project.server.services.LikeService;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

  @Autowired
  private LikeService likeSvc;

  @PostMapping("/{postId}")
  public ResponseEntity<String> likePost(@PathVariable String postId) {
    likeSvc.incrementLikes(postId);
    return ResponseEntity.ok(Json.createObjectBuilder()
        .add("message", "Liked post " + postId)
        .build().toString());
  }

  @DeleteMapping("/{postId}")
  public ResponseEntity<String> unlikePost(@PathVariable String postId) {
    likeSvc.decrementLikes(postId);
    return ResponseEntity.ok(Json.createObjectBuilder()
        .add("message", "Unliked post " + postId)
        .build().toString());
  }

  @GetMapping("/{postId}/count")
  public ResponseEntity<String> getLikeCount(@PathVariable String postId) {
    return ResponseEntity.ok(Json.createObjectBuilder()
        .add("likes", likeSvc.getLikeCount(postId))
        .build().toString());
  }
}
