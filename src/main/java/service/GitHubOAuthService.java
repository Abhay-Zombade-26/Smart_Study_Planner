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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class GitHubOAuthService {
    
    private static String CLIENT_ID;
    private static String CLIENT_SECRET;
    private static final String REDIRECT_URI = "http://localhost:8888/callback";
    
    static {
        try {
            Properties props = new Properties();
            try (InputStream input = new FileInputStream("src/main/resources/config.properties")) {
                props.load(input);
                CLIENT_ID = props.getProperty("github.client.id");
                CLIENT_SECRET = props.getProperty("github.client.secret");
                System.out.println("? GitHubOAuthService initialized with Client ID: " + CLIENT_ID);
            }
        } catch (Exception e) {
            System.err.println("? Failed to load config: " + e.getMessage());
        }
    }
    
    public String getAuthorizationUrl() {
        String url = "https://github.com/login/oauth/authorize?" +
                "client_id=" + CLIENT_ID +
                "&redirect_uri=" + REDIRECT_URI +
                "&scope=repo,user" +
                "&response_type=code";
        System.out.println("?? Auth URL: " + url);
        return url;
    }
    
    public User authenticate(String code) {
        try {
            System.out.println("1. Getting access token...");
            String accessToken = getAccessToken(code);
            System.out.println("2. Got access token");
            
            System.out.println("3. Getting user info...");
            JSONObject userJson = getUserInfo(accessToken);
            String login = userJson.getString("login");
            System.out.println("4. GitHub username: " + login);
            
            System.out.println("5. Getting email...");
            String email = getUserEmail(accessToken);
            System.out.println("6. Email: " + email);
            
            User user = new User();
            user.setName(userJson.optString("name", login));
            user.setEmail(email);
            user.setRole(UserRole.IT);
            user.setOauthProvider("GITHUB");
            user.setGithubUsername(login);
            user.setAccessToken(accessToken);
            user.setAvatarUrl(userJson.optString("avatar_url"));
            
            System.out.println("7. Saving user to database...");
            UserDAO userDAO = new UserDAO();
            User savedUser = userDAO.save(user);
            System.out.println("8. User saved with ID: " + (savedUser != null ? savedUser.getId() : "null"));
            
            return savedUser;
            
        } catch (Exception e) {
            System.err.println("? Authentication error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private String getAccessToken(String code) throws Exception {
        URL url = new URL("https://github.com/login/oauth/access_token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        
        String body = "client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8") +
                "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, "UTF-8") +
                "&code=" + URLEncoder.encode(code, "UTF-8") +
                "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8");
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Token exchange failed: " + responseCode);
        }
        
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        return new JSONObject(response.toString()).getString("access_token");
    }
    
    private JSONObject getUserInfo(String accessToken) throws Exception {
        URL url = new URL("https://api.github.com/user");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "token " + accessToken);
        conn.setRequestProperty("Accept", "application/json");
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Failed to get user info: " + responseCode);
        }
        
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        return new JSONObject(response.toString());
    }
    
    private String getUserEmail(String accessToken) throws Exception {
        URL url = new URL("https://api.github.com/user/emails");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "token " + accessToken);
        conn.setRequestProperty("Accept", "application/json");
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            return "github-user@users.noreply.github.com";
        }
        
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        JSONArray emails = new JSONArray(response.toString());
        for (int i = 0; i < emails.length(); i++) {
            JSONObject emailObj = emails.getJSONObject(i);
            if (emailObj.getBoolean("primary")) {
                return emailObj.getString("email");
            }
        }
        return "github-user@users.noreply.github.com";
    }
    
    public List<Map<String, String>> getRepositories(String accessToken) {
        List<Map<String, String>> repos = new ArrayList<>();
        try {
            URL url = new URL("https://api.github.com/user/repos?sort=updated&per_page=100");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "token " + accessToken);
            conn.setRequestProperty("Accept", "application/json");
            
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Failed to get repos: " + responseCode);
                return repos;
            }
            
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            JSONArray reposArray = new JSONArray(response.toString());
            for (int i = 0; i < reposArray.length(); i++) {
                JSONObject repo = reposArray.getJSONObject(i);
                Map<String, String> repoInfo = new HashMap<>();
                repoInfo.put("name", repo.getString("full_name"));
                repoInfo.put("private", String.valueOf(repo.getBoolean("private")));
                repoInfo.put("updated_at", repo.getString("updated_at"));
                repoInfo.put("html_url", repo.getString("html_url"));
                repoInfo.put("description", repo.optString("description", "No description"));
                repos.add(repoInfo);
            }
            System.out.println("?? Loaded " + repos.size() + " repositories");
            
        } catch (Exception e) {
            System.err.println("Error fetching repos: " + e.getMessage());
        }
        return repos;
    }
}
