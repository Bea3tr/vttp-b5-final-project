package vttp.project.server.services;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import vttp.project.server.models.Item;
import vttp.project.server.repositories.ShopRepository;

import static vttp.project.server.models.Utils.*;

@Service
public class ShopService {

  @Autowired
  private ShopRepository shopRepo;

  public String listItem(List<MultipartFile> files,
      String userId, String item, String desc, String price)
      throws IOException, RuntimeException {
        Double convertedPrice = Double.parseDouble(price);
    return shopRepo.listItem(files, userId, item, desc, convertedPrice);
  }

  public JsonArray getItems(String userId) {
    Optional<List<Item>> items = shopRepo.getItems(userId);
    return itemsToArr(items);
  }

  public JsonArray getItems(String filter, String userId) {
    Optional<List<Item>> items = shopRepo.getItems(filter, userId);
    return itemsToArr(items);
  }

  public JsonArray getListedItems(String userId) {
    Optional<List<Item>> items = shopRepo.getListedItems(userId);
    return itemsToArr(items);
  }

  public JsonObject getItemById(String itemId) {
    Optional<Item> item = shopRepo.getItemById(itemId);
    if(!item.isEmpty()) {
      return Item.toJson(item.get());
    }
    return Json.createObjectBuilder()
      .add("message", "Item not found")
      .build();
  }

  public boolean deleteItem(String itemId) {
    return shopRepo.deleteItem(itemId);
  }

  public boolean purchaseItem(String itemId) {
    return shopRepo.purchaseItem(itemId);
  }

  public boolean saveItemToUser(String userId, String itemId) {
    return shopRepo.saveItemToUser(userId, itemId);
  }

  public boolean removeItemFromUser(String userId, String itemId) {
    return shopRepo.removeItemFromUser(userId, itemId);
  }

  public JsonArray getSavedItems(String userId) {
    Document result = shopRepo.getSavedItems(userId);
    JsonArrayBuilder itemArr = Json.createArrayBuilder();
    try {
      List<String> itemIds = result.getList(F_SAVED_ITEMS, String.class);
      if (!itemIds.isEmpty()) {
        for (String id : itemIds)
          itemArr.add(id);
      }
    } catch (Exception ex) {
      // ignore null return
    }
    return itemArr.build();
  }

  private JsonArray itemsToArr(Optional<List<Item>> items) {
    JsonArrayBuilder itemArr = Json.createArrayBuilder();
    if (!items.isEmpty()) {
      for (Item i : items.get()) {
        JsonObject itemObj = Item.toJson(i);
        itemArr.add(itemObj);
      }
    }
    return itemArr.build();
  }
}
