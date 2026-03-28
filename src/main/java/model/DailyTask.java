package model;

import java.time.LocalDate;

// This class exists only for backward compatibility with GitHub code
// It will be removed once all GitHub code is migrated to StudyTask
public class DailyTask extends StudyTask {

    public DailyTask() {
        super();
    }

    // Helper methods for GitHub code
    public void setUserId(int userId) {
        super.setUserId(userId);
    }

    public int getUserId() {
        return super.getUserId();
    }

    public void setRepositoryName(String repoName) {
        super.setRepositoryName(repoName);
    }

    public String getRepositoryName() {
        return super.getRepositoryName();
    }

    public void setPlannedHours(int hours) {
        super.setPlannedHours(hours);
    }

    public int getPlannedHours() {
        return super.getPlannedHours();
    }

    public void setActualHours(int hours) {
        super.setActualHours(hours);
    }

    public int getActualHours() {
        return super.getActualHours();
    }

    public void setPlannedCommits(int commits) {
        super.setPlannedCommits(commits);
    }

    public int getPlannedCommits() {
        return super.getPlannedCommits();
    }

    public void setActualCommits(int commits) {
        super.setActualCommits(commits);
    }

    public int getActualCommits() {
        return super.getActualCommits();
    }
}