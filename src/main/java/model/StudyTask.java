package model;

import java.time.LocalDate;

public class StudyTask {
    private int id;
    private int goalId;
    private int userId;
    private String repositoryName;
    private LocalDate taskDate;
    private int plannedHours;
    private int actualHours;
    private int plannedCommits;
    private int actualCommits;
    private String description;
    private boolean requiredCommit;
    private String status;
    private int topicId;
    private String sessionType;
    private String expectedFiles;  // Add this field for GitHub/AI feature

    public StudyTask() {}

    public StudyTask(int goalId, LocalDate taskDate, String description, boolean requiredCommit) {
        this.goalId = goalId;
        this.taskDate = taskDate;
        this.description = description;
        this.requiredCommit = requiredCommit;
        this.status = "PENDING";
    }

    public StudyTask(int goalId, LocalDate taskDate, String description, boolean requiredCommit,
                     int topicId, String sessionType) {
        this(goalId, taskDate, description, requiredCommit);
        this.topicId = topicId;
        this.sessionType = sessionType;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getGoalId() { return goalId; }
    public void setGoalId(int goalId) { this.goalId = goalId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getRepositoryName() { return repositoryName; }
    public void setRepositoryName(String repositoryName) { this.repositoryName = repositoryName; }

    public LocalDate getTaskDate() { return taskDate; }
    public void setTaskDate(LocalDate taskDate) { this.taskDate = taskDate; }

    public int getPlannedHours() { return plannedHours; }
    public void setPlannedHours(int plannedHours) { this.plannedHours = plannedHours; }

    public int getActualHours() { return actualHours; }
    public void setActualHours(int actualHours) { this.actualHours = actualHours; }

    public int getPlannedCommits() { return plannedCommits; }
    public void setPlannedCommits(int plannedCommits) { this.plannedCommits = plannedCommits; }

    public int getActualCommits() { return actualCommits; }
    public void setActualCommits(int actualCommits) { this.actualCommits = actualCommits; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isRequiredCommit() { return requiredCommit; }
    public void setRequiredCommit(boolean requiredCommit) { this.requiredCommit = requiredCommit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTopicId() { return topicId; }
    public void setTopicId(int topicId) { this.topicId = topicId; }

    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    public String getExpectedFiles() { return expectedFiles; }
    public void setExpectedFiles(String expectedFiles) { this.expectedFiles = expectedFiles; }

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
}