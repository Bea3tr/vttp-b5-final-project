package vttp.project.server.repositories;

import static vttp.project.server.models.Utils.*;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import vttp.project.server.models.Item;
import vttp.project.server.models.ItemMediaFile;
import vttp.project.server.models.UserInfo;

@Repository
public class ShopRepository {

  private static final Logger logger = Logger.getLogger(ShopRepository.class.getName());

  @Autowired
  private JdbcTemplate sqlTemplate;

  @Autowired
  private MongoTemplate mgTemplate;

  public String listItem(List<MultipartFile> files,
      String userId, String item, String desc, double price)
      throws IOException, RuntimeException {
    logger.info("[Shop Repo] Inserting items");
    String itemId = UUID.randomUUID().toString().substring(0, 8);

    insertItem(itemId, userId, item, desc, price);
    if (files != null) {
      files.forEach(file -> {
        try {
          saveFile(file, itemId);
        } catch (Exception e) {
          logger.warning("[Shop Repo] Error: " + e.getMessage());
          throw new RuntimeException("Error saving file: " + file.getOriginalFilename(), e);
        }
      });
    }
    return itemId;
  }

  public Optional<List<Item>> getItems(String userId) {
    logger.info("[Shop Repo] Retrieving items");
    Query query = Query.query(Criteria.where(F_PURCHASED).is(false)
      .andOperator(Criteria.where(USERID)).nin(userId));
    // Find all items
    List<Document> itemDocs = mgTemplate.find(query, Document.class, C_ITEM);
    return getItemsFromDocs(itemDocs);
  }

  public Optional<List<Item>> getItems(String filter, String userId) {
    logger.info("[Shop Repo] Retrieving items: " + filter);
    Criteria criteria = Criteria.where(ITEM).regex(filter, "i")
        .andOperator(Criteria.where(F_PURCHASED).is(false),
                    Criteria.where(USERID).nin(userId));
    List<Document> docs = mgTemplate.find(Query.query(criteria), Document.class, C_ITEM);
    return getItemsFromDocs(docs);
  }

  public Optional<List<Item>> getListedItems(String userId) {
    logger.info("[Shop Repo] Retrieving items listed by: " + userId);
    Criteria criteria = Criteria.where(USERID).is(userId);
    List<Document> docs = mgTemplate.find(Query.query(criteria), Document.class, C_ITEM);
    return getItemsFromDocs(docs);
  }

  public boolean saveItemToUser(String userId, String itemId) {
    Update updateOps = new Update()
        .addToSet(F_SAVED_ITEMS, itemId);
    Query query = Query.query(Criteria.where(USERID).is(userId));
    UpdateResult result = mgTemplate.upsert(query, updateOps, C_USER);
    return result.getModifiedCount() > 0;
  }

  public boolean removeItemFromUser(String userId, String itemId) {
    Query query = new Query(Criteria.where(USERID).is(userId));
    Update updateOps = new Update()
        .pull(F_SAVED_ITEMS, itemId);

    UpdateResult result = mgTemplate.upsert(query, updateOps, C_USER);
    return result.getModifiedCount() > 0;
  }

  public Document getSavedItems(String userId) {
    Criteria criteria = Criteria.where(USERID).is(userId);
    Query query = Query.query(criteria);
    return mgTemplate.findOne(query, Document.class, C_USER);
  }

  public boolean purchaseItem(String itemId) {
    Update updateOps = new Update()
        .set(F_PURCHASED, true);
    Query query = Query.query(Criteria.where("_id").is(itemId));
    UpdateResult result = mgTemplate.updateMulti(query, updateOps, C_ITEM);
    return result.getModifiedCount() > 0;
  }

  public boolean deleteItem(String itemId) {
    Query query = Query.query(Criteria.where("_id").is(itemId));
    DeleteResult result = mgTemplate.remove(query, C_ITEM);
    deleteItemFiles(itemId);
    return result.getDeletedCount() > 0;
  }

  public Optional<Item> getItemById(String itemId) {
    Query query = Query.query(Criteria.where("_id").is(itemId));
    List<Document> docs = mgTemplate.find(query, Document.class, C_ITEM);
    Optional<List<Item>> items = getItemsFromDocs(docs);
    if(!items.isEmpty()) {
      return Optional.of(items.get().get(0));
    }
    return Optional.empty();
  }

  @SuppressWarnings("unused")
  private Optional<List<Item>> getItemsFromDocs(List<Document> docs) {
    List<Item> items = new LinkedList<>();
    for (Document doc : docs) {
      Item item = Item.docToItem(doc);
      List<ItemMediaFile> mediaFiles = sqlTemplate.query(
          SQL_GET_MEDIA_FILES_BY_ITEMID,
          (ResultSet rs_mf, int rowNum) -> {
            return ItemMediaFile.populate(rs_mf);
          }, item.getId());

      Optional<UserInfo> opt_ui = sqlTemplate.query(
          SQL_GET_USER,
          (ResultSet rs_ui) -> {
            if (rs_ui.next()) {
              return Optional.of(UserInfo.populate(rs_ui));
            } else {
              return Optional.empty();
            }
          }, item.getUserId());
      if (!opt_ui.isEmpty()) {
        UserInfo ui = opt_ui.get();
        item.setUserImg(ui.getPicture());
        item.setUsername(ui.getName());
      }
      item.setFiles(mediaFiles);
      items.add(item);
    }
    return items.isEmpty() ? Optional.empty() : Optional.of(items);
  }

  // Save files in MySQL
  private void saveFile(MultipartFile file, String itemId)
      throws DataAccessException, IOException {

    sqlTemplate.update(SQL_INSERT_ITEM_MEDIA_FILE,
        UUID.randomUUID().toString().substring(0, 8),
        itemId, file.getContentType(), file.getBytes());
  }

  // Save item details in MongoDB
  private void insertItem(String id, String userId, String item, String desc, double price) {
    Document toInsert = new Document()
        .append("_id", id)
        .append(USERID, userId)
        .append(ITEM, item)
        .append(DESCRIPTION, desc)
        .append(PRICE, price)
        .append(TIMESTAMP, new Date())
        .append(F_PURCHASED, false);

    mgTemplate.insert(toInsert, C_ITEM);
  }

  private boolean deleteItemFiles(String itemId) {
    return sqlTemplate.update(SQL_DELETE_ITEM_FILES, itemId) > 0;
  }
}
