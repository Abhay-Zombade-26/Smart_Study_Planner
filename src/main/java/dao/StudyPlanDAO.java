package dao;

import db.DBConnection;
import model.StudyPlan;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudyPlanDAO {

    public StudyPlan save(StudyPlan plan) {
        String sql = "INSERT INTO goals (user_id, plan_name, repository_name, subject_name, subjects, end_date, experience_level, daily_hours, completion_percentage, ai_generated, role, login_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, plan.getUserId());
            stmt.setString(2, plan.getPlanName());
            stmt.setString(3, plan.getRepositoryName());
            stmt.setString(4, plan.getSubjectName());
            stmt.setString(5, plan.getSubjects());
            stmt.setDate(6, Date.valueOf(plan.getDeadline()));
            stmt.setString(7, plan.getDifficulty());
            stmt.setInt(8, plan.getDailyHours());
            stmt.setInt(9, plan.getCompletionPercentage());
            stmt.setBoolean(10, plan.isAiGenerated());
            stmt.setString(11, plan.getRole() != null ? plan.getRole() : "NORMAL");
            stmt.setString(12, plan.getLoginType() != null ? plan.getLoginType() : "GOOGLE");

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
            rs.close();

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
                StudyPlan plan = mapResultSetToStudyPlan(rs);
                rs.close();
                return plan;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<StudyPlan> findByUserIdAndRole(int userId, String role, String loginType) {
        List<StudyPlan> plans = new ArrayList<>();
        String sql = "SELECT * FROM goals WHERE user_id = ? AND role = ? AND login_type = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, role);
            stmt.setString(3, loginType);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                plans.add(mapResultSetToStudyPlan(rs));
            }
            rs.close();

            System.out.println("Total " + role + " plans found for user " + userId + ": " + plans.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return plans;
    }

    @Deprecated
    public List<StudyPlan> findByUserId(int userId) {
        List<StudyPlan> plans = new ArrayList<>();
        String sql = "SELECT * FROM goals WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                plans.add(mapResultSetToStudyPlan(rs));
            }
            rs.close();

            System.out.println("Total plans found for user " + userId + ": " + plans.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return plans;
    }

    public boolean update(StudyPlan plan) {
        String sql = "UPDATE goals SET plan_name = ?, subject_name = ?, subjects = ?, end_date = ?, experience_level = ?, daily_hours = ?, completion_percentage = ?, ai_generated = ? WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, plan.getPlanName());
            stmt.setString(2, plan.getSubjectName());
            stmt.setString(3, plan.getSubjects());
            stmt.setDate(4, Date.valueOf(plan.getDeadline()));
            stmt.setString(5, plan.getDifficulty());
            stmt.setInt(6, plan.getDailyHours());
            stmt.setInt(7, plan.getCompletionPercentage());
            stmt.setBoolean(8, plan.isAiGenerated());
            stmt.setInt(9, plan.getId());

            int affectedRows = stmt.executeUpdate();
            System.out.println("StudyPlanDAO.update: affectedRows = " + affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Database error in StudyPlanDAO.update: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

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

        try {
            plan.setPlanName(rs.getString("plan_name"));
        } catch (SQLException e) {
            plan.setPlanName(null);
        }

        try {
            plan.setRepositoryName(rs.getString("repository_name"));
        } catch (SQLException e) {
            plan.setRepositoryName(null);
        }

        try {
            plan.setSubjectName(rs.getString("subject_name"));
        } catch (SQLException e) {
            plan.setSubjectName(null);
        }

        try {
            plan.setSubjects(rs.getString("subjects"));
        } catch (SQLException e) {
            plan.setSubjects(null);
        }

        plan.setDeadline(rs.getDate("end_date").toLocalDate());
        plan.setDifficulty(rs.getString("experience_level"));
        plan.setDailyHours(rs.getInt("daily_hours"));

        try {
            plan.setCompletionPercentage(rs.getInt("completion_percentage"));
        } catch (SQLException e) {
            plan.setCompletionPercentage(0);
        }

        try {
            plan.setAiGenerated(rs.getBoolean("ai_generated"));
        } catch (SQLException e) {
            plan.setAiGenerated(false);
        }

        try {
            plan.setRole(rs.getString("role"));
        } catch (SQLException e) {
            plan.setRole("NORMAL");
        }

        try {
            plan.setLoginType(rs.getString("login_type"));
        } catch (SQLException e) {
            plan.setLoginType("GOOGLE");
        }

        return plan;
    }
}