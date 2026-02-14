package service;

import config.Auth0Config;
import dao.UserDAO;
import enums.UserRole;
import model.User;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Auth0Service {

    // GitHub OAuth credentials from your config.properties
    private static final String GITHUB_CLIENT_ID = "Ov23lixD0hWxkdPzcNcz";
    private static final String GITHUB_CLIENT_SECRET = "aec4bbcd57209b716eeffd9a1bb3559e625c4b22";
    private static final String REDIRECT_URI = "http://localhost:8888/callback";

    // ===========================================
    // METHOD 1: Get Authorization URL (Now uses direct GitHub)
    // ===========================================
    public String getAuthorizationUrl() {
        // DIRECT GITHUB OAUTH - Bypassing Auth0 due to 503 error
        System.out.println("\n🔧 USING DIRECT GITHUB OAUTH:");
        System.out.println("   Client ID: " + GITHUB_CLIENT_ID);
        System.out.println("   Redirect URI: " + REDIRECT_URI);
        System.out.println("   Scope: repo,user");

        String url = "https://github.com/login/oauth/authorize?" +
                "client_id=" + GITHUB_CLIENT_ID +
                "&redirect_uri=" + REDIRECT_URI +
                "&scope=repo,user" +
                "&response_type=code";

        System.out.println("🔗 GitHub Auth URL: " + url);
        System.out.println("🔍 This bypasses Auth0 completely\n");
        return url;
    }

    // ===========================================
    // METHOD 2: Main authenticate method (Tries direct GitHub first)
    // ===========================================
    public User authenticate(String code) {
        System.out.println("\n=== AUTHENTICATION START ===");
        System.out.println("Code received: " + code);

        // Try direct GitHub OAuth first
        User user = authenticateWithDirectGitHub(code);
        if (user != null) {
            System.out.println("✅ Direct GitHub authentication successful");
            return user;
        }

        System.out.println("⚠️ Direct GitHub failed, trying Auth0 as fallback...");

        // Fall back to Auth0
        return authenticateWithAuth0(code);
    }

    // ===========================================
    // METHOD 3: Direct GitHub OAuth
    // ===========================================
    private User authenticateWithDirectGitHub(String code) {
        HttpURLConnection conn = null;

        try {
            System.out.println("\n=== DIRECT GITHUB AUTHENTICATION ===");

            // Step 1: Exchange code for access token
            URL tokenUrl = new URL("https://github.com/login/oauth/access_token");
            conn = (HttpURLConnection) tokenUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            String body = "client_id=" + URLEncoder.encode(GITHUB_CLIENT_ID, "UTF-8") +
                    "&client_secret=" + URLEncoder.encode(GITHUB_CLIENT_SECRET, "UTF-8") +
                    "&code=" + URLEncoder.encode(code, "UTF-8") +
                    "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8");

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = body.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            System.out.println("GitHub token response code: " + responseCode);

            if (responseCode != 200) {
                BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();
                System.err.println("❌ GitHub error: " + errorResponse.toString());
                return null;
            }

            // Read the response
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject tokenJson = new JSONObject(response.toString());
            String accessToken = tokenJson.getString("access_token");
            System.out.println("✅ Access token received");

            // Step 2: Get user info from GitHub
            return getUserFromGitHub(accessToken);

        } catch (Exception e) {
            System.err.println("❌ Direct GitHub auth error: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // ===========================================
    // METHOD 4: Get user info from GitHub
    // ===========================================
    private User getUserFromGitHub(String accessToken) {
        HttpURLConnection conn = null;

        try {
            System.out.println("\n=== FETCHING GITHUB USER INFO ===");

            // Get user profile
            URL userUrl = new URL("https://api.github.com/user");
            conn = (HttpURLConnection) userUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "token " + accessToken);
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();
            System.out.println("GitHub user API response: " + responseCode);

            if (responseCode != 200) {
                BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();
                System.err.println("❌ GitHub user error: " + errorResponse.toString());
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

            JSONObject userJson = new JSONObject(response.toString());

            String login = userJson.getString("login");
            String name = userJson.optString("name", login);
            String avatarUrl = userJson.optString("avatar_url", "");

            System.out.println("GitHub Username: " + login);
            System.out.println("Name: " + name);

            // Get primary email
            String email = getUserEmail(accessToken);
            System.out.println("Email: " + email);

            // Create user object
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setRole(UserRole.IT);
            user.setOauthProvider("GITHUB");
            user.setGithubUsername(login);
            user.setAccessToken(accessToken);
            user.setAvatarUrl(avatarUrl);

            // Save to database
            UserDAO userDAO = new UserDAO();
            User savedUser = userDAO.save(user);

            if (savedUser != null && savedUser.getId() > 0) {
                System.out.println("✅ User saved with ID: " + savedUser.getId());
                return savedUser;
            } else {
                System.err.println("❌ Failed to save user");
                return null;
            }

        } catch (Exception e) {
            System.err.println("❌ Error fetching GitHub user: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // ===========================================
    // METHOD 5: Get user email from GitHub
    // ===========================================
    private String getUserEmail(String accessToken) {
        HttpURLConnection conn = null;

        try {
            URL emailUrl = new URL("https://api.github.com/user/emails");
            conn = (HttpURLConnection) emailUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "token " + accessToken);
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                return "github-user@users.noreply.github.com";
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONArray emails = new JSONArray(response.toString());

            // Find primary email
            for (int i = 0; i < emails.length(); i++) {
                JSONObject emailObj = emails.getJSONObject(i);
                if (emailObj.getBoolean("primary")) {
                    return emailObj.getString("email");
                }
            }

            // Return first email if no primary found
            if (emails.length() > 0) {
                return emails.getJSONObject(0).getString("email");
            }

        } catch (Exception e) {
            System.err.println("⚠️ Could not fetch email: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }

        return "github-user@users.noreply.github.com";
    }

    // ===========================================
    // METHOD 6: Fallback Auth0 authentication
    // ===========================================
    private User authenticateWithAuth0(String code) {
        HttpURLConnection conn = null;
        HttpURLConnection userConn = null;

        try {
            System.out.println("\n=== AUTH0 AUTHENTICATION (FALLBACK) ===");
            System.out.println("Code received: " + code);

            // Exchange code for token
            String tokenUrl = "https://" + Auth0Config.DOMAIN + "/oauth/token";
            System.out.println("Token URL: " + tokenUrl);

            String body = "grant_type=authorization_code" +
                    "&client_id=" + Auth0Config.CLIENT_ID +
                    "&client_secret=" + Auth0Config.CLIENT_SECRET +
                    "&code=" + code +
                    "&redirect_uri=" + Auth0Config.REDIRECT_URI;

            URL url = new URL(tokenUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = body.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            System.out.println("Token response code: " + responseCode);

            if (responseCode != 200) {
                BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();
                System.err.println("❌ Auth0 error: " + errorResponse.toString());
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

            JSONObject tokenJson = new JSONObject(response.toString());
            String accessToken = tokenJson.getString("access_token");
            System.out.println("✅ Access token received");

            // Get user info from Auth0
            String userInfoUrl = "https://" + Auth0Config.DOMAIN + "/userinfo";
            URL userUrl = new URL(userInfoUrl);
            userConn = (HttpURLConnection) userUrl.openConnection();
            userConn.setRequestMethod("GET");
            userConn.setRequestProperty("Authorization", "Bearer " + accessToken);

            int userResponseCode = userConn.getResponseCode();
            if (userResponseCode != 200) {
                return null;
            }

            BufferedReader userReader = new BufferedReader(
                    new InputStreamReader(userConn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder userResponse = new StringBuilder();
            while ((line = userReader.readLine()) != null) {
                userResponse.append(line);
            }
            userReader.close();

            JSONObject userJson = new JSONObject(userResponse.toString());

            String name = userJson.optString("name", "GitHub User");
            String email = userJson.optString("email", "");
            String nickname = userJson.optString("nickname", "");
            String picture = userJson.optString("picture", "");

            User user = new User();
            user.setName(name);
            user.setEmail(email.isEmpty() ? nickname + "@users.noreply.github.com" : email);
            user.setRole(UserRole.IT);
            user.setOauthProvider("AUTH0-GITHUB");
            user.setGithubUsername(nickname);
            user.setAvatarUrl(picture);

            UserDAO userDAO = new UserDAO();
            return userDAO.save(user);

        } catch (Exception e) {
            System.err.println("❌ Auth0 error: " + e.getMessage());
            return null;
        } finally {
            if (conn != null) conn.disconnect();
            if (userConn != null) userConn.disconnect();
        }
    }
}