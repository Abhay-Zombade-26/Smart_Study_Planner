package model;

import enums.UserRole;

public class User {
    private int id;
    private String name;
    private String email;
    private UserRole role;
    private String oauthProvider;
    private String githubUsername;
    private String accessToken;
    private String avatarUrl;

    public User() {}

    public User(String name, String email, UserRole role, String oauthProvider) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.oauthProvider = oauthProvider;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public void setRole(String role) { this.role = UserRole.valueOf(role); }

    public String getOauthProvider() { return oauthProvider; }
    public void setOauthProvider(String oauthProvider) { this.oauthProvider = oauthProvider; }

    public String getGithubUsername() { return githubUsername; }
    public void setGithubUsername(String githubUsername) { this.githubUsername = githubUsername; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", githubUsername='" + githubUsername + '\'' +
                '}';
    }
}