package dao;

import db.DBConnection;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User save(User user) {
        String sql = "INSERT INTO users (name, email, role, oauth_provider, github_username, access_token, avatar_url, active_plan_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "name = VALUES(name), role = VALUES(role), github_username = VALUES(github_username), " +
                "access_token = VALUES(access_token), avatar_url = VALUES(avatar_url), active_plan_id = VALUES(active_plan_id)";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getInstance().getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getRole().name());
            stmt.setString(4, user.getOauthProvider());
            stmt.setString(5, user.getGithubUsername());
            stmt.setString(6, user.getAccessToken());
            stmt.setString(7, user.getAvatarUrl());

            if (user.getActivePlanId() != null) {
                stmt.setInt(8, user.getActivePlanId());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }

            int affectedRows = stmt.executeUpdate();
            System.out.println("UserDAO.save: affectedRows = " + affectedRows);

            // Try to get generated keys (for new insert)
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                user.setId(rs.getInt(1));
                System.out.println("✅ Generated new user ID: " + user.getId());
                return user;
            }

            // If no generated keys, it was an update - fetch the user by email
            if (affectedRows > 0) {
                User existing = findByEmail(user.getEmail());
                if (existing != null) {
                    System.out.println("✅ Found existing user with ID: " + existing.getId());
                    return existing;
                }
            }

            return user;

        } catch (SQLException e) {
            System.err.println("❌ Database error in UserDAO.save: " + e.getMessage());
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

    public boolean update(User user) {
        String sql = "UPDATE users SET name = ?, role = ?, oauth_provider = ?, " +
                "github_username = ?, access_token = ?, avatar_url = ?, active_plan_id = ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getInstance().getConnection();
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getRole().name());
            stmt.setString(3, user.getOauthProvider());
            stmt.setString(4, user.getGithubUsername());
            stmt.setString(5, user.getAccessToken());
            stmt.setString(6, user.getAvatarUrl());

            if (user.getActivePlanId() != null) {
                stmt.setInt(7, user.getActivePlanId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            stmt.setInt(8, user.getId());

            int rowsAffected = stmt.executeUpdate();
            System.out.println("✅ Updated user " + user.getId() + ": " + rowsAffected + " rows");

            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("❌ SQL Error updating user: " + e.getMessage());
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

    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getInstance().getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getInstance().getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public int getUserIdByEmail(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getInstance().getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getInstance().getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return users;
    }

    public void updateActivePlan(int userId, Integer planId) {
        String sql = "UPDATE users SET active_plan_id = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.getInstance().getConnection();
            stmt = conn.prepareStatement(sql);

            if (planId == null) {
                stmt.setNull(1, Types.INTEGER);
            } else {
                stmt.setInt(1, planId);
            }
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            System.out.println("UserDAO.updateActivePlan: user " + userId + " active plan set to " + planId);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Integer getActivePlanId(int userId) {
        String sql = "SELECT active_plan_id FROM users WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getInstance().getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("active_plan_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

        int activePlanId = rs.getInt("active_plan_id");
        if (!rs.wasNull()) {
            user.setActivePlanId(activePlanId);
        } else {
            user.setActivePlanId(null);
        }

        return user;
    }
}