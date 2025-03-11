package vttp.project.server.repositories;

import static vttp.project.server.models.Utils.SQL_UPLOAD_POST;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

@Repository
public class PostRepository {

    @Autowired
    private JdbcTemplate template;

    public String upload(MultipartFile file, String post, String id) 
        throws DataAccessException, IOException {
        String postId = UUID.randomUUID().toString().substring(0, 8);
        template.update(SQL_UPLOAD_POST, postId, id, post, file.getBytes());
        return postId;
    }
    
}
