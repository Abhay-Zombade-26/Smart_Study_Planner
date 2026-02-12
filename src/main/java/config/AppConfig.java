package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties props = new Properties();

    // OAuth Configuration
    public static final String GOOGLE_CLIENT_ID;
    public static final String GOOGLE_CLIENT_SECRET;
    public static final String GOOGLE_REDIRECT_URI = "http://localhost:8888/callback";

    public static final String GITHUB_CLIENT_ID;
    public static final String GITHUB_CLIENT_SECRET;
    public static final String GITHUB_REDIRECT_URI = "http://localhost:8888/github-callback";

    // Database Configuration
    public static final String DB_URL;
    public static final String DB_USER;
    public static final String DB_PASSWORD;

    static {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                props.load(input);
            } else {
                // Load default properties
                props.setProperty("google.client.id", "YOUR_GOOGLE_CLIENT_ID");
                props.setProperty("google.client.secret", "YOUR_GOOGLE_CLIENT_SECRET");
                props.setProperty("github.client.id", "YOUR_GITHUB_CLIENT_ID");
                props.setProperty("github.client.secret", "YOUR_GITHUB_CLIENT_SECRET");
                props.setProperty("db.url", "jdbc:mysql://localhost:3306/smart_study_planner");
                props.setProperty("db.user", "root");
                props.setProperty("db.password", "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        GOOGLE_CLIENT_ID = props.getProperty("google.client.id");
        GOOGLE_CLIENT_SECRET = props.getProperty("google.client.secret");
        GITHUB_CLIENT_ID = props.getProperty("github.client.id");
        GITHUB_CLIENT_SECRET = props.getProperty("github.client.secret");
        DB_URL = props.getProperty("db.url");
        DB_USER = props.getProperty("db.user");
        DB_PASSWORD = props.getProperty("db.password");
    }

    public static void initialize() {
        System.out.println("AppConfig initialized");
    }
}