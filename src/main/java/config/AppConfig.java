package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties props = new Properties();

    // OAuth Configuration
    public static final String GOOGLE_CLIENT_ID;
    public static final String GOOGLE_CLIENT_SECRET;
    public static final String GOOGLE_REDIRECT_URI = "http://localhost:8888/google-callback";

    public static final String GITHUB_CLIENT_ID;
    public static final String GITHUB_CLIENT_SECRET;
    public static final String GITHUB_REDIRECT_URI = "http://localhost:8888/callback";

    // AI Configuration
    public static final String AI_API_KEY;
    public static final String AI_API_URL;
    public static final String AI_MODEL;
    public static final int AI_MAX_TOKENS;

    // Database Configuration
    public static final String DB_URL;
    public static final String DB_USER;
    public static final String DB_PASSWORD;

    static {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                props.load(input);
            } else {
                // Default properties
                props.setProperty("google.client.id", "YOUR_GOOGLE_CLIENT_ID");
                props.setProperty("google.client.secret", "YOUR_GOOGLE_CLIENT_SECRET");
                props.setProperty("github.client.id", "YOUR_GITHUB_CLIENT_ID");
                props.setProperty("github.client.secret", "YOUR_GITHUB_CLIENT_SECRET");
                props.setProperty("ai.api.key", "");
                props.setProperty("ai.api.url", "https://api.openai.com/v1/chat/completions");
                props.setProperty("ai.model", "gpt-3.5-turbo");
                props.setProperty("ai.max.tokens", "2000");
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

        AI_API_KEY = props.getProperty("ai.api.key", "");
        AI_API_URL = props.getProperty("ai.api.url", "https://api.openai.com/v1/chat/completions");
        AI_MODEL = props.getProperty("ai.model", "gpt-3.5-turbo");
        AI_MAX_TOKENS = Integer.parseInt(props.getProperty("ai.max.tokens", "2000"));

        DB_URL = props.getProperty("db.url");
        DB_USER = props.getProperty("db.user");
        DB_PASSWORD = props.getProperty("db.password");
    }

    public static void initialize() {
        System.out.println("AppConfig initialized");
        if (AI_API_KEY != null && !AI_API_KEY.isEmpty() && !AI_API_KEY.equals("YOUR_OPENAI_API_KEY_HERE")) {
            System.out.println("🤖 AI Integration: ENABLED");
        } else {
            System.out.println("⚠️ AI Integration: DISABLED (API key not configured)");
        }
    }
}