package vttp.project.server.models;

public class Utils {

        public static final String[] PF_PARAMS = {"type", "breed", "size", "gender", "age", "name", "city"};
        public static final String[] PF_STRING_ATTRIBUTES = 
                {"url", "name", "species", "breeds.primary", "colors.primary", "age", "gender", "size", 
                "coat", "description", "contact.email", "contact.phone"};
        public static final String[] PF_RETURN_STRING_ATTRIBUTES = 
                {"url", "name", "species", "breed", "color", "age", "gender", "size", 
                "coat", "description", "email", "phone"};

        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String EMAIL = "email";
        public static final String PICTURE = "picture";
        public static final String PASSWORD = "password";
        public static final String GLOGIN = "google_login";
        public static final String USERID = "user_id";
        public static final String USERIMG = "user_img";
        public static final String POST = "post";
        public static final String TIMESTAMP = "timestamp";
        public static final String STATUS = "status";

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
                        INSERT INTO posts (id, user_id, name, user_img, post, picture, timestamp, status)
                        VALUES (?, ?, ?, ?, ?, ?, SYSDATE(), ?)
                        """;

        public static final String SQL_GET_POST_BY_USERID = """
                        SELECT * FROM posts WHERE user_id = ?
                        """;
}

