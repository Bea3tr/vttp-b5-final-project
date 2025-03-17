package vttp.project.server.services;

import static vttp.project.server.models.Utils.*;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import vttp.project.server.repositories.CommentRepository;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepo;

    public boolean postComment(String userId, String postId, String comment) {
        return commentRepo.postComment(userId, postId, comment);
    }

    public boolean editComment(String cId, String comment) {
        return commentRepo.editComment(cId, comment);
    }

    public JsonObject deleteComment(String cId) {
        Document deleted = commentRepo.deleteComment(cId);
        JsonObject result = Json.createObjectBuilder()
            .add(COMMENT, deleted.getString(COMMENT))
            .add(TIMESTAMP, deleted.getString(TIMESTAMP))
            .build();
        return result;
    }

    public JsonArray getCommentsByPost(String postId) {
        JsonArrayBuilder cBuilder = Json.createArrayBuilder();
        List<Document> result = commentRepo.getCommentsByPost(postId);
        if(result != null) {
            for(Document doc : result) {
                JsonObject comment = Json.createObjectBuilder()
                    .add(ID, doc.getString(COMMENTID))
                    .add(USERID, doc.getString(USERID))
                    .add(POSTID, doc.getString(POSTID))
                    .add(COMMENT, doc.getString(COMMENT))
                    .add(TIMESTAMP, doc.getString(TIMESTAMP))
                    .build();
                cBuilder.add(comment);
            }
        }
        return cBuilder.build();
    }
    
}
