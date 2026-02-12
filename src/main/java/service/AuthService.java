package service;

import model.User;

public interface AuthService {
    User authenticate(String authCode);
    String getAuthorizationUrl();
    User getUserInfo(String accessToken);
}