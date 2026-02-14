package service;

import dao.UserDAO;
import enums.UserRole;
import model.User;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class DirectGitHubAuthService {
    
    private static final String CLIENT_ID = "Ov23lixD0hWxkdPzcNcz";
    private static final String CLIENT_SECRET = "aec4bbcd57209b716eeffd9a1bb3559e625c4b22";
    private static final String REDIRECT_URI = "http://localhost:8888/callback";
    
    public String getAuthorizationUrl() {
        try {
            String url = "https://github.com/login/oauth/authorize?" +
                "client_id=" + CLIENT_ID +
                "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8") +
                "&scope=" + URLEncoder.encode("repo read:user user:email", "UTF-8");
            
            System.out.println("ðŸ”— Direct GitHub Auth URL: " + url);
            return url;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public User authenticate(String code) {
        HttpURLConnection conn = null;
        
        try {
            System.out.println("\n=== DIRECT GITHUB AUTH ===");
            System.out.println("1. Code received: " + code);
            
            // Exchange code for token
            String tokenUrl = "https://github.com/login/oauth/access_token";
            String body = "client_id=" + CLIENT_ID +
                         "&client_secret=" + CLIENT_SECRET +
                         "&code=" + code +
                         "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8");
            
            URL url = new URL(tokenUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
            
            int responseCode = conn.getResponseCode();
            System.out.println("2. Token response code: " + responseCode);
            
            if (responseCode != 200) {
                BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();
                System.err.println("âŒ Token error: " + errorResponse.toString());
                return null;
            }
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            System.out.println("3. Token response: " + response.toString());
            
            JSONObject tokenJson = new JSONObject(response.toString());
            String accessToken = tokenJson.getString("access_token");
            System.out.println("4. âœ… Access token received");
            System.out.println("   Token length: " + accessToken.length() + " chars");
            
            // Get user info
            String userUrl = "https://api.github.com/user";
            HttpURLConnection userConn = (HttpURLConnection) new URL(userUrl).openConnection();
            userConn.setRequestMethod("GET");
            userConn.setRequestProperty("Authorization", "Bearer " + accessToken);
            userConn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            
            BufferedReader userReader = new BufferedReader(
                new InputStreamReader(userConn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder userResponse = new StringBuilder();
            while ((line = userReader.readLine()) != null) {
                userResponse.append(line);
            }
            userReader.close();
            
            JSONObject userJson = new JSONObject(userResponse.toString());
            
            String name = userJson.optString("name", userJson.getString("login"));
            String email = userJson.optString("email", "");
            String login = userJson.getString("login");
            String avatar = userJson.getString("avatar_url");
            
            System.out.println("5. User details:");
            System.out.println("   Name: " + name);
            System.out.println("   Login: " + login);
            
            // Get emails if primary email is private
            if (email.isEmpty()) {
                try {
                    String emailsUrl = "https://api.github.com/user/emails";
                    HttpURLConnection emailsConn = (HttpURLConnection) new URL(emailsUrl).openConnection();
                    emailsConn.setRequestMethod("GET");
                    emailsConn.setRequestProperty("Authorization", "Bearer " + accessToken);
                    
                    BufferedReader emailsReader = new BufferedReader(
                        new InputStreamReader(emailsConn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder emailsResponse = new StringBuilder();
                    while ((line = emailsReader.readLine()) != null) {
                        emailsResponse.append(line);
                    }
                    emailsReader.close();
                    
                    JSONArray emailsArray = new JSONArray(emailsResponse.toString());
                    for (int i = 0; i < emailsArray.length(); i++) {
                        JSONObject emailObj = emailsArray.getJSONObject(i);
                        if (emailObj.getBoolean("primary")) {
                            email = emailObj.getString("email");
                            break;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Could not fetch emails: " + e.getMessage());
                }
            }
            
            User user = new User();
            user.setName(name);
            user.setEmail(email.isEmpty() ? login + "@users.noreply.github.com" : email);
            user.setRole(UserRole.IT);
            user.setOauthProvider("DIRECT-GITHUB");
            user.setGithubUsername(login);
            user.setAccessToken(accessToken);
            user.setAvatarUrl(avatar);
            
            System.out.println("6. Saving user to database...");
            
            UserDAO userDAO = new UserDAO();
            User savedUser = userDAO.save(user);
            
            if (savedUser != null && savedUser.getId() > 0) {
                System.out.println("7. âœ… User saved with ID: " + savedUser.getId());
                System.out.println("   Access token saved: " + (savedUser.getAccessToken() != null ? "YES" : "NO"));
                return savedUser;
            } else {
                System.err.println("âŒ Failed to save user");
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("âŒ GitHub auth error: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}