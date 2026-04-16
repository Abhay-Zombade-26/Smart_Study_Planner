package service;

import config.AppConfig;
import dao.UserDAO;
import enums.UserRole;
import model.User;
import org.json.JSONObject;
import util.HttpUtil;
import util.JsonUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GoogleAuthService implements AuthService {

    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";
    private static final String REDIRECT_URI = "http://localhost:8888/google-callback";

    @Override
    public String getAuthorizationUrl() {
        String clientId = AppConfig.GOOGLE_CLIENT_ID.trim();

        String url = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=" + clientId +
                "&redirect_uri=" + REDIRECT_URI +
                "&response_type=code" +
                "&scope=openid%20email%20profile" +
                "&access_type=offline" +
                "&prompt=select_account" +
                "&authuser=-1" +
                "&include_granted_scopes=true";

        System.out.println("🔗 Google Auth URL: " + url);
        return url;
    }

    @Override
    public User authenticate(String authCode) {
        try {
            System.out.println("\n=== GOOGLE AUTH DEBUG ===");
            System.out.println("1. Auth Code received: " + authCode);
            System.out.println("2. Client ID: " + maskString(AppConfig.GOOGLE_CLIENT_ID));
            System.out.println("3. Redirect URI: " + REDIRECT_URI);

            String accessToken = exchangeCodeForToken(authCode);
            System.out.println("4. ✅ Token received successfully!");

            return getUserInfo(accessToken);

        } catch (IOException e) {
            System.err.println("❌ Authentication failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String maskString(String str) {
        if (str == null || str.length() < 8) return "****";
        return str.substring(0, 4) + "****" + str.substring(str.length() - 4);
    }

    private String exchangeCodeForToken(String code) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("client_id", AppConfig.GOOGLE_CLIENT_ID.trim());
        params.put("client_secret", AppConfig.GOOGLE_CLIENT_SECRET.trim());
        params.put("redirect_uri", REDIRECT_URI);
        params.put("grant_type", "authorization_code");

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        String body = HttpUtil.encodeParams(params);
        System.out.println("\n--- Token Request ---");
        System.out.println("POST " + GOOGLE_TOKEN_URL);

        String response = HttpUtil.sendPost(GOOGLE_TOKEN_URL, headers, body);
        System.out.println("Response received");
        System.out.println("--------------------\n");

        JSONObject json = JsonUtil.parseObject(response);

        if (json.has("error")) {
            String error = json.getString("error");
            String errorDescription = json.optString("error_description", "No description");
            throw new IOException("Google OAuth error: " + error + " - " + errorDescription);
        }

        return json.getString("access_token");
    }

    @Override
    public User getUserInfo(String accessToken) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + accessToken);

            System.out.println("\n--- User Info Request ---");
            System.out.println("GET " + GOOGLE_USERINFO_URL);

            String response = HttpUtil.sendGet(GOOGLE_USERINFO_URL, headers);
            System.out.println("Response received");
            System.out.println("------------------------\n");

            JSONObject userInfo = JsonUtil.parseObject(response);

            System.out.println("✅ Google User Info:");
            System.out.println("   - Name: " + userInfo.getString("name"));
            System.out.println("   - Email: " + userInfo.getString("email"));

            User user = new User();
            user.setName(userInfo.getString("name"));
            user.setEmail(userInfo.getString("email"));
            user.setRole(UserRole.NORMAL);
            user.setOauthProvider("GOOGLE");
            user.setAvatarUrl(userInfo.getString("picture"));

            System.out.println("🔍 Before saving - User object ID: " + user.getId());

            UserDAO userDAO = new UserDAO();
            User savedUser = userDAO.save(user);

            if (savedUser == null) {
                System.err.println("❌ Failed to save user to database");

                // Try to find existing user
                savedUser = userDAO.findByEmail(user.getEmail());
                if (savedUser != null) {
                    System.out.println("✅ Found existing user with ID: " + savedUser.getId());
                    return savedUser;
                }
                return null;
            }

            System.out.println("✅ After saving - User ID: " + savedUser.getId());
            System.out.println("✅ User saved to database: " + savedUser.getEmail());
            return savedUser;

        } catch (IOException e) {
            System.err.println("❌ Failed to get user info: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}