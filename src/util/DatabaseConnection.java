package util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
    private static final String URL  = "jdbc:postgresql://localhost:5432/NOTE";
    private static final String USER = "postgres";
    private static final String PASS = "1234";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
