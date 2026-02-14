package dao;

import db.DBConnection;
import enums.UserRole;
import model.User;
import java.sql.*;

public class UserDAO {
    
    public User save(User user) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getInstance().getConnection();
            
            // Check if user exists
            String checkSql = "SELECT id FROM users WHERE email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, user.getEmail());
            ResultSet checkRs = checkStmt.executeQuery();
            
            if (checkRs.next()) {
                // Update existing user
                int existingId = checkRs.getInt("id");
                user.setId(existingId);
                
                String updateSql = "UPDATE users SET name=?, role=?, oauth_provider=?, " +
                                  "github_username=?, access_token=?, avatar_url=? WHERE id=?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, user.getName());
                updateStmt.setString(2, user.getRole().name());
                updateStmt.setString(3, user.getOauthProvider());
                updateStmt.setString(4, user.getGithubUsername());
                updateStmt.setString(5, user.getAccessToken());
                updateStmt.setString(6, user.getAvatarUrl());
                updateStmt.setInt(7, existingId);
                updateStmt.executeUpdate();
                updateStmt.close();
            } else {
                // Insert new user
                String insertSql = "INSERT INTO users (name, email, role, oauth_provider, github_username, access_token, avatar_url) " +
                                  "VALUES (?, ?, ?, ?, ?, ?, ?)";
                stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, user.getName());
                stmt.setString(2, user.getEmail());
                stmt.setString(3, user.getRole().name());
                stmt.setString(4, user.getOauthProvider());
                stmt.setString(5, user.getGithubUsername());
                stmt.setString(6, user.getAccessToken());
                stmt.setString(7, user.getAvatarUrl());
                stmt.executeUpdate();
                
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }
            
            checkRs.close();
            checkStmt.close();
            return user;
            
        } catch (SQLException e) {
            System.err.println("? SQL Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // ========== NEW UPDATE METHOD ADDED ==========
    public boolean update(User user) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DBConnection.getInstance().getConnection();
            
            String sql = "UPDATE users SET name = ?, role = ?, oauth_provider = ?, " +
                        "github_username = ?, access_token = ?, avatar_url = ? WHERE id = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getRole().name());
            stmt.setString(3, user.getOauthProvider());
            stmt.setString(4, user.getGithubUsername());
            stmt.setString(5, user.getAccessToken());
            stmt.setString(6, user.getAvatarUrl());
            stmt.setInt(7, user.getId());
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("? Updated user " + user.getId() + ": " + rowsAffected + " rows");
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("? SQL Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    // =============================================
    
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));
        user.setOauthProvider(rs.getString("oauth_provider"));
        user.setGithubUsername(rs.getString("github_username"));
        user.setAccessToken(rs.getString("access_token"));
        user.setAvatarUrl(rs.getString("avatar_url"));
        return user;
    }
}
