package vttp.project.server.services;

import static vttp.project.server.models.Utils.F_SAVED_POSTS;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import vttp.project.server.models.Post;
import vttp.project.server.models.UserInfo;
import vttp.project.server.repositories.PostRepository;

@Service
public class PostService {

    private static final Logger logger = Logger.getLogger(PostService.class.getName());

    @Autowired
    private PostRepository postRepo;

    public boolean deletePost(String postId) throws DataAccessException {
        return postRepo.deletePost(postId);
    }

    public String upload(List<MultipartFile> files, String post, String status, UserInfo ui)
            throws DataAccessException, IOException {
        return postRepo.upload(files, post, status, ui);
    }

    public boolean editPost(String postId, String edited) {
        return postRepo.editPost(postId, edited);
    }

    public Optional<List<Post>> getPostsByUserId(String userId) throws DataAccessException {
        return postRepo.getPostsByUserId(userId);
    }

    public Optional<List<Post>> getPostsByUserIdPublic(String userId) throws DataAccessException {
        return postRepo.getPostsByUserIdPublic(userId);
    }

    public Optional<List<Post>> getPublicPosts() {
        return postRepo.getPublicPosts();
    }

    public boolean savePostToUser(String userId, String postId) {
        return postRepo.savePostToUser(userId, postId);
    }

    public boolean removeSavedPost(String userId, String postId) {
        return postRepo.removeSavedPost(userId, postId);
    }

    public JsonArray getSavedPosts(String userId) {
        Document result = postRepo.getSavedPosts(userId);
        JsonArrayBuilder pfArr = Json.createArrayBuilder();
        try {
            List<String> postIds = result.getList(F_SAVED_POSTS, String.class);
            if (!postIds.isEmpty()) {
                for (String id : postIds)
                    pfArr.add(id);
            }
        } catch (Exception ex) {
            // ignore null return
        }
        return pfArr.build();
    }

    public Optional<List<Post>> getSavedPostsData(String userId) {
        Document result = postRepo.getSavedPosts(userId);
        List<String> postIds = result.getList(F_SAVED_POSTS, String.class);
        logger.info("Saved post ids: " + postIds);
        return postRepo.getPostsSaved(postIds);
    }

}
