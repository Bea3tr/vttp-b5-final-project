package vttp.project.server.controllers;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import vttp.project.server.services.ShopService;

@RestController
@RequestMapping(path = "/api/shop", produces = MediaType.APPLICATION_JSON_VALUE)
public class ShopController {

  private static final Logger logger = Logger.getLogger(ShopController.class.getName());

  @Autowired
  private ShopService shopSvc;

  @PostMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<String> listItem(
      @PathVariable String id,
      @RequestPart(value = "files") List<MultipartFile> files,
      @RequestPart(value = "item") String item,
      @RequestPart("description") String desc,
      @RequestPart("price") String price) {
    String itemId = "";
    try {
      itemId = shopSvc.listItem(files, id, item, desc, price);
      logger.info("[Shop Ctrl] Item id: " + itemId);

    } catch (IOException | RuntimeException ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Json.createObjectBuilder()
              .add("message", ex.getMessage())
              .build().toString());
    }
    JsonObject payload = Json.createObjectBuilder()
        .add("message", "Listed item")
        .add("itemId", itemId)
        .add("success", true)
        .build();

    return ResponseEntity.ok(payload.toString());
  }

  @GetMapping("/get/{id}")
  public ResponseEntity<String> getItems(@PathVariable String id) {
    try {
      JsonArray results = shopSvc.getItems(id);
      return ResponseEntity.ok(results.toString());

    } catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Json.createObjectBuilder()
              .add("message", ex.getMessage())
              .build().toString());
    }
  }

  @GetMapping("/get-filtered/{id}")
  public ResponseEntity<String> getItemsFiltered(@PathVariable String id, @RequestParam String filter) {
    try {
      JsonArray results = shopSvc.getItems(filter, id);
      return ResponseEntity.ok(results.toString());

    } catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Json.createObjectBuilder()
              .add("message", ex.getMessage())
              .build().toString());
    }
  }

  @GetMapping("/get-item/{id}")
  public ResponseEntity<String> getItemById(@PathVariable String id) {
    try {
      JsonObject item = shopSvc.getItemById(id);
      return ResponseEntity.ok(item.toString());
    } catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Json.createObjectBuilder()
              .add("message", ex.getMessage())
              .build().toString());
    }
  }

  @GetMapping("/get-listed/{id}")
  public ResponseEntity<String> getListedItems(@PathVariable String id) {
    try {
      JsonArray results = shopSvc.getListedItems(id);
      return ResponseEntity.ok(results.toString());
    } catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Json.createObjectBuilder()
          .add("message", ex.getMessage())
          .build().toString());
    }
  }

  @PutMapping("/purchase")
  public ResponseEntity<String> purchaseItem(@RequestBody String payload) {
    String itemId = Json.createReader(new StringReader(payload))
        .readObject().getJsonObject("body").getString("itemId");

    boolean purchased = shopSvc.purchaseItem(itemId);
    if (purchased) {
      return ResponseEntity.ok(Json.createObjectBuilder()
          .add("message", "Item purchased")
          .build().toString());
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Json.createObjectBuilder()
            .add("message", "Error purchasing item")
            .build().toString());
  }

  @DeleteMapping("/delete")
  public ResponseEntity<String> deleteItem(@RequestBody String payload) {
    logger.info("[Shop Ctrl] Payload: " + payload);
    String itemId = Json.createReader(new StringReader(payload))
        .readObject().getString("itemId");

    boolean deleted = shopSvc.deleteItem(itemId);
    if (deleted) {
      return ResponseEntity.ok(Json.createObjectBuilder()
          .add("message", "Item deleted")
          .build().toString());
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Json.createObjectBuilder()
            .add("message", "Error deleting item")
            .build().toString());
  }

  @PutMapping("/save")
  public ResponseEntity<String> saveItem(@RequestBody String payload) {
    logger.info("[Shop Ctrl] Payload: " + payload);
    JsonObject inObj = Json.createReader(new StringReader(payload))
        .readObject().getJsonObject("body");
    boolean saved = shopSvc.saveItemToUser(inObj.getString("userId"), inObj.getString("itemId"));
    if (saved) {
      return ResponseEntity.ok(Json.createObjectBuilder()
          .add("message", "Saved item to user")
          .build().toString());
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Json.createObjectBuilder()
            .add("message", "Error saving item to user")
            .build().toString());
  }

  @PutMapping("/remove")
  public ResponseEntity<String> removeItem(@RequestBody String payload) {
    logger.info("[Shop Ctrl] Payload: " + payload);
    JsonObject inObj = Json.createReader(new StringReader(payload))
        .readObject().getJsonObject("body");
    boolean removed = shopSvc.removeItemFromUser(inObj.getString("userId"), inObj.getString("itemId"));
    if (removed) {
      return ResponseEntity.ok(Json.createObjectBuilder()
          .add("message", "Removed item from user")
          .build().toString());
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Json.createObjectBuilder()
            .add("message", "Error removing item to user")
            .build().toString());
  }

  @GetMapping(path = "/get-saved/{userId}")
  public ResponseEntity<String> getSavedPosts(@PathVariable String userId) {
      JsonArray result = shopSvc.getSavedItems(userId);
      if (result.isEmpty()) {
          return ResponseEntity.ok(Json.createObjectBuilder()
                  .add("message", "No saved items")
                  .add("result", result)
                  .build().toString());
      }
      return ResponseEntity.ok(Json.createObjectBuilder()
              .add("message", "Data retrieved")
              .add("result", result)
              .build().toString());
  }
}
