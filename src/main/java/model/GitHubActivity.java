package model;

import java.time.LocalDate;

public class GitHubActivity {
    private int id;
    private int userId;
    private String repoName;
    private int commitCount;
    private LocalDate lastCommitDate;
    private int streakCount;

    public GitHubActivity() {}

    public GitHubActivity(int userId, String repoName) {
        this.userId = userId;
        this.repoName = repoName;
        this.commitCount = 0;
        this.streakCount = 0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }

    public int getCommitCount() { return commitCount; }
    public void setCommitCount(int commitCount) { this.commitCount = commitCount; }

    public LocalDate getLastCommitDate() { return lastCommitDate; }
    public void setLastCommitDate(LocalDate lastCommitDate) { this.lastCommitDate = lastCommitDate; }

    public int getStreakCount() { return streakCount; }
    public void setStreakCount(int streakCount) { this.streakCount = streakCount; }

    public String getStatus() {
        if (lastCommitDate == null) return "No activity";

        LocalDate today = LocalDate.now();
        if (lastCommitDate.equals(today)) return "Green";
        if (lastCommitDate.equals(today.minusDays(1))) return "Yellow";
        return "Red";
    }
}