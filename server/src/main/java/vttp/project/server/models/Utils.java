package vttp.project.server.models;

public class Utils {

    public static final String SQL_INSERT_USER = """
            INSERT INTO users (id, name, email, picture)
            VALUES (?, ?, ?, ?)
            """;
    
    public static final String SQL_COUNT_USER = """
            SELECT COUNT(*) AS count FROM users WHERE email = ?
            """;

    public static final String SQL_GET_USER_ID = """
            SELECT id FROM users WHERE email = ?
            """;
}
