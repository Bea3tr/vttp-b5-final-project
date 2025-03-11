package vttp.project.server.models;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public class UserInfo {

    private String id;
    private String email;
    private String name;
    private String picture;
    private String password;
    private boolean googleLogin;
    
    public String getId() {return id;}
    public void setId(String id) {this.id = id;}
    
    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}
    
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    
    public String getPicture() {return picture;}
    public void setPicture(String picture) {this.picture = picture;}

    public boolean isGoogleLogin() {return googleLogin;}
    public void setGoogleLogin(boolean googleLogin) {this.googleLogin = googleLogin;}
    
    public static JsonObject toJson(UserInfo ui) {
        return Json.createObjectBuilder()
            .add("id", ui.getId())
            .add("name", ui.getName())
            .add("email", ui.getEmail())
            .add("picture", ui.getPicture())
            .add("password", ui.getPassword())
            .add("googleLogin", ui.isGoogleLogin())
            .build();
    }

    @Override
    public String toString() {
        return "UserInfo [id=" + id + ", email=" + email + ", name=" + name + ", picture=" + picture + ", password="
                + password + ", googleLogin=" + googleLogin + "]";
    }

}
