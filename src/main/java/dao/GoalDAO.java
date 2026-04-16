package dao;

import model.Goal;
import db.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GoalDAO {

    public boolean save(Goal goal) {
        String sql = "INSERT INTO goals (user_id, repository_name, priority, target_features, " +
                "duration_months, daily_hours, experience_level, start_date, end_date, " +
                "target_commits, current_commits, status, project_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, goal.getUserId());
            stmt.setString(2, goal.getRepositoryName());
            stmt.setString(3, goal.getPriority());
            stmt.setString(4, goal.getTargetFeatures());
            stmt.setInt(5, goal.getDurationMonths());
            stmt.setInt(6, goal.getDailyHours());
            stmt.setString(7, goal.getExperienceLevel());

            // Handle null dates
            if (goal.getStartDate() != null) {
                stmt.setDate(8, Date.valueOf(goal.getStartDate()));
            } else {
                stmt.setNull(8, Types.DATE);
            }

            if (goal.getEndDate() != null) {
                stmt.setDate(9, Date.valueOf(goal.getEndDate()));
            } else {
                stmt.setNull(9, Types.DATE);
            }

            stmt.setInt(10, goal.getTargetCommits());
            stmt.setInt(11, goal.getCurrentCommits());
            stmt.setString(12, goal.getStatus());

            // New project_type field
            if (goal.getProjectType() != null) {
                stmt.setString(13, goal.getProjectType());
            } else {
                stmt.setNull(13, Types.VARCHAR);
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        goal.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error saving goal: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Goal> findByUserId(int userId) {
        List<Goal> goals = new ArrayList<>();
        String sql = "SELECT * FROM goals WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                goals.add(mapResultSetToGoal(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching goals: " + e.getMessage());
        }
        return goals;
    }

    // NEW METHOD: Find goal by repository name
    public Goal findByRepositoryName(String repositoryName, int userId) {
        String sql = "SELECT * FROM goals WHERE repository_name = ? AND user_id = ? ORDER BY created_at DESC LIMIT 1";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, repositoryName);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToGoal(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding goal by repository name: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteByUserId(int userId) {
        String sql = "DELETE FROM goals WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            int deleted = stmt.executeUpdate();
            System.out.println("✅ Deleted " + deleted + " goals for user " + userId);
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting goals: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteById(int goalId) {
        String sql = "DELETE FROM goals WHERE id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, goalId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting goal: " + e.getMessage());
            return false;
        }
    }

    private Goal mapResultSetToGoal(ResultSet rs) throws SQLException {
        Goal goal = new Goal();
        goal.setId(rs.getInt("id"));
        goal.setUserId(rs.getInt("user_id"));
        goal.setRepositoryName(rs.getString("repository_name"));

        // Handle nullable fields with try-catch
        try { goal.setPriority(rs.getString("priority")); } catch (SQLException e) { goal.setPriority("MEDIUM"); }
        try { goal.setTargetFeatures(rs.getString("target_features")); } catch (SQLException e) { goal.setTargetFeatures(""); }
        try { goal.setExperienceLevel(rs.getString("experience_level")); } catch (SQLException e) { goal.setExperienceLevel("INTERMEDIATE"); }

        // NEW: Get project_type
        try {
            goal.setProjectType(rs.getString("project_type"));
        } catch (SQLException e) {
            goal.setProjectType("UNKNOWN");
        }

        goal.setDurationMonths(rs.getInt("duration_months"));
        goal.setDailyHours(rs.getInt("daily_hours"));

        // Handle null dates
        Date startDateSql = rs.getDate("start_date");
        if (startDateSql != null) {
            goal.setStartDate(startDateSql.toLocalDate());
        } else {
            goal.setStartDate(LocalDate.now());
        }

        Date endDateSql = rs.getDate("end_date");
        if (endDateSql != null) {
            goal.setEndDate(endDateSql.toLocalDate());
        } else {
            if (goal.getStartDate() != null) {
                goal.setEndDate(goal.getStartDate().plusMonths(goal.getDurationMonths()));
            } else {
                goal.setEndDate(LocalDate.now().plusMonths(goal.getDurationMonths()));
            }
        }

        goal.setTargetCommits(rs.getInt("target_commits"));
        goal.setCurrentCommits(rs.getInt("current_commits"));
        goal.setStatus(rs.getString("status"));
        return goal;
    }
}