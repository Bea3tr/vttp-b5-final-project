package vttp.project.server.models;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemMediaFile {

  private String id;
  private String itemId;
  private String type;
  private byte[] file;

  public String getId() {return id;}
  public void setId(String id) {this.id = id;}

  public String getItemId() {return itemId;}
  public void setItemId(String itemId) {this.itemId = itemId;}

  public String getType() {return type;}
  public void setType(String type) {this.type = type;}

  public byte[] getFile() {return file;}
  public void setFile(byte[] file) {this.file = file;}

  public static ItemMediaFile populate(ResultSet rs) throws SQLException {
    ItemMediaFile mf = new ItemMediaFile();
    mf.setId(rs.getString("id"));
    mf.setItemId(rs.getString("item_id"));
    mf.setType(rs.getString("type"));
    mf.setFile(rs.getBytes("file"));
    return mf;
  }

}
