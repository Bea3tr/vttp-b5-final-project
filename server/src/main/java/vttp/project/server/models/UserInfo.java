package vttp.project.server.models;

import static vttp.project.server.models.Utils.*;

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
            .add(ID, ui.getId())
            .add(NAME, ui.getName())
            .add(EMAIL, ui.getEmail())
            .add(PICTURE, ui.getPicture().toString())
            .add(PASSWORD, ui.getPassword())
            .add("googleLogin", ui.isGoogleLogin())
            .build();
    }

    public static UserInfo populate(ResultSet rs) throws SQLException {
        UserInfo ui = new UserInfo();
        ui.setId(rs.getString(ID));
        ui.setName(rs.getString(NAME));
        ui.setEmail(rs.getString(EMAIL));
        ui.setPicture(rs.getBytes(PICTURE));
        ui.setPassword(rs.getString(PASSWORD));
        ui.setGoogleLogin(rs.getBoolean(GLOGIN));
        return ui;
    }

    @Override
    public String toString() {
        return "UserInfo [id=" + id + ", email=" + email + ", name=" + name + ", picture=" + picture + ", password="
                + password + ", googleLogin=" + googleLogin + "]";
    }

}
