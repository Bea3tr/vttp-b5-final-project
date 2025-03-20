package vttp.project.server.services;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import vttp.project.server.models.UserInfo;
import vttp.project.server.repositories.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    public boolean insertNewUser(UserInfo user) {
        return userRepo.insertNewUser(user);
    }

    public boolean userExists(String email) {
        return userRepo.userExists(email);
    }

    public String getUserId(String email) {
        return userRepo.getUserId(email);
    }

    public String getUserPassword(String email) {
        return userRepo.getUserPassword(email);
    }

    public String getUserPasswordById(String id) {
        return userRepo.getUserPasswordById(id);
    }

    public boolean isGoogleLogin(String email) {
        return userRepo.isGoogleLogin(email);
    }

    public Optional<UserInfo> getUserInfo(String id) {
        return userRepo.getUserInfo(id);
    }
    
    public String updatePic(MultipartFile file, String userId) 
        throws IOException, RuntimeException {
        return userRepo.updatePic(file, userId);
    }

    public String updateName(String name, String id) {
        return userRepo.updateName(name, id);
    }

    public String updatePassword(String password, String id) {
        return userRepo.updatePassword(password, id);
    }
}
