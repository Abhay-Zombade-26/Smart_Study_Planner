package dao;

import db.DBConnection;
import model.StudyTask;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudyTaskDAO {

    public void save(StudyTask task) {
        // Include all columns that are NOT NULL or need to be set
        String sql = "INSERT INTO study_tasks (user_id, goal_id, repository_name, task_date, planned_hours, actual_hours, " +
                "planned_commits, actual_commits, description, required_commit, status, topic_id, session_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // user_id (GitHub flow)
            if (task.getUserId() > 0) {
                stmt.setInt(1, task.getUserId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }

            // goal_id (Google flow)
            if (task.getGoalId() > 0) {
                stmt.setInt(2, task.getGoalId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }

            // repository_name
            stmt.setString(3, task.getRepositoryName());

            // task_date
            stmt.setDate(4, Date.valueOf(task.getTaskDate()));

            // planned_hours
            stmt.setInt(5, task.getPlannedHours());

            // actual_hours
            stmt.setInt(6, task.getActualHours());

            // planned_commits
            stmt.setInt(7, task.getPlannedCommits());

            // actual_commits
            stmt.setInt(8, task.getActualCommits());

            // description
            stmt.setString(9, task.getDescription());

            // required_commit
            stmt.setBoolean(10, task.isRequiredCommit());

            // status
            stmt.setString(11, task.getStatus());

            // topic_id
            stmt.setInt(12, task.getTopicId());

            // session_type
            stmt.setString(13, task.getSessionType());

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

    public List<StudyTask> findByGoalId(int goalId) {
        List<StudyTask> tasks = new ArrayList<>();
        String sql = "SELECT * FROM study_tasks WHERE goal_id = ? ORDER BY task_date ASC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, goalId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                StudyTask task = mapResultSetToStudyTask(rs);
                tasks.add(task);
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

        // Try Google flow first (with goal_id)
        String sqlGoogle = "SELECT t.* FROM study_tasks t " +
                "JOIN goals g ON t.goal_id = g.id " +
                "WHERE g.user_id = ? ORDER BY t.task_date DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlGoogle)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tasks.add(mapResultSetToStudyTask(rs));
            }
            rs.close();

            if (!tasks.isEmpty()) {
                return tasks;
            }

        } catch (SQLException e) {
            // Ignore, try GitHub flow
        }

        // Try GitHub flow (direct user_id)
        String sqlGitHub = "SELECT * FROM study_tasks WHERE user_id = ? ORDER BY task_date DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlGitHub)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tasks.add(mapResultSetToStudyTaskGitHub(rs));
            }
            rs.close();

        } catch (SQLException e) {
            System.err.println("Error in findByUserId: " + e.getMessage());
            e.printStackTrace();
        }

        return tasks;
    }

    private StudyTask mapResultSetToStudyTaskGitHub(ResultSet rs) throws SQLException {
        StudyTask task = new StudyTask();
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
        task.setRequiredCommit(true);
        return task;
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

    public List<StudyTask> findTodayTasks(int userId) {
        List<StudyTask> tasks = new ArrayList<>();
        String sql = "SELECT t.* FROM study_tasks t " +
                "JOIN goals g ON t.goal_id = g.id " +
                "WHERE g.user_id = ? AND t.task_date = CURRENT_DATE " +
                "ORDER BY t.id ASC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tasks.add(mapResultSetToStudyTask(rs));
            }
            rs.close();

            System.out.println("Found " + tasks.size() + " tasks for today for user ID: " + userId);
        } catch (SQLException e) {
            System.err.println("Database error in StudyTaskDAO.findTodayTasks: " + e.getMessage());
            e.printStackTrace();
        }

        return tasks;
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

    public List<StudyTask> findAllTasksByUser(int userId) {
        return findByUserId(userId);
    }

    public List<StudyTask> findTasksByDate(int userId, LocalDate date) {
        List<StudyTask> tasks = new ArrayList<>();
        String sql = "SELECT t.* FROM study_tasks t " +
                "JOIN goals g ON t.goal_id = g.id " +
                "WHERE g.user_id = ? AND t.task_date = ? " +
                "ORDER BY t.id ASC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tasks.add(mapResultSetToStudyTask(rs));
            }
            rs.close();

            System.out.println("Found " + tasks.size() + " tasks for date " + date + " for user ID: " + userId);
        } catch (SQLException e) {
            System.err.println("Database error in StudyTaskDAO.findTasksByDate: " + e.getMessage());
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
        String sql = "UPDATE study_tasks SET status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, task.getStatus());
            stmt.setInt(2, task.getId());

            int affectedRows = stmt.executeUpdate();
            System.out.println("✅ Task " + task.getId() + " updated (rows: " + affectedRows + ")");
        } catch (SQLException e) {
            System.err.println("❌ Database error in StudyTaskDAO.update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean deleteByUserId(int userId) {
        String sql = "DELETE FROM study_tasks WHERE goal_id IN (SELECT id FROM goals WHERE user_id = ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            int deleted = stmt.executeUpdate();
            System.out.println("✅ Deleted " + deleted + " tasks for user " + userId);
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting tasks: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteByGoalId(int goalId) {
        String sql = "DELETE FROM study_tasks WHERE goal_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, goalId);
            int affectedRows = stmt.executeUpdate();
            System.out.println("Deleted " + affectedRows + " tasks for goal ID: " + goalId);
            return true;
        } catch (SQLException e) {
            System.err.println("Database error in StudyTaskDAO.deleteByGoalId: " + e.getMessage());
            e.printStackTrace();
            return false;
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

        // CRITICAL FIX: Read commit counts from database
        try {
            task.setPlannedCommits(rs.getInt("planned_commits"));
        } catch (SQLException e) {
            task.setPlannedCommits(1);
        }

        try {
            task.setActualCommits(rs.getInt("actual_commits"));
        } catch (SQLException e) {
            task.setActualCommits(0);
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
            task.setUserId(rs.getInt("user_id"));
        } catch (SQLException e) {
            task.setUserId(0);
        }

        try {
            task.setRepositoryName(rs.getString("repository_name"));
        } catch (SQLException e) {
            task.setRepositoryName(null);
        }

        // Handle nullable fields
        try {
            task.setTopicId(rs.getInt("topic_id"));
            if (rs.wasNull()) task.setTopicId(0);
        } catch (SQLException e) {
            task.setTopicId(0);
        }

        try {
            task.setSessionType(rs.getString("session_type"));
        } catch (SQLException e) {
            task.setSessionType(null);
        }

        return task;
    }
}