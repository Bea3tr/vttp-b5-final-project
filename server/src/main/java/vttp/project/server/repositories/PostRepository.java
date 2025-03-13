package vttp.project.server.repositories;

import static vttp.project.server.models.Utils.*;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import vttp.project.server.models.Post;
import vttp.project.server.models.UserInfo;

@Repository
public class PostRepository {

    @Autowired
    private JdbcTemplate template;

    public String upload(MultipartFile file, String post, String status, UserInfo ui) 
        throws DataAccessException, IOException {
        String postId = UUID.randomUUID().toString().substring(0, 8);
        if(file == null || file.isEmpty()) {
            template.update(SQL_INSERT_POST, postId, ui.getId(), ui.getName(), ui.getPicture(), post, null, status);
        } else {
            template.update(SQL_INSERT_POST, postId, ui.getId(), ui.getName(), ui.getPicture(), post, file.getBytes(), status);
        }
        return postId;
    }

    public Optional<List<Post>> getPostsByUserId(String userId) throws DataAccessException {
        return template.query(
            SQL_GET_POST_BY_USERID,
            (ResultSet rs) -> {
                List<Post> posts = new LinkedList<>();
                while(rs.next()) {
                    posts.add(Post.populate(rs));
                }
                if(posts.isEmpty()) {
                    return Optional.empty();
                } else {
                    return Optional.of(posts);
                }
            }, userId);
    }


    
}
