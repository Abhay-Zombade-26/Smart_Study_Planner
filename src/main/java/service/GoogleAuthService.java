package service;

import config.AppConfig;
import dao.UserDAO;
import enums.UserRole;
import model.User;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GoogleAuthService implements AuthService {

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";
    private static final String REDIRECT_URI = "http://localhost:8888/google-callback";

    @Override
    public String getAuthorizationUrl() {
        String url = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=" + AppConfig.GOOGLE_CLIENT_ID +
                "&redirect_uri=" + REDIRECT_URI +
                "&response_type=code" +
                "&scope=email%20profile" +
                "&access_type=offline" +
                "&prompt=select_account";
        
        System.out.println("🔗 Google Auth URL: " + url);
        return url;
    }

    @Override
    public User authenticate(String code) {
        try {
            System.out.println("\n=== GOOGLE AUTH ===");
            System.out.println("1. Code: " + code);
            
            String accessToken = getAccessToken(code);
            System.out.println("2. ✅ Got access token");
            
            JSONObject userInfo = fetchUserInfo(accessToken);
            System.out.println("3. ✅ Got user info");
            
            String email = userInfo.getString("email");
            String name = userInfo.optString("name", email.split("@")[0]);
            String picture = userInfo.optString("picture", null);
            
            System.out.println("4. Email: " + email);
            System.out.println("5. Name: " + name);
            
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setRole(UserRole.NORMAL);
            user.setOauthProvider("GOOGLE");
            user.setAccessToken(accessToken);
            user.setAvatarUrl(picture);
            
            UserDAO userDAO = new UserDAO();
            User savedUser = userDAO.save(user);
            
            if (savedUser == null) {
                savedUser = userDAO.findByEmail(email);
            }
            
            System.out.println("6. ✅ User saved with ID: " + (savedUser != null ? savedUser.getId() : "null"));
            return savedUser;
            
        } catch (Exception e) {
            System.err.println("❌ Google auth error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private String getAccessToken(String code) throws Exception {
        String body = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8.name()) +
                "&client_id=" + URLEncoder.encode(AppConfig.GOOGLE_CLIENT_ID, StandardCharsets.UTF_8.name()) +
                "&client_secret=" + URLEncoder.encode(AppConfig.GOOGLE_CLIENT_SECRET, StandardCharsets.UTF_8.name()) +
                "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8.name()) +
                "&grant_type=authorization_code";
        
        URL url = new URL(TOKEN_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorResponse.append(line);
            }
            errorReader.close();
            throw new Exception("Token exchange failed: " + errorResponse.toString());
        }
        
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        JSONObject json = new JSONObject(response.toString());
        return json.getString("access_token");
    }
    
    private JSONObject fetchUserInfo(String accessToken) throws Exception {
        URL url = new URL(USER_INFO_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Failed to get user info: " + responseCode);
        }
        
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        return new JSONObject(response.toString());
    }

    @Override
    public User getUserInfo(String accessToken) {
        try {
            JSONObject userInfo = fetchUserInfo(accessToken);
            User user = new User();
            user.setEmail(userInfo.getString("email"));
            user.setName(userInfo.optString("name", userInfo.getString("email").split("@")[0]));
            user.setAvatarUrl(userInfo.optString("picture", null));
            return user;
        } catch (Exception e) {
            System.err.println("❌ Failed to get user info: " + e.getMessage());
            return null;
        }
    }
}