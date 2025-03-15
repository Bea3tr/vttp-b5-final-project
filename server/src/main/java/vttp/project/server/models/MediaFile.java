package vttp.project.server.models;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MediaFile {

    private String id;
    private String postId;
    private String type;
    private byte[] file;
    
    public String getId() {return id;}
    public void setId(String id) {this.id = id;}
    
    public String getPostId() {return postId;}
    public void setPostId(String postId) {this.postId = postId;}
    
    public String getType() {return type;}
    public void setType(String type) {this.type = type;}
    
    public byte[] getFile() {return file;}
    public void setFile(byte[] file) {this.file = file;}

    public static MediaFile populate(ResultSet rs) throws SQLException {
        MediaFile mf = new MediaFile();
        mf.setId(rs.getString("id"));
        mf.setPostId(rs.getString("post_id"));
        mf.setType(rs.getString("type"));
        mf.setFile(rs.getBytes("file"));
        return mf;
    }
    
}
