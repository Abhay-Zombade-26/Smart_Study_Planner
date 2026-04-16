package db;

import config.AppConfig;

import java.sql.*;
import java.util.Properties;

public class DBConnection {
    private static DBConnection instance;
    private Connection connection;

    private DBConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found! Make sure mysql-connector-java is in pom.xml");
            e.printStackTrace();
        }
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("🔌 Attempting to connect to database...");
                System.out.println("   URL: " + AppConfig.DB_URL);
                System.out.println("   User: " + AppConfig.DB_USER);

                Properties props = new Properties();
                props.setProperty("user", AppConfig.DB_USER);
                props.setProperty("password", AppConfig.DB_PASSWORD);
                props.setProperty("useSSL", "false");
                props.setProperty("serverTimezone", "UTC");
                props.setProperty("allowPublicKeyRetrieval", "true");

                connection = DriverManager.getConnection(AppConfig.DB_URL, props);
                System.out.println("✅ Database connected successfully!");
            }
            return connection;
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed!");
            System.err.println("   Error: " + e.getMessage());
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Error Code: " + e.getErrorCode());
            throw e;
        }
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            boolean valid = conn != null && !conn.isClosed() && conn.isValid(5);
            if (valid) {
                System.out.println("✅ Database connection test: SUCCESS");
            } else {
                System.err.println("❌ Database connection test: FAILED");
            }
            return valid;
        } catch (SQLException e) {
            System.err.println("❌ Database connection test: FAILED with exception");
            e.printStackTrace();
            return false;
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔌 Database connection closed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}