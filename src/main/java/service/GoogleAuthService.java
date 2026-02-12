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

    @Override
    public String getAuthorizationUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=" + AppConfig.GOOGLE_CLIENT_ID +
                "&redirect_uri=" + AppConfig.GOOGLE_REDIRECT_URI +
                "&response_type=code" +
                "&scope=openid%20email%20profile" +
                "&access_type=offline";
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
        params.put("code", code);
        params.put("client_id", AppConfig.GOOGLE_CLIENT_ID);
        params.put("client_secret", AppConfig.GOOGLE_CLIENT_SECRET);
        params.put("redirect_uri", AppConfig.GOOGLE_REDIRECT_URI);
        params.put("grant_type", "authorization_code");

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        String body = HttpUtil.encodeParams(params);
        String response = HttpUtil.sendPost(GOOGLE_TOKEN_URL, headers, body);

        JSONObject json = JsonUtil.parseObject(response);
        return json.getString("access_token");
    }

    @Override
    public User getUserInfo(String accessToken) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + accessToken);

            String response = HttpUtil.sendGet(GOOGLE_USERINFO_URL, headers);
            JSONObject userInfo = JsonUtil.parseObject(response);

            User user = new User();
            user.setName(userInfo.getString("name"));
            user.setEmail(userInfo.getString("email"));
            user.setRole(UserRole.NORMAL);
            user.setOauthProvider("GOOGLE");
            user.setAvatarUrl(userInfo.getString("picture"));

            // Save to database
            UserDAO userDAO = new UserDAO();
            return userDAO.save(user);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}