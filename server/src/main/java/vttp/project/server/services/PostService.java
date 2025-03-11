package vttp.project.server.services;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import vttp.project.server.repositories.PostRepository;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepo;

    public String upload(MultipartFile file, String post, String id)
        throws DataAccessException, IOException {
        return postRepo.upload(file, post, id);
    }
    
}
