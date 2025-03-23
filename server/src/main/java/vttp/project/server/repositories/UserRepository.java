package vttp.project.server.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import vttp.project.server.models.UserInfo;
import static vttp.project.server.models.Utils.*;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Repository
public class UserRepository {

    private static final Logger logger = Logger.getLogger(UserRepository.class.getName());
    private static final String IMG_URL = "https://pawdiaries-production.up.railway.app/user.png";

    @Autowired
    private JdbcTemplate template;

    public boolean insertNewUser(UserInfo user) {
        try {

            byte[] defaultImg = getDefaultPic();
            logger.info("[User Repo] Inserting user into MySQL");
            return template.update(SQL_INSERT_USER, user.getId(), user.getName(),
                    user.getEmail(), defaultImg, user.getPassword(), user.isGoogleLogin()) > 0;
        } catch (DataAccessException ex) {
            logger.warning(ex.getMessage());
            return false;
        }
    }

    public String getUserId(String email) {
        logger.info("[User Repo] Retrieving id for: " + email);
        String id = "";
        SqlRowSet rs = template.queryForRowSet(SQL_GET_USER_ID, email);
        while (rs.next()) {
            id = rs.getString("id");
        }
        return id;
    }

    public String getUserPassword(String email) {
        logger.info("[User Repo] Retrieving password for: " + email);
        String pw = "";
        SqlRowSet rs = template.queryForRowSet(SQL_GET_USER_PASSWORD, email);
        while (rs.next()) {
            pw = rs.getString("password");
        }
        return pw;
    }

    public String getUserPasswordById(String id) {
        logger.info("[User Repo] Retrieving password for: " + id);
        String pw = "";
        SqlRowSet rs = template.queryForRowSet(SQL_GET_PASSWORD_BY_ID, id);
        while (rs.next()) {
            pw = rs.getString("password");
        }
        return pw;
    }

    public boolean userExists(String email) {
        logger.info("[User Repo] Checking existing user: " + email);
        int count = 0;
        SqlRowSet rs = template.queryForRowSet(SQL_COUNT_USER, email);
        while (rs.next()) {
            count = rs.getInt("count");
        }
        return count > 0;
    }

    public boolean isGoogleLogin(String email) {
        logger.info("[User Repo] Checking Google user: " + email);
        boolean isGoogle = false;
        SqlRowSet rs = template.queryForRowSet(SQL_IS_GOOGLE_USER, email);
        while (rs.next()) {
            isGoogle = rs.getBoolean("google_login");
        }
        return isGoogle;
    }

    public Optional<UserInfo> getUserInfo(String id) {
        return template.query(
                SQL_GET_USER,
                (ResultSet rs) -> {
                    if (rs.next()) {
                        return Optional.of(UserInfo.populate(rs));
                    } else {
                        return Optional.empty();
                    }
                }, id);
    }

    public Optional<List<UserInfo>> getAllUsers(String id) {
        return template.query(
                SQL_GET_ALL_USER,
                (ResultSet rs) -> {
                    List<UserInfo> users = new LinkedList<>();
                    while (rs.next()) {
                        if (!rs.getString(ID).equals(id))
                            users.add(UserInfo.populate(rs));
                    }
                    if (users.isEmpty())
                        return Optional.empty();
                    return Optional.of(users);
                });
    }

    public String updatePic(MultipartFile file, String userId)
            throws IOException, RuntimeException {
        template.update(SQL_UPDATE_USERPIC, file.getBytes(), userId);
        return userId;
    }

    public String updateName(String name, String userId)
            throws RuntimeException {
        template.update(SQL_UPDATE_USERNAME, name, userId);
        return userId;
    }

    public String updatePassword(String password, String userId)
            throws RuntimeException {
        template.update(SQL_UPDATE_PASSWORD, password, userId);
        return userId;
    }

    public boolean resetPassword(String email, String newPassword)
            throws RuntimeException {
        return template.update(SQL_RESET_PASSWORD, newPassword, email) > 0;
    }

    private byte[] getDefaultPic() {
        RequestEntity<Void> req = RequestEntity.get(IMG_URL)
                .accept(MediaType.IMAGE_PNG)
                .build();
        RestTemplate template = new RestTemplate();
        ResponseEntity<byte[]> resp = template.exchange(req, byte[].class);
        byte[] img_file = resp.getBody();
        return img_file;
    }
}
