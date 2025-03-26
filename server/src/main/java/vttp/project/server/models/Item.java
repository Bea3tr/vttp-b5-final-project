package vttp.project.server.models;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.bson.Document;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

import static vttp.project.server.models.Utils.*;

public class Item {

    private static SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
    private static final String BASE64_IMG_PREFIX = "data:image/png;base64,";

    private String id;
    private String userId;
    private String username;
    private byte[] userImg;
    private Date timestamp;
    private String item;
    private String description;
    private Double price;
    private boolean purchased;
    private List<ItemMediaFile> files = new LinkedList<>();

    public boolean isPurchased() {return purchased;}
    public void setPurchased(boolean purchased) {this.purchased = purchased;}

    public String getId() {return id;}
    public void setId(String id) {this.id = id;}

    public String getItem() {return item;}
    public void setItem(String item) {this.item = item;}

    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    public Double getPrice() {return price;}
    public void setPrice(Double price) {this.price = price;}

    public List<ItemMediaFile> getFiles() {return files;}
    public void setFiles(List<ItemMediaFile> files) {this.files = files;}

    public String getUserId() {return userId;}
    public void setUserId(String userId) {this.userId = userId;}

    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}

    public byte[] getUserImg() {return userImg;}
    public void setUserImg(byte[] userImg) {this.userImg = userImg;}

    public Date getTimestamp() {return timestamp;}
    public void setTimestamp(Date timestamp) {this.timestamp = timestamp;}

    public static JsonObject toJson(Item item) {
        JsonArrayBuilder fileArr = Json.createArrayBuilder();
        String content = "";
        if (item.getItem() != null) {
            content = item.getItem();
        }
        if (!item.getFiles().isEmpty()) {
            item.getFiles().forEach(file -> {
                String encodedString = "";
                if (file.getType().startsWith("image")) {
                    // logger.info("[item] File type: image");
                    encodedString = BASE64_IMG_PREFIX + Base64.getEncoder().encodeToString(file.getFile());
                }
                JsonObject fileObj = Json.createObjectBuilder()
                        .add(ID, file.getId())
                        .add(TYPE, file.getType())
                        .add(FILE, encodedString)
                        .build();
                fileArr.add(fileObj);
            });
        }
        return Json.createObjectBuilder()
                .add(ID, item.getId())
                .add(USERID, item.getUserId())
                .add(NAME, item.getUsername())
                .add(USERIMG, BASE64_IMG_PREFIX + Base64.getEncoder().encodeToString(item.getUserImg()))
                .add(ITEM, content)
                .add(DESCRIPTION, item.getDescription())
                .add(PRICE, item.getPrice())
                .add(FILES, fileArr)
                .add(TIMESTAMP, df.format(new Date(item.getTimestamp().getTime())))
                .add(F_PURCHASED, item.isPurchased())
                .build();
    }

    public static Item docToItem(Document doc) {
        Item item = new Item();
        item.setId(doc.getString("_id"));
        item.setUserId(doc.getString(USERID));
        item.setItem(doc.getString(ITEM));
        item.setTimestamp(doc.getDate(TIMESTAMP));
        item.setDescription(doc.getString(DESCRIPTION));
        item.setPrice(doc.getDouble(PRICE));
        item.setPurchased(doc.getBoolean(F_PURCHASED));
        return item;
    }

}
