package service;
//i am on main branch

import config.AppConfig;
import dao.UserDAO;
import enums.UserRole;
import model.User;
import org.json.JSONObject;
import org.json.JSONException;
import util.HttpUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Auth0Service  implements AuthService {
    
    private static final String GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String GITHUB_USER_URL = "https://api.github.com/user";
    
    @Override
    public String getAuthorizationUrl() {
        String clientId = AppConfig.GITHUB_CLIENT_ID.trim();
        String redirectUri = AppConfig.GITHUB_REDIRECT_URI;
        
        String url = "https://github.com/login/oauth/authorize?" +
               "client_id=" + clientId +
               "&redirect_uri=" + redirectUri +
               "&scope=repo%20read:user%20user:email" +
               "&response_type=code";
        
        System.out.println("🔗 Auth URL: " + url);
        return url;
    }
    
    @Override
    public User authenticate(String authCode) {
        try {
            System.out.println("=== GITHUB AUTH DEBUG ===");
            System.out.println("1. Auth Code received: " + authCode);
            
            // Clean the client secret - REMOVE ANY TRAILING + SIGNS!
            String clientSecret = AppConfig.GITHUB_CLIENT_SECRET.trim();
            if (clientSecret.endsWith("+")) {
                clientSecret = clientSecret.substring(0, clientSecret.length() - 1);
                System.out.println("⚠️ Removed trailing + from client secret");
            }
            
            System.out.println("2. Client ID: " + maskString(AppConfig.GITHUB_CLIENT_ID));
            System.out.println("3. Client Secret: " + maskString(clientSecret));
            System.out.println("4. Redirect URI: " + AppConfig.GITHUB_REDIRECT_URI);
            
            String accessToken = exchangeCodeForToken(authCode, clientSecret);
            System.out.println("5. ✅ Token received successfully!");
            
            return getUserInfo(accessToken);
        } catch (Exception e) {
            System.err.println("❌ Authentication failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private String maskString(String str) {
        if (str == null || str.length() < 8) return "****";
        return str.substring(0, 4) + "****" + str.substring(str.length() - 4);
    }
    
    private String exchangeCodeForToken(String code, String clientSecret) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("client_id", AppConfig.GITHUB_CLIENT_ID.trim());
        params.put("client_secret", clientSecret); // Use cleaned secret
        params.put("code", code);
        params.put("redirect_uri", AppConfig.GITHUB_REDIRECT_URI);
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        
        // Manually build the body to avoid URL encoding issues
        StringBuilder body = new StringBuilder();
        body.append("client_id=").append(URLEncoder.encode(params.get("client_id"), StandardCharsets.UTF_8.name()));
        body.append("&client_secret=").append(URLEncoder.encode(params.get("client_secret"), StandardCharsets.UTF_8.name()));
        body.append("&code=").append(URLEncoder.encode(params.get("code"), StandardCharsets.UTF_8.name()));
        body.append("&redirect_uri=").append(URLEncoder.encode(params.get("redirect_uri"), StandardCharsets.UTF_8.name()));
        
        System.out.println("\n--- Token Request ---");
        System.out.println("POST " + GITHUB_TOKEN_URL);
        System.out.println("Body: " + body);
        
        String response = HttpUtil.sendPost(GITHUB_TOKEN_URL, headers, body.toString());
        System.out.println("Response: '" + response + "'");
        System.out.println("--------------------\n");
        
        // Try to parse as JSON
        try {
            JSONObject json = new JSONObject(response);
            if (json.has("access_token")) {
                return json.getString("access_token");
            } else if (json.has("error")) {
                String error = json.getString("error");
                String errorDescription = json.optString("error_description", "No description");
                throw new IOException("GitHub OAuth error: " + error + " - " + errorDescription);
            }
        } catch (JSONException e) {
            // Parse as query string
            String[] pairs = response.split("&");
            Map<String, String> queryParams = new HashMap<>();
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    try {
                        String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8.name());
                        String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name());
                        queryParams.put(key, value);
                    } catch (Exception ex) {
                        queryParams.put(keyValue[0], keyValue[1]);
                    }
                }
            }
            
            if (queryParams.containsKey("access_token")) {
                return queryParams.get("access_token");
            } else if (queryParams.containsKey("error")) {
                String error = queryParams.get("error");
                String errorDescription = queryParams.getOrDefault("error_description", "No description");
                throw new IOException("GitHub OAuth error: " + error + " - " + errorDescription);
            }
        }
        
        throw new IOException("No access_token found in response: " + response);
    }
    
    @Override
    public User getUserInfo(String accessToken) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + accessToken);
            headers.put("Accept", "application/vnd.github.v3+json");
            headers.put("User-Agent", "Smart-Study-Planner");
            
            System.out.println("\n--- User Info Request ---");
            String userResponse = HttpUtil.sendGet(GITHUB_USER_URL, headers);
            System.out.println("User Response received");
            
            JSONObject userInfo = new JSONObject(userResponse);
            System.out.println("✅ GitHub Username: " + userInfo.getString("login"));
            
            String email = getEmail(accessToken, userInfo);
            
            User user = new User();
            String name = userInfo.optString("name");
            if (name == null || name.trim().isEmpty() || name.equals("null")) {
                name = userInfo.getString("login");
            }
            user.setName(name);
            user.setEmail(email);
            user.setRole(UserRole.IT);
            user.setOauthProvider("GITHUB");
            user.setGithubUsername(userInfo.getString("login"));
            user.setAccessToken(accessToken);
            user.setAvatarUrl(userInfo.getString("avatar_url"));
            
            UserDAO userDAO = new UserDAO();
            User savedUser = userDAO.save(user);
            System.out.println("✅ User saved: " + savedUser.getEmail());
            
            return savedUser;
        } catch (Exception e) {
            System.err.println("❌ Failed to get user info: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private String getEmail(String accessToken, JSONObject userInfo) throws IOException {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + accessToken);
            headers.put("Accept", "application/vnd.github.v3+json");
            headers.put("User-Agent", "Smart-Study-Planner");
            
            String emailResponse = HttpUtil.sendGet("https://api.github.com/user/emails", headers);
            org.json.JSONArray emails = new org.json.JSONArray(emailResponse);
            
            for (int i = 0; i < emails.length(); i++) {
                JSONObject emailObj = emails.getJSONObject(i);
                if (emailObj.getBoolean("primary")) {
                    return emailObj.getString("email");
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Could not fetch emails: " + e.getMessage());
        }
        
        String email = userInfo.optString("email");
        if (email != null && !email.isEmpty() && !email.equals("null")) {
            return email;
        }
        
        return userInfo.getString("login") + "@users.noreply.github.com";
    }
}