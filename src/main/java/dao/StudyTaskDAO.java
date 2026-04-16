package dao;

import db.DBConnection;
import model.StudyTask;
import model.DailyTask;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudyTaskDAO {

    // For DailyTask (used by GitHub/IT)
    public void save(DailyTask dailyTask) {
        String sql = "INSERT INTO study_tasks (user_id, goal_id, repository_name, task_date, planned_hours, actual_hours, " +
                "planned_commits, actual_commits, description, required_commit, status, topic_id, session_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, dailyTask.getUserId());
            stmt.setInt(2, dailyTask.getGoalId());
            stmt.setString(3, dailyTask.getRepositoryName());
            stmt.setDate(4, Date.valueOf(dailyTask.getTaskDate()));
            stmt.setInt(5, dailyTask.getPlannedHours());
            stmt.setInt(6, dailyTask.getActualHours());
            stmt.setInt(7, dailyTask.getPlannedCommits());
            stmt.setInt(8, dailyTask.getActualCommits());
            stmt.setString(9, dailyTask.getDescription());
            stmt.setBoolean(10, true);
            stmt.setString(11, dailyTask.getStatus());
            stmt.setInt(12, 0);
            stmt.setString(13, "CODING");

            int affectedRows = stmt.executeUpdate();
            System.out.println("StudyTaskDAO.save(DailyTask): affectedRows = " + affectedRows);

            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    dailyTask.setId(rs.getInt(1));
                    System.out.println("Generated task ID: " + dailyTask.getId());
                }
                rs.close();
            }
        } catch (SQLException e) {
            System.err.println("Database error in StudyTaskDAO.save(DailyTask): " + e.getMessage());
            e.printStackTrace();
        }
    }

    // For Normal/Google tasks - simplified INSERT
    public void save(StudyTask task) {
        String sql = "INSERT INTO study_tasks (goal_id, task_date, description, required_commit, status, topic_id, session_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, task.getGoalId());
            stmt.setDate(2, Date.valueOf(task.getTaskDate()));
            stmt.setString(3, task.getDescription());
            stmt.setBoolean(4, task.isRequiredCommit());
            stmt.setString(5, task.getStatus());
            stmt.setInt(6, task.getTopicId());
            stmt.setString(7, task.getSessionType());

            int affectedRows = stmt.executeUpdate();
            System.out.println("StudyTaskDAO.save: affectedRows = " + affectedRows);

            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    task.setId(rs.getInt(1));
                    System.out.println("Generated task ID: " + task.getId());
                }
                rs.close();
            }
        } catch (SQLException e) {
            System.err.println("Database error in StudyTaskDAO.save: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveAll(List<StudyTask> tasks) {
        System.out.println("Saving " + tasks.size() + " tasks to database");
        for (StudyTask task : tasks) {
            save(task);
        }
        System.out.println("All tasks saved successfully");
    }

    public void saveAllDailyTasks(List<DailyTask> tasks) {
        System.out.println("Saving " + tasks.size() + " DailyTask tasks to database");
        for (DailyTask task : tasks) {
            save(task);
        }
        System.out.println("All DailyTask tasks saved successfully");
    }

    public List<StudyTask> findByGoalId(int goalId) {
        List<StudyTask> tasks = new ArrayList<>();
        String sql = "SELECT * FROM study_tasks WHERE goal_id = ? ORDER BY task_date ASC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, goalId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tasks.add(mapResultSetToStudyTask(rs));
            }
            rs.close();

            System.out.println("Found " + tasks.size() + " tasks for goal ID: " + goalId);
        } catch (SQLException e) {
            System.err.println("Database error in StudyTaskDAO.findByGoalId: " + e.getMessage());
            e.printStackTrace();
        }

        return tasks;
    }

    public List<StudyTask> findByUserId(int userId) {
        List<StudyTask> tasks = new ArrayList<>();
        String sql = "SELECT t.* FROM study_tasks t " +
                "JOIN goals g ON t.goal_id = g.id " +
                "WHERE g.user_id = ? ORDER BY t.task_date DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tasks.add(mapResultSetToStudyTask(rs));
            }
            rs.close();

        } catch (SQLException e) {
            System.err.println("Error in findByUserId: " + e.getMessage());
            e.printStackTrace();
        }

        return tasks;
    }

    public List<StudyTask> findAllTasksByUser(int userId) {
        return findByUserId(userId);
    }

    public List<StudyTask> findTodayTasksByPlan(int planId) {
        List<StudyTask> tasks = new ArrayList<>();
        String sql = "SELECT * FROM study_tasks WHERE goal_id = ? AND task_date = CURRENT_DATE ORDER BY id";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, planId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tasks.add(mapResultSetToStudyTask(rs));
            }
            rs.close();

            System.out.println("Found " + tasks.size() + " tasks for today for plan ID: " + planId);
        } catch (SQLException e) {
            System.err.println("Database error in StudyTaskDAO.findTodayTasksByPlan: " + e.getMessage());
            e.printStackTrace();
        }

        return tasks;
    }

    public List<StudyTask> findTasksByDate(int planId, LocalDate date) {
        List<StudyTask> tasks = new ArrayList<>();
        String sql = "SELECT * FROM study_tasks WHERE goal_id = ? AND task_date = ? ORDER BY id";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, planId);
            stmt.setDate(2, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tasks.add(mapResultSetToStudyTask(rs));
            }
            rs.close();

            System.out.println("Found " + tasks.size() + " tasks for plan " + planId + " on date " + date);
        } catch (SQLException e) {
            System.err.println("Database error in StudyTaskDAO.findTasksByDate: " + e.getMessage());
            e.printStackTrace();
        }

        return tasks;
    }

    public List<StudyTask> findMissedTasks(int planId) {
        List<StudyTask> tasks = new ArrayList<>();
        String sql = "SELECT * FROM study_tasks WHERE goal_id = ? AND task_date < CURRENT_DATE AND status != 'COMPLETED' ORDER BY task_date DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, planId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tasks.add(mapResultSetToStudyTask(rs));
            }
            rs.close();

            System.out.println("Found " + tasks.size() + " missed tasks for plan ID: " + planId);
        } catch (SQLException e) {
            System.err.println("Database error in StudyTaskDAO.findMissedTasks: " + e.getMessage());
            e.printStackTrace();
        }

        return tasks;
    }

    public void updateStatus(int taskId, String status) {
        String sql = "UPDATE study_tasks SET status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, taskId);

            int affectedRows = stmt.executeUpdate();
            System.out.println("✅ Task " + taskId + " status updated to: " + status + " (rows: " + affectedRows + ")");
        } catch (SQLException e) {
            System.err.println("❌ Database error in StudyTaskDAO.updateStatus: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void update(StudyTask task) {
        updateStatus(task.getId(), task.getStatus());
    }

    public void updateCommitCount(int taskId, int commits) {
        String sql = "UPDATE study_tasks SET actual_commits = ? WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commits);
            stmt.setInt(2, taskId);
            int rows = stmt.executeUpdate();
            System.out.println("✅ Task " + taskId + " commit count updated to: " + commits + " (rows: " + rows + ")");
        } catch (SQLException e) {
            System.err.println("❌ Database error updating commit count: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updatePlannedCommits(int taskId, int plannedCommits) {
        String sql = "UPDATE study_tasks SET planned_commits = ? WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, plannedCommits);
            stmt.setInt(2, taskId);
            int rows = stmt.executeUpdate();
            System.out.println("✅ Task " + taskId + " planned commits updated to: " + plannedCommits + " (rows: " + rows + ")");
        } catch (SQLException e) {
            System.err.println("❌ Database error updating planned commits: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int deleteByUserId(int userId) {
        String sql = "DELETE FROM study_tasks WHERE goal_id IN (SELECT id FROM goals WHERE user_id = ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            int deleted = stmt.executeUpdate();
            System.out.println("✅ Deleted " + deleted + " tasks for user " + userId);
            return deleted;
        } catch (SQLException e) {
            System.err.println("Error deleting tasks: " + e.getMessage());
            return 0;
        }
    }

    public void deleteByGoalId(int goalId) {
        String sql = "DELETE FROM study_tasks WHERE goal_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, goalId);
            int affectedRows = stmt.executeUpdate();
            System.out.println("Deleted " + affectedRows + " tasks for goal ID: " + goalId);
        } catch (SQLException e) {
            System.err.println("Database error in StudyTaskDAO.deleteByGoalId: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteTasksByGoalId(int goalId) {
        String sql = "DELETE FROM study_tasks WHERE goal_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, goalId);
            int affectedRows = stmt.executeUpdate();
            System.out.println("Deleted " + affectedRows + " tasks for goal ID: " + goalId);
        } catch (SQLException e) {
            System.err.println("Database error in StudyTaskDAO.deleteTasksByGoalId: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteTaskById(int taskId) {
        String sql = "DELETE FROM study_tasks WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            int rows = stmt.executeUpdate();
            System.out.println("Deleted task " + taskId + " (rows: " + rows + ")");
        } catch (SQLException e) {
            System.err.println("Error deleting task: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getTotalTaskCountByPlan(int planId) {
        String sql = "SELECT COUNT(*) FROM study_tasks WHERE goal_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, planId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                rs.close();
                return count;
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Database error in StudyTaskDAO.getTotalTaskCountByPlan: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public int getCompletedTaskCountByPlan(int planId) {
        String sql = "SELECT COUNT(*) FROM study_tasks WHERE goal_id = ? AND status = 'COMPLETED'";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, planId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                rs.close();
                return count;
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Database error in StudyTaskDAO.getCompletedTaskCountByPlan: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalTaskCount(int userId) {
        String sql = "SELECT COUNT(*) FROM study_tasks t " +
                "JOIN goals g ON t.goal_id = g.id " +
                "WHERE g.user_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                rs.close();
                return count;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getCompletedTaskCount(int userId) {
        String sql = "SELECT COUNT(*) FROM study_tasks t " +
                "JOIN goals g ON t.goal_id = g.id " +
                "WHERE g.user_id = ? AND t.status = 'COMPLETED'";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                rs.close();
                return count;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private StudyTask mapResultSetToStudyTask(ResultSet rs) throws SQLException {
        StudyTask task = new StudyTask();
        task.setId(rs.getInt("id"));
        task.setGoalId(rs.getInt("goal_id"));
        task.setTaskDate(rs.getDate("task_date").toLocalDate());
        task.setDescription(rs.getString("description"));
        task.setRequiredCommit(rs.getBoolean("required_commit"));
        task.setStatus(rs.getString("status"));
        task.setTopicId(rs.getInt("topic_id"));
        task.setSessionType(rs.getString("session_type"));

        try {
            task.setUserId(rs.getInt("user_id"));
        } catch (SQLException e) {
            task.setUserId(0);
        }

        try {
            task.setRepositoryName(rs.getString("repository_name"));
        } catch (SQLException e) {
            task.setRepositoryName(null);
        }

        try {
            task.setPlannedHours(rs.getInt("planned_hours"));
        } catch (SQLException e) {
            task.setPlannedHours(0);
        }

        try {
            task.setActualHours(rs.getInt("actual_hours"));
        } catch (SQLException e) {
            task.setActualHours(0);
        }

        try {
            task.setPlannedCommits(rs.getInt("planned_commits"));
        } catch (SQLException e) {
            task.setPlannedCommits(0);
        }

        try {
            task.setActualCommits(rs.getInt("actual_commits"));
        } catch (SQLException e) {
            task.setActualCommits(0);
        }

        return task;
    }
}