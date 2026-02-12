package dao;

import db.DBConnection;
import model.StudyPlan;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudyPlanDAO {

    public StudyPlan save(StudyPlan plan) {
        String sql = "INSERT INTO goals (user_id, repository_name, deadline, difficulty, daily_hours, completion_percentage) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, plan.getUserId());
            stmt.setString(2, plan.getRepositoryName());
            stmt.setDate(3, Date.valueOf(plan.getDeadline()));
            stmt.setString(4, plan.getDifficulty());
            stmt.setInt(5, plan.getDailyHours());
            stmt.setInt(6, plan.getCompletionPercentage());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                plan.setId(rs.getInt(1));
            }

            return plan;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public StudyPlan findById(int id) {
        String sql = "SELECT * FROM goals WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToStudyPlan(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<StudyPlan> findByUserId(int userId) {
        List<StudyPlan> plans = new ArrayList<>();
        String sql = "SELECT * FROM goals WHERE user_id = ? ORDER BY deadline ASC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                plans.add(mapResultSetToStudyPlan(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return plans;
    }

    public void updateCompletionPercentage(int goalId, int percentage) {
        String sql = "UPDATE goals SET completion_percentage = ? WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, percentage);
            stmt.setInt(2, goalId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private StudyPlan mapResultSetToStudyPlan(ResultSet rs) throws SQLException {
        StudyPlan plan = new StudyPlan();
        plan.setId(rs.getInt("id"));
        plan.setUserId(rs.getInt("user_id"));
        plan.setRepositoryName(rs.getString("repository_name"));
        plan.setDeadline(rs.getDate("deadline").toLocalDate());
        plan.setDifficulty(rs.getString("difficulty"));
        plan.setDailyHours(rs.getInt("daily_hours"));
        plan.setCompletionPercentage(rs.getInt("completion_percentage"));
        return plan;
    }
}