package dao;

import db.DBConnection;
import model.StudyPlan;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudyPlanDAO {

    public StudyPlan save(StudyPlan plan) {
        String sql = "INSERT INTO goals (user_id, repository_name, subject_name, subjects, deadline, difficulty, daily_hours, completion_percentage) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, plan.getUserId());
            stmt.setString(2, plan.getRepositoryName());
            stmt.setString(3, plan.getSubjectName());
            stmt.setString(4, plan.getSubjects());
            stmt.setDate(5, Date.valueOf(plan.getDeadline()));
            stmt.setString(6, plan.getDifficulty());
            stmt.setInt(7, plan.getDailyHours());
            stmt.setInt(8, plan.getCompletionPercentage());

            int affectedRows = stmt.executeUpdate();
            System.out.println("StudyPlanDAO.save: affectedRows = " + affectedRows);

            if (affectedRows == 0) {
                return null;
            }

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                plan.setId(rs.getInt(1));
                System.out.println("Generated plan ID: " + plan.getId());
            }

            return plan;
        } catch (SQLException e) {
            System.err.println("Database error in StudyPlanDAO.save: " + e.getMessage());
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
        String sql = "SELECT * FROM goals WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                plans.add(mapResultSetToStudyPlan(rs));
                System.out.println("Found plan with ID: " + rs.getInt("id") + ", deadline: " + rs.getDate("deadline"));
            }

            System.out.println("Total plans found for user " + userId + ": " + plans.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return plans;
    }

    // ✅ Update an existing plan
    public boolean update(StudyPlan plan) {
        String sql = "UPDATE goals SET subject_name = ?, subjects = ?, deadline = ?, difficulty = ?, daily_hours = ?, completion_percentage = ? WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, plan.getSubjectName());
            stmt.setString(2, plan.getSubjects());
            stmt.setDate(3, Date.valueOf(plan.getDeadline()));
            stmt.setString(4, plan.getDifficulty());
            stmt.setInt(5, plan.getDailyHours());
            stmt.setInt(6, plan.getCompletionPercentage());
            stmt.setInt(7, plan.getId());

            int affectedRows = stmt.executeUpdate();
            System.out.println("StudyPlanDAO.update: affectedRows = " + affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Database error in StudyPlanDAO.update: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ✅ Delete a plan by ID
    public boolean deleteById(int planId) {
        String sql = "DELETE FROM goals WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, planId);
            int affectedRows = stmt.executeUpdate();
            System.out.println("StudyPlanDAO.deleteById: affectedRows = " + affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Database error in StudyPlanDAO.deleteById: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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
        plan.setSubjectName(rs.getString("subject_name"));
        plan.setSubjects(rs.getString("subjects"));
        plan.setDeadline(rs.getDate("deadline").toLocalDate());
        plan.setDifficulty(rs.getString("difficulty"));
        plan.setDailyHours(rs.getInt("daily_hours"));
        plan.setCompletionPercentage(rs.getInt("completion_percentage"));
        return plan;
    }
}