package vttp.project.server.models;

public class Utils {

        public static final String[] PF_PARAMS = {"type", "breed", "size", "gender", "age", "name", "location"};
        public static final String[] PF_PARAMS_FILTERED = 
                {"type", "breed", "size", "gender", "age", "name", "location", "pf_ids"};
        public static final String[] PF_STRING_ATTRIBUTES = 
                {"id", "url", "name", "species", "breeds.primary", "colors.primary", "age", "gender", "size", 
                "coat", "description", "contact.email", "contact.phone"};
        public static final String[] PF_RETURN_STRING_ATTRIBUTES = 
                {"id", "url", "name", "species", "breed", "color", "age", "gender", "size", 
                "coat", "description", "email", "phone"};

        public static final String C_PF = "pf_results";
        public static final String C_USER = "user_details";
        public static final String C_COMMENT = "post_comments";

        public static final String F_SAVED_PF = "saved_pf";
        public static final String F_SAVED_POSTS = "saved_posts";

        public static final String R_PF_TYPES = "pf_types";
        public static final String R_PF_BREEDS = "pf_breeds";

        public static final String ID = "id";
        public static final String USERID = "user_id";
        public static final String POSTID = "post_id";
        public static final String COMMENTID = "c_id";
        public static final String PFID = "pf_id";
        public static final String NAME = "name";
        public static final String EMAIL = "email";
        public static final String PICTURE = "picture";
        public static final String TYPE = "type";
        public static final String FILE = "file";
        public static final String FILES = "files";
        public static final String PASSWORD = "password";
        public static final String GLOGIN = "google_login";
        public static final String USERIMG = "user_img";
        public static final String POST = "post";
        public static final String TIMESTAMP = "timestamp";
        public static final String STATUS = "status";
        public static final String COMMENT = "comment";
        public static final String COMMENTS = "comments";
        public static final String EDITED = "edited";

        public static final String SQL_INSERT_USER = """
                        INSERT INTO users (id, name, email, picture, password, google_login)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """;

        public static final String SQL_COUNT_USER = """
                        SELECT COUNT(*) AS count FROM users WHERE email = ?
                        """;

        public static final String SQL_GET_USER_ID = """
                        SELECT id FROM users WHERE email = ?
                        """;

        public static final String SQL_GET_USER_PASSWORD = """
                        SELECT password FROM users WHERE email = ?
                        """;
        
        public static final String SQL_IS_GOOGLE_USER = """
                        SELECT google_login FROM users WHERE email = ?
                        """;
        
        public static final String SQL_GET_USER = """
                        SELECT * FROM users WHERE id = ?
                        """;

        public static final String SQL_INSERT_POST = """
                        INSERT INTO posts (id, user_id, name, user_img, post, timestamp, status)
                        VALUES (?, ?, ?, ?, ?, SYSDATE(), ?)
                        """;

        public static final String SQL_GET_PUBLIC_POSTS = """
                        SELECT * FROM posts WHERE status = 'public' 
                        ORDER BY timestamp DESC
                        """;

        public static final String SQL_GET_POST_BY_USERID = """
                        SELECT * FROM posts WHERE user_id = ?
                        """;

        public static final String SQL_INSERT_MEDIA_FILE = """
                        INSERT INTO media_files (id, post_id, type, file)
                        VALUES (?, ?, ?, ?)
                        """;
        
        public static final String SQL_GET_MEDIA_FILES_BY_POSTID = """
                        SELECT * FROM media_files WHERE post_id = ?
                        """;

        public static final String SQL_DELETE_POST_BY_ID = """
                        DELETE FROM posts WHERE id = ?
                        """;

        public static final String SQL_UPDATE_POST = """
                        UPDATE posts SET post = ?, timestamp = SYSDATE()
                        WHERE id = ?
                        """;

        public static final String SQL_UPDATE_USERPIC = """
                        UPDATE users SET picture = ?
                        WHERE id = ?
                        """;

        public static final String SQL_UPDATE_USERNAME = """
                        UPDATE users SET name = ?
                        WHERE id = ?
                        """;
        
        public static final String SQL_UPDATE_PASSWORD = """
                        UPDATE users SET password = ?
                        WHERE id = ?
                        """;

        public static final String SQL_GET_PASSWORD_BY_ID = """
                        SELECT password FROM users WHERE id = ?
                        """;
}

