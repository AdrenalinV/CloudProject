package ru.gb.server;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {
    public static final String DB_URL = "jdbc:h2:./DBSource";
    public static final String DB_DRIVER = "org.h2.Driver";
    static {
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("JDBC драйвер не найден");
            throw new RuntimeException("Database Driver initialization Error");
        }

    }
    private DataSource() {}
    public static Connection getConnection()throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

}
