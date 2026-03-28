package dao;

import db.DBConnection;
import model.Topic;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TopicDAO {

    // Save a new topic to database
    public Topic save(Topic topic) {
        String sql = "INSERT INTO topics (plan_id, subject, name, difficulty, size, weight) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, topic.getPlanId());
            stmt.setString(2, topic.getSubject());
            stmt.setString(3, topic.getName());
            stmt.setInt(4, topic.getDifficulty());
            stmt.setInt(5, topic.getSize());
            stmt.setDouble(6, topic.getWeight());

            int affectedRows = stmt.executeUpdate();
            System.out.println("TopicDAO.save: affectedRows = " + affectedRows);

            if (affectedRows == 0) {
                return null;
            }

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                topic.setId(rs.getInt(1));
                System.out.println("Generated topic ID: " + topic.getId());
            }
            return topic;

        } catch (SQLException e) {
            System.err.println("Database error in TopicDAO.save: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Find topic by ID
    public Topic findById(int id) {
        String sql = "SELECT * FROM topics WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToTopic(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Find all topics for a specific plan
    public List<Topic> findByPlanId(int planId) {
        List<Topic> topics = new ArrayList<>();
        String sql = "SELECT * FROM topics WHERE plan_id = ? ORDER BY id";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, planId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                topics.add(mapResultSetToTopic(rs));
            }

            System.out.println("Found " + topics.size() + " topics for plan ID: " + planId);
        } catch (SQLException e) {
            System.err.println("Database error in TopicDAO.findByPlanId: " + e.getMessage());
            e.printStackTrace();
        }
        return topics;
    }

    // Update an existing topic
    public boolean update(Topic topic) {
        String sql = "UPDATE topics SET subject = ?, name = ?, difficulty = ?, size = ?, weight = ? WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, topic.getSubject());
            stmt.setString(2, topic.getName());
            stmt.setInt(3, topic.getDifficulty());
            stmt.setInt(4, topic.getSize());
            stmt.setDouble(5, topic.getWeight());
            stmt.setInt(6, topic.getId());

            int affectedRows = stmt.executeUpdate();
            System.out.println("TopicDAO.update: affectedRows = " + affectedRows);
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Database error in TopicDAO.update: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Delete a topic by ID
    public boolean delete(int id) {
        String sql = "DELETE FROM topics WHERE id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            System.out.println("TopicDAO.delete: affectedRows = " + affectedRows);
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Database error in TopicDAO.delete: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Delete all topics for a given plan (useful when regenerating plan)
    public boolean deleteByPlanId(int planId) {
        String sql = "DELETE FROM topics WHERE plan_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, planId);
            int affectedRows = stmt.executeUpdate();
            System.out.println("TopicDAO.deleteByPlanId: deleted " + affectedRows + " topics for plan " + planId);
            return true; // even if zero, it's fine
        } catch (SQLException e) {
            System.err.println("Database error in TopicDAO.deleteByPlanId: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Helper method to map ResultSet to Topic object
    private Topic mapResultSetToTopic(ResultSet rs) throws SQLException {
        Topic topic = new Topic();
        topic.setId(rs.getInt("id"));
        topic.setPlanId(rs.getInt("plan_id"));
        topic.setSubject(rs.getString("subject"));
        topic.setName(rs.getString("name"));
        topic.setDifficulty(rs.getInt("difficulty"));
        topic.setSize(rs.getInt("size"));
        topic.setWeight(rs.getDouble("weight"));
        return topic;
    }
}