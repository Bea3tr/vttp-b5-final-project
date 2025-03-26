package vttp.project.server.repositories;

import static vttp.project.server.models.Utils.*;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.client.result.UpdateResult;

import vttp.project.server.models.MediaFile;
import vttp.project.server.models.Post;
import vttp.project.server.models.UserInfo;

@Repository
public class PostRepository {

    private static final Logger logger = Logger.getLogger(PostRepository.class.getName());

    @Autowired
    private JdbcTemplate template;

    @Autowired
    private MongoTemplate mgTemplate;

    public boolean deletePost(String postId) throws DataAccessException {
        return template.update(SQL_DELETE_POST_BY_ID, postId) > 0;
    }

    public String upload(List<MultipartFile> files, String post, String status, UserInfo ui)
            throws IOException, RuntimeException {
        String postId = UUID.randomUUID().toString().substring(0, 8);

        template.update(SQL_INSERT_POST, postId, ui.getId(), post, status);
        if (files != null) {
            files.forEach(file -> {
                try {
                    saveFile(file, postId);
                } catch (Exception e) {
                    throw new RuntimeException("Error saving file: " + file.getOriginalFilename(), e);
                }
            });
        }
        return postId;
    }

    public boolean editPost(String postId, String edited) {
        return template.update(SQL_UPDATE_POST, edited, postId) > 0;
    }

    private void saveFile(MultipartFile file, String postId)
            throws DataAccessException, IOException {

        template.update(SQL_INSERT_MEDIA_FILE,
                UUID.randomUUID().toString().substring(0, 8),
                postId, file.getContentType(), file.getBytes());

    }

    public Optional<List<Post>> getPostsByUserId(String userId) throws DataAccessException {
        return getPosts(SQL_GET_POST_BY_USERID, userId);
    }

    public Optional<List<Post>> getPostsByUserIdPublic(String userId) throws DataAccessException {
        return getPosts(SQL_GET_POST_BY_USERID_PUBLIC, userId);
    }

    @SuppressWarnings("unused")
    public Optional<List<Post>> getPublicPosts() throws DataAccessException {
        return template.query(
                SQL_GET_PUBLIC_POSTS,
                (ResultSet rs) -> {
                    List<Post> posts = new LinkedList<>();
                    while (rs.next()) {
                        logger.info("[Post Repo] Result set: " + rs.getString("id"));
                        Post post = Post.populate(rs);

                        // Fetch media files using queryForList to avoid cursor interference
                        List<MediaFile> mediaFiles = template.query(
                                SQL_GET_MEDIA_FILES_BY_POSTID,
                                (ResultSet rs_mf, int rowNum) -> {
                                    // logger.info("[Post Repo] Media files for: " + post.getId());
                                    return MediaFile.populate(rs_mf);
                                }, post.getId());

                        Optional<UserInfo> opt_ui = template.query(
                                SQL_GET_USER,
                                (ResultSet rs_ui) -> {
                                    if (rs_ui.next()) {
                                        return Optional.of(UserInfo.populate(rs_ui));
                                    } else {
                                        return Optional.empty();
                                    }
                                }, post.getUserId());
                        if (!opt_ui.isEmpty()) {
                            UserInfo ui = opt_ui.get();
                            post.setUserImg(ui.getPicture());
                            post.setUsername(ui.getName());
                        }

                        post.setFiles(mediaFiles);
                        posts.add(post);
                    }
                    return posts.isEmpty() ? Optional.empty() : Optional.of(posts);
                });
    }

    public boolean savePostToUser(String userId, String postId) {
        Update updateOps = new Update()
                .addToSet(F_SAVED_POSTS, postId);
        Query query = Query.query(Criteria.where(USERID).is(userId));
        UpdateResult result = mgTemplate.upsert(query, updateOps, C_USER);
        return result.getModifiedCount() > 0;
    }

    public Document getSavedPosts(String userId) {
        Criteria criteria = Criteria.where(USERID).is(userId);
        Query query = Query.query(criteria);
        return mgTemplate.findOne(query, Document.class, C_USER);
    }

    public boolean removeSavedPost(String userId, String postId) {
        Query query = new Query(Criteria.where(USERID).is(userId));
        Update updateOps = new Update()
                .pull(F_SAVED_POSTS, postId);

        UpdateResult result = mgTemplate.upsert(query, updateOps, C_USER);
        return result.getModifiedCount() > 0;
    }

    public Optional<List<Post>> getPostsSaved(List<String> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Optional.empty();
        }
        String placeholders = String.join(",", postIds.stream().map(id -> "?").toList());
        String SQL_GET_SAVED_POSTS = "SELECT * FROM myapp.posts WHERE id IN (" + placeholders + ")";

        logger.info("[Post Repo] ids: " + placeholders);

        return template.query(
                SQL_GET_SAVED_POSTS,
                (ResultSet rs) -> {
                    List<Post> posts = new LinkedList<>();
                    while (rs.next()) {
                        Post post = Post.populate(rs);
                        List<MediaFile> mediaFiles = template.query(
                                SQL_GET_MEDIA_FILES_BY_POSTID,
                                (ResultSet rs_mf, int rowNum) -> {
                                    return MediaFile.populate(rs_mf);
                                }, post.getId());
                        Optional<UserInfo> opt_ui = template.query(
                                SQL_GET_USER,
                                (ResultSet rs_ui) -> {
                                    if (rs_ui.next()) {
                                        return Optional.of(UserInfo.populate(rs_ui));
                                    } else {
                                        return Optional.empty();
                                    }
                                }, post.getUserId());
                        if (!opt_ui.isEmpty()) {
                            UserInfo ui = opt_ui.get();
                            post.setUserImg(ui.getPicture());
                            post.setUsername(ui.getName());
                        }
                        post.setFiles(mediaFiles);
                        posts.add(post);
                        logger.info("[Post Repo] Posts: " + posts);
                    }
                    if (posts.isEmpty()) {
                        return Optional.empty();
                    } else {
                        return Optional.of(posts);
                    }
                }, postIds.toArray());
    }

    private Optional<List<Post>> getPosts(String query, String userId) {
        return template.query(query,
                (ResultSet rs) -> {
                    List<Post> posts = new LinkedList<>();
                    while (rs.next()) {
                        Post post = Post.populate(rs);
                        List<MediaFile> mediaFiles = template.query(
                                SQL_GET_MEDIA_FILES_BY_POSTID,
                                (ResultSet rs_mf, int rowNum) -> {
                                    return MediaFile.populate(rs_mf);
                                }, post.getId());
                        Optional<UserInfo> opt_ui = template.query(
                                SQL_GET_USER,
                                (ResultSet rs_ui) -> {
                                    if (rs_ui.next()) {
                                        return Optional.of(UserInfo.populate(rs_ui));
                                    } else {
                                        return Optional.empty();
                                    }
                                }, userId);
                        if (!opt_ui.isEmpty()) {
                            UserInfo ui = opt_ui.get();
                            post.setUserImg(ui.getPicture());
                            post.setUsername(ui.getName());
                        }
                        post.setFiles(mediaFiles);
                        posts.add(post);
                    }
                    if (posts.isEmpty()) {
                        return Optional.empty();
                    } else {
                        return Optional.of(posts);
                    }
                }, userId);
    }

}
