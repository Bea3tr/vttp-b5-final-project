package vttp.project.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public boolean isGoogleLogin(String email) {
        return userRepo.isGoogleLogin(email);
    }
    
}
