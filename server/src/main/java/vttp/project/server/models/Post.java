package vttp.project.server.models;

import static vttp.project.server.models.Utils.*;

import java.util.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Base64;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public class Post {

    private static SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
    private static final String BASE64_PREFIX = "data:image/png;base64,";

    private String id;
    private String userId;
    private String username;
    private byte[] userImg;
    private String post;
    private byte[] picture;
    private Date timestamp;
    private String status;

    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}
    
    public String getId() {return id;}
    public void setId(String id) {this.id = id;}
    
    public String getUserId() {return userId;}
    public void setUserId(String userId) {this.userId = userId;}
    
    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}
    
    public byte[] getUserImg() {return userImg;}
    public void setUserImg(byte[] userImg) {this.userImg = userImg;}
    
    public String getPost() {return post;}
    public void setPost(String post) {this.post = post;}
    
    public byte[] getPicture() {return picture;}
    public void setPicture(byte[] picture) {this.picture = picture;}
    
    public Date getTimestamp() {return timestamp;}
    public void setTimestamp(Date timestamp) {this.timestamp = timestamp;}

     public static JsonObject toJson(Post post) {
        String picture = "";
        String content = "";
        if(post.getPicture() != null) {
            picture = BASE64_PREFIX + Base64.getEncoder().encodeToString(post.getPicture());
        }
        if(post.getPost() != null) {
            content = post.getPost();
        }
        return Json.createObjectBuilder()
            .add(ID, post.getId())
            .add(USERID, post.getUserId())
            .add(NAME, post.getUsername())
            .add(USERIMG, BASE64_PREFIX + Base64.getEncoder().encodeToString(post.getUserImg()))
            .add(POST, content)
            .add(PICTURE, picture)
            .add(TIMESTAMP, df.format(new Date(post.getTimestamp().getTime())))
            .add(STATUS, post.getStatus())
            .build();
    }
    
    public static Post populate(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getString(ID));
        post.setUserId(rs.getString(USERID));
        post.setUsername(rs.getString(NAME));
        post.setUserImg(rs.getBytes(USERIMG));
        post.setPost(rs.getString(POST));
        post.setPicture(rs.getBytes(PICTURE));
        post.setTimestamp(rs.getDate(TIMESTAMP));
        post.setStatus(rs.getString(STATUS));
        return post;
    }    
    
}
