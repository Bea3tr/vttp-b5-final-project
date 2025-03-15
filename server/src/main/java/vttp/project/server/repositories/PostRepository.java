package vttp.project.server.repositories;

import static vttp.project.server.models.Utils.*;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import vttp.project.server.models.MediaFile;
import vttp.project.server.models.Post;
import vttp.project.server.models.UserInfo;

@Repository
public class PostRepository {

    private static final Logger logger = Logger.getLogger(PostRepository.class.getName());

    @Autowired
    private JdbcTemplate template;

    // public String upload(MultipartFile file, String post, String status, UserInfo
    // ui)
    public String upload(List<MultipartFile> files, String post, String status, UserInfo ui)
            throws IOException, RuntimeException {
        String postId = UUID.randomUUID().toString().replaceAll("\\-", "")
                .substring(0, 16);

        template.update(SQL_INSERT_POST, postId, ui.getId(), ui.getName(), ui.getPicture(), post, status);
        if (!files.isEmpty()) {
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

    private void saveFile(MultipartFile file, String postId)
            throws DataAccessException, IOException {

        template.update(SQL_INSERT_MEDIA_FILE,
                UUID.randomUUID().toString().replaceAll("\\-", "")
                        .substring(0, 16),
                postId, file.getContentType(), file.getBytes());

    }

    public Optional<List<Post>> getPostsByUserId(String userId) throws DataAccessException {
        return template.query(
                SQL_GET_POST_BY_USERID,
                (ResultSet rs) -> {
                    List<Post> posts = new LinkedList<>();
                    while (rs.next()) {
                        List<MediaFile> mediaFiles = new LinkedList<>();
                        Post post = Post.populate(rs);
                        template.query(
                                SQL_GET_MEDIA_FILES_BY_POSTID,
                                (ResultSet rs_mf) -> {
                                    while (rs_mf.next()) {
                                        mediaFiles.add(MediaFile.populate(rs_mf));
                                    }
                                }, post.getId());
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
                                logger.info("[Post Repo] Media files for: " + post.getId());
                                return MediaFile.populate(rs_mf);
                            }, post.getId());

                        post.setFiles(mediaFiles);
                        posts.add(post);
                    }
                    return posts.isEmpty() ? Optional.empty() : Optional.of(posts);
                }
        );
    }

}
