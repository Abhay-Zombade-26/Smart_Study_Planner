package dao;

import model.Goal;
import db.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GoalDAO {
    
    public boolean save(Goal goal) {
        String sql = "INSERT INTO goals (user_id, repository_name, duration_months, " +
                    "daily_hours, start_date, end_date, target_commits, current_commits, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, goal.getUserId());
            stmt.setString(2, goal.getRepositoryName());
            stmt.setInt(3, goal.getDurationMonths());
            stmt.setInt(4, goal.getDailyHours());
            stmt.setDate(5, Date.valueOf(goal.getStartDate()));
            stmt.setDate(6, Date.valueOf(goal.getEndDate()));
            stmt.setInt(7, goal.getTargetCommits());
            stmt.setInt(8, goal.getCurrentCommits());
            stmt.setString(9, goal.getStatus());
            
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
    
    // ADD THIS DELETE METHOD
    public boolean deleteByUserId(int userId) {
        String sql = "DELETE FROM goals WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            int deleted = stmt.executeUpdate();
            System.out.println("? Deleted " + deleted + " goals for user " + userId);
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting goals: " + e.getMessage());
            return false;
        }
    }
    
    // ADD THIS DELETE METHOD FOR SINGLE GOAL
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
        goal.setDurationMonths(rs.getInt("duration_months"));
        goal.setDailyHours(rs.getInt("daily_hours"));
        goal.setStartDate(rs.getDate("start_date").toLocalDate());
        goal.setEndDate(rs.getDate("end_date").toLocalDate());
        goal.setTargetCommits(rs.getInt("target_commits"));
        goal.setCurrentCommits(rs.getInt("current_commits"));
        goal.setStatus(rs.getString("status"));
        return goal;
    }
}
