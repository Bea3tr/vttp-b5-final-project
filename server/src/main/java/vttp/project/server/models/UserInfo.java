package vttp.project.server.models;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public class UserInfo {

    private String email;
    private String name;
    private String profilePicture;
    
    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}
    
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    
    public String getProfilePicture() {return profilePicture;}
    public void setProfilePicture(String profilePicture) {this.profilePicture = profilePicture;}

    public static JsonObject toJson(UserInfo ui) {
        return Json.createObjectBuilder()
            .add("name", ui.getName())
            .add("email", ui.getEmail())
            .add("picture", ui.getProfilePicture())
            .build();
    }
    @Override
    public String toString() {
        return "UserInfo [email=" + email + ", name=" + name + ", profilePicture=" + profilePicture + "]";
    }

    

    
    
}
