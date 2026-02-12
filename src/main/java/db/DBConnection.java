package db;

import config.AppConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {
    private static DBConnection instance;
    private Connection connection;

    private DBConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
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
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(
                    AppConfig.DB_URL,
                    AppConfig.DB_USER,
                    AppConfig.DB_PASSWORD
            );
        }
        return connection;
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
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

        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createGoalsTable);
            stmt.execute(createStudyTasksTable);
            stmt.execute(createGitHubActivityTable);
            System.out.println("Database tables initialized successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}