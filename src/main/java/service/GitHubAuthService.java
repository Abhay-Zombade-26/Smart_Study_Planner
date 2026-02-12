package service;

import config.AppConfig;
import dao.UserDAO;
import enums.UserRole;
import model.User;
import org.json.JSONObject;
import util.HttpUtil;
import util.JsonUtil;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class GitHubAuthService implements AuthService {

    private static final String GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String GITHUB_USER_URL = "https://api.github.com/user";

    @Override
    public String getAuthorizationUrl() {
        return "https://github.com/login/oauth/authorize?" +
                "client_id=" + AppConfig.GITHUB_CLIENT_ID +
                "&redirect_uri=" + AppConfig.GITHUB_REDIRECT_URI +
                "&scope=repo%20read:user%20user:email";
    }

    @Override
    public User authenticate(String authCode) {
        try {
            String accessToken = exchangeCodeForToken(authCode);
            return getUserInfo(accessToken);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String exchangeCodeForToken(String code) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("client_id", AppConfig.GITHUB_CLIENT_ID);
        params.put("client_secret", AppConfig.GITHUB_CLIENT_SECRET);
        params.put("code", code);

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        String body = HttpUtil.encodeParams(params);
        String response = HttpUtil.sendPost(GITHUB_TOKEN_URL, headers, body);

        JSONObject json = JsonUtil.parseObject(response);
        return json.getString("access_token");
    }

    @Override
    public User getUserInfo(String accessToken) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + accessToken);
            headers.put("Accept", "application/vnd.github.v3+json");

            // Get user info
            String userResponse = HttpUtil.sendGet(GITHUB_USER_URL, headers);
            JSONObject userInfo = JsonUtil.parseObject(userResponse);

            // Get primary email
            String email = getPrimaryEmail(accessToken);

            User user = new User();
            user.setName(userInfo.getString("name") != null && !userInfo.getString("name").equals("null") ?
                    userInfo.getString("name") : userInfo.getString("login"));
            user.setEmail(email);
            user.setRole(UserRole.IT);
            user.setOauthProvider("GITHUB");
            user.setGithubUsername(userInfo.getString("login"));
            user.setAccessToken(accessToken);
            user.setAvatarUrl(userInfo.getString("avatar_url"));

            // Save to database
            UserDAO userDAO = new UserDAO();
            return userDAO.save(user);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getPrimaryEmail(String accessToken) throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        headers.put("Accept", "application/vnd.github.v3+json");

        String response = HttpUtil.sendGet("https://api.github.com/user/emails", headers);
        org.json.JSONArray emails = new org.json.JSONArray(response);

        for (int i = 0; i < emails.length(); i++) {
            JSONObject emailObj = emails.getJSONObject(i);
            if (emailObj.getBoolean("primary")) {
                return emailObj.getString("email");
            }
        }

        return ""; // Return empty string if no primary email found
    }}