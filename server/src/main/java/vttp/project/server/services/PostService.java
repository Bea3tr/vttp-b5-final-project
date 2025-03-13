package vttp.project.server.services;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import vttp.project.server.models.Post;
import vttp.project.server.models.UserInfo;
import vttp.project.server.repositories.PostRepository;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepo;

    public String upload(MultipartFile file, String post, String status, UserInfo ui)
        throws DataAccessException, IOException {
        return postRepo.upload(file, post, status, ui);
    }

    public Optional<List<Post>> getPostsByUserId(String userId) throws DataAccessException {
        return postRepo.getPostsByUserId(userId);
    }
    
}
