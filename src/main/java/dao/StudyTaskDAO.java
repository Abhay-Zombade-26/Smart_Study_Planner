package dao;

import model.DailyTask;
import db.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudyTaskDAO {
    
    public boolean save(DailyTask task) {
        String sql = "INSERT INTO study_tasks (user_id, repository_name, task_date, planned_hours, " +
                    "planned_commits, status, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, task.getUserId());
            stmt.setString(2, task.getRepositoryName());
            stmt.setDate(3, Date.valueOf(task.getTaskDate()));
            stmt.setInt(4, task.getPlannedHours());
            stmt.setInt(5, task.getPlannedCommits());
            stmt.setString(6, task.getStatus());
            stmt.setString(7, task.getDescription());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        task.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error saving task: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean saveAll(List<DailyTask> tasks) {
        boolean allSaved = true;
        for (DailyTask task : tasks) {
            if (!save(task)) {
                allSaved = false;
            }
        }
        return allSaved;
    }
    
    public List<DailyTask> findByUserId(int userId) {
        List<DailyTask> tasks = new ArrayList<>();
        String sql = "SELECT * FROM study_tasks WHERE user_id = ? ORDER BY task_date ASC";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching tasks: " + e.getMessage());
            e.printStackTrace();
        }
        return tasks;
    }
    
    // ========== ADD THIS UPDATE METHOD ==========
    public boolean update(DailyTask task) {
        String sql = "UPDATE study_tasks SET actual_hours = ?, actual_commits = ?, status = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, task.getActualHours());
            stmt.setInt(2, task.getActualCommits());
            stmt.setString(3, task.getStatus());
            stmt.setInt(4, task.getId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating task: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    // ============================================
    
    public boolean deleteByUserId(int userId) {
        String sql = "DELETE FROM study_tasks WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            int deleted = stmt.executeUpdate();
            System.out.println("? Deleted " + deleted + " tasks for user " + userId);
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting tasks: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteById(int taskId) {
        String sql = "DELETE FROM study_tasks WHERE id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, taskId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting task: " + e.getMessage());
            return false;
        }
    }
    
    private DailyTask mapResultSetToTask(ResultSet rs) throws SQLException {
        DailyTask task = new DailyTask();
        task.setId(rs.getInt("id"));
        task.setUserId(rs.getInt("user_id"));
        task.setRepositoryName(rs.getString("repository_name"));
        task.setTaskDate(rs.getDate("task_date").toLocalDate());
        task.setPlannedHours(rs.getInt("planned_hours"));
        task.setActualHours(rs.getInt("actual_hours"));
        task.setPlannedCommits(rs.getInt("planned_commits"));
        task.setActualCommits(rs.getInt("actual_commits"));
        task.setStatus(rs.getString("status"));
        task.setDescription(rs.getString("description"));
        return task;
    }
}
