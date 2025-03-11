package vttp.project.server.models;

public class Utils {

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

        public static final String SQL_UPLOAD_POST = """
                        INSERT INTO posts (id, user_id, post, picture, timestamp)
                        VALUES (?, ?, ?, ?, SYSDATE())
                        """;
}

