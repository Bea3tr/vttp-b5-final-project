package vttp.project.server.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import vttp.project.server.models.UserInfo;
import static vttp.project.server.models.Utils.*;

import java.util.logging.Logger;

@Repository
public class UserRepository {

    private static final Logger logger = Logger.getLogger(UserRepository.class.getName());

    @Autowired
    private JdbcTemplate template;

    public boolean insertNewUser(UserInfo user, String id) {
        logger.info("[User Repo] Inserting user into MySQL");
        try {
            return 
                template.update(SQL_INSERT_USER, id, user.getName(), user.getEmail(), user.getProfilePicture()) > 0;
        } catch (DataAccessException ex) {
            logger.warning(ex.getMessage());
            return false;
        }
    }

    public String getUserId(String email) {
        logger.info("[User Repo] Retrieving id for: " + email);
        String id = "";
        SqlRowSet rs = template.queryForRowSet(SQL_GET_USER_ID, email);
        while(rs.next()) {
            id = rs.getString("id");
        }
        return id;
    }

    public boolean userExists(String email) {
        logger.info("[User Repo] Checking existing user: " + email);
        int count = 0;
        SqlRowSet rs = template.queryForRowSet(SQL_COUNT_USER, email);
        while(rs.next()) {
            count = rs.getInt("count");
        }
        return count > 0;
    }
    
}
