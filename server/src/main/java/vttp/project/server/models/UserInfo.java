package vttp.project.server.models;

import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public class UserInfo {

    private String id;
    private String email;
    private String name;
    private byte[] picture;
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
    
    public byte[] getPicture() {return picture;}
    public void setPicture(byte[] picture) {this.picture = picture;}

    public boolean isGoogleLogin() {return googleLogin;}
    public void setGoogleLogin(boolean googleLogin) {this.googleLogin = googleLogin;}
    
    public static JsonObject toJson(UserInfo ui) {
        return Json.createObjectBuilder()
            .add("id", ui.getId())
            .add("name", ui.getName())
            .add("email", ui.getEmail())
            .add("picture", ui.getPicture().toString())
            .add("password", ui.getPassword())
            .add("googleLogin", ui.isGoogleLogin())
            .build();
    }

    public static UserInfo populate(ResultSet rs) throws SQLException {
        UserInfo ui = new UserInfo();
        ui.setId(rs.getString("id"));
        ui.setName(rs.getString("name"));
        ui.setEmail(rs.getString("email"));
        ui.setPicture(rs.getBytes("picture"));
        ui.setPassword(rs.getString("password"));
        ui.setGoogleLogin(rs.getBoolean("google_login"));
        return ui;
    }

    @Override
    public String toString() {
        return "UserInfo [id=" + id + ", email=" + email + ", name=" + name + ", picture=" + picture + ", password="
                + password + ", googleLogin=" + googleLogin + "]";
    }

}
