package vttp.project.server.repositories;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static vttp.project.server.models.Utils.*;

import java.text.SimpleDateFormat;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.client.result.UpdateResult;

@Repository
public class CommentRepository {

    private static final SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    @Autowired
    private MongoTemplate mgTemplate;

    public boolean postComment(String userId, String postId, String comment) {
        Document commentToInsert = new Document(COMMENTID, 
            UUID.randomUUID().toString().substring(0, 8))
            .append(USERID, userId)
            .append(POSTID, postId)
            .append(COMMENT, comment)
            .append(TIMESTAMP, df.format(new Date()));
        Document result = mgTemplate.insert(commentToInsert, C_COMMENT);
        return !result.isEmpty();
    }

    public boolean editComment(String cId, String comment) {
        Criteria criteria = Criteria.where(COMMENTID).is(cId);
        Query queryForOld = Query.query(criteria);
        queryForOld.fields().exclude("_id")
            .include(COMMENT, TIMESTAMP);
        Document oldComment = mgTemplate.findOne(queryForOld, Document.class, C_COMMENT);
        Update updateOps = new Update()
            .set(COMMENT, comment)
            .set(TIMESTAMP, df.format(new Date()))
            .push(EDITED, oldComment);

        Query query = Query.query(
            Criteria.where(COMMENTID).is(cId)
        );
        UpdateResult result = mgTemplate.upsert(query, updateOps, C_COMMENT);
        return result.getModifiedCount() > 0;
    }

    public Document deleteComment(String cId) {
        Query query = Query.query(Criteria.where(COMMENTID).is(cId));
        Document deleted = mgTemplate.findAndRemove(query, Document.class, C_COMMENT);
        return deleted;
    }

    public List<Document> getCommentsByPost(String postId) {
        Criteria criteria = Criteria.where(POSTID).is(postId);
        Query query = Query.query(criteria);
        query.fields().exclude("_id")
            .include(COMMENTID, COMMENT, TIMESTAMP, USERID, POSTID);
        return mgTemplate.find(query, Document.class, C_COMMENT);
    }
    
}
