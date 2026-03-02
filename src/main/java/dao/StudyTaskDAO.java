package dao;

import model.DailyTask;
import db.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudyTaskDAO {
    
    // Save single task
    public boolean save(DailyTask task) {
        String sql = "INSERT INTO study_tasks (user_id, repository_name, task_date, planned_hours, " +
                    "planned_commits, status, description, goal_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, task.getUserId());
            stmt.setString(2, task.getRepositoryName());
            stmt.setDate(3, Date.valueOf(task.getTaskDate()));
            stmt.setInt(4, task.getPlannedHours());
            stmt.setInt(5, task.getPlannedCommits());
            stmt.setString(6, task.getStatus());
            stmt.setString(7, task.getDescription());
            stmt.setObject(8, task.getGoalId() > 0 ? task.getGoalId() : null);
            
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
    
    // Save multiple tasks (for plan generation)
    public boolean saveAll(List<DailyTask> tasks) {
        boolean allSaved = true;
        for (DailyTask task : tasks) {
            if (!save(task)) {
                allSaved = false;
            }
        }
        return allSaved;
    }
    
    // Find tasks by user ID
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
    
    // Find tasks by goal ID
    public List<DailyTask> findByGoalId(int goalId) {
        List<DailyTask> tasks = new ArrayList<>();
        String sql = "SELECT * FROM study_tasks WHERE goal_id = ? ORDER BY task_date ASC";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, goalId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching tasks by goal: " + e.getMessage());
            e.printStackTrace();
        }
        return tasks;
    }
    
    // Find today's tasks
    public List<DailyTask> findTodayTasks(int userId) {
        List<DailyTask> tasks = new ArrayList<>();
        String sql = "SELECT * FROM study_tasks WHERE user_id = ? AND task_date = CURRENT_DATE ORDER BY repository_name";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching today's tasks: " + e.getMessage());
            e.printStackTrace();
        }
        return tasks;
    }
    
    // Update task status
    public boolean updateStatus(int taskId, String status) {
        String sql = "UPDATE study_tasks SET status = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, taskId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating task status: " + e.getMessage());
            return false;
        }
    }
    
    // Update task with actual data
    public boolean update(DailyTask task) {
        String sql = "UPDATE study_tasks SET actual_hours = ?, actual_commits = ?, status = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, task.getActualHours());
            stmt.setInt(2, task.getActualCommits());
            stmt.setString(3, task.getStatus());
            stmt.setInt(4, task.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating task: " + e.getMessage());
            return false;
        }
    }
    
    // Find task by user, date, and repo
    public DailyTask findByUserAndDate(int userId, LocalDate date, String repoName) {
        String sql = "SELECT * FROM study_tasks WHERE user_id = ? AND task_date = ? AND repository_name = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setString(3, repoName);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToTask(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding task: " + e.getMessage());
        }
        return null;
    }
    
    // Find pending tasks from date
    public List<DailyTask> findPendingFromDate(int userId, LocalDate fromDate) {
        List<DailyTask> tasks = new ArrayList<>();
        String sql = "SELECT * FROM study_tasks WHERE user_id = ? AND task_date >= ? AND status = 'PENDING' ORDER BY task_date";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(fromDate));
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching pending tasks: " + e.getMessage());
        }
        return tasks;
    }
    
    private DailyTask mapResultSetToTask(ResultSet rs) throws SQLException {
        DailyTask task = new DailyTask();
        task.setId(rs.getInt("id"));
        task.setUserId(rs.getInt("user_id"));
        task.setGoalId(rs.getInt("goal_id"));
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
