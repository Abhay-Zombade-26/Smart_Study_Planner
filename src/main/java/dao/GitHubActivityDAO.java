package dao;

import db.DBConnection;
import model.GitHubActivity;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GitHubActivityDAO {

    public void saveOrUpdate(GitHubActivity activity) {
        String sql = """
            INSERT INTO github_activity (user_id, repo_name, commit_count, last_commit_date, streak_count)
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            commit_count = VALUES(commit_count),
            last_commit_date = VALUES(last_commit_date),
            streak_count = VALUES(streak_count),
            last_updated = CURRENT_TIMESTAMP
        """;

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, activity.getUserId());
            stmt.setString(2, activity.getRepoName());
            stmt.setInt(3, activity.getCommitCount());
            stmt.setDate(4, activity.getLastCommitDate() != null ? Date.valueOf(activity.getLastCommitDate()) : null);
            stmt.setInt(5, activity.getStreakCount());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public GitHubActivity findByUserAndRepo(int userId, String repoName) {
        String sql = "SELECT * FROM github_activity WHERE user_id = ? AND repo_name = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, repoName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToGitHubActivity(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<GitHubActivity> findByUserId(int userId) {
        List<GitHubActivity> activities = new ArrayList<>();
        String sql = "SELECT * FROM github_activity WHERE user_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                activities.add(mapResultSetToGitHubActivity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return activities;
    }

    private GitHubActivity mapResultSetToGitHubActivity(ResultSet rs) throws SQLException {
        GitHubActivity activity = new GitHubActivity();
        activity.setId(rs.getInt("id"));
        activity.setUserId(rs.getInt("user_id"));
        activity.setRepoName(rs.getString("repo_name"));
        activity.setCommitCount(rs.getInt("commit_count"));
        activity.setLastCommitDate(rs.getDate("last_commit_date") != null ?
                rs.getDate("last_commit_date").toLocalDate() : null);
        activity.setStreakCount(rs.getInt("streak_count"));
        return activity;
    }
}