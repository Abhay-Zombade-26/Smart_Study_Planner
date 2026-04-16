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
            System.err.println("❌ MySQL JDBC Driver not found!");
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

    public void initializeDatabase() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                email VARCHAR(255) UNIQUE NOT NULL,
                role ENUM('NORMAL', 'IT') DEFAULT 'NORMAL',
                oauth_provider ENUM('GOOGLE', 'GITHUB') NOT NULL,
                github_username VARCHAR(255),
                access_token TEXT,
                avatar_url TEXT,
                active_plan_id INT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createGoalsTable = """
            CREATE TABLE IF NOT EXISTS goals (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                repository_name VARCHAR(255),
                deadline DATE NOT NULL,
                difficulty ENUM('EASY', 'MODERATE', 'HARD') DEFAULT 'MODERATE',
                daily_hours INT DEFAULT 2,
                completion_percentage INT DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """;

        String createStudyTasksTable = """
            CREATE TABLE IF NOT EXISTS study_tasks (
                id INT AUTO_INCREMENT PRIMARY KEY,
                goal_id INT NOT NULL,
                task_date DATE NOT NULL,
                description TEXT NOT NULL,
                required_commit BOOLEAN DEFAULT FALSE,
                status ENUM('PENDING', 'COMPLETED', 'MISSED') DEFAULT 'PENDING',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE CASCADE
            )
        """;

        String createGitHubActivityTable = """
            CREATE TABLE IF NOT EXISTS github_activity (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                repo_name VARCHAR(255) NOT NULL,
                commit_count INT DEFAULT 0,
                last_commit_date DATE,
                streak_count INT DEFAULT 0,
                last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createGoalsTable);
            stmt.execute(createStudyTasksTable);
            stmt.execute(createGitHubActivityTable);
            System.out.println("✅ Database tables initialized successfully");
        } catch (SQLException e) {
            System.err.println("❌ Error initializing database tables:");
            e.printStackTrace();
        }
    }
}