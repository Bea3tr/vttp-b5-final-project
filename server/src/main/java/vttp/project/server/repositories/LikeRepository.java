package vttp.project.server.repositories;

import static vttp.project.server.models.Utils.C_LIKES;

import java.util.logging.Logger;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class LikeRepository {

  private static final Logger logger = Logger.getLogger(LikeRepository.class.getName());

  @Autowired
  private MongoTemplate mgTemplate;

  public boolean incrementLikes(String id) {
    logger.info("[Like Repo] Incrementing like: " + id);
    Query query = Query.query(Criteria.where("_id").is(id));
    Document post = mgTemplate.findOne(query, 
      Document.class, C_LIKES);
    int currentLikes = 0;
    if(post != null) {
      currentLikes = post.getInteger("likes");
    }
    Update updateOps = new Update()
      .set("likes", currentLikes+1);
    return mgTemplate.upsert(query, updateOps, C_LIKES).getModifiedCount() > 0;
  }

  public boolean decrementLikes(String id) {
    logger.info("[Like Repo] Decrementing like: " + id);
    Query query = Query.query(Criteria.where("_id").is(id));
    Document post = mgTemplate.findOne(query, 
      Document.class, C_LIKES);
    int currentLikes = 0;
    if(post != null) {
      currentLikes = post.getInteger("likes");
    }
    Update updateOps = new Update()
      .set("likes", currentLikes-1);
    return mgTemplate.upsert(query, updateOps, C_LIKES).getModifiedCount() > 0;
  }
  
  public int getLikesCount(String id) {
    logger.info("[Like Repo] Retrieving like count: " + id);
    Query query = Query.query(Criteria.where("_id").is(id));
    Document post = mgTemplate.findOne(query, 
      Document.class, C_LIKES);
    if(post != null) {
      return post.getInteger("likes");
    }
    return 0;
  }
}
