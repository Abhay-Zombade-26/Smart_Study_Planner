package model;

import java.time.LocalDate;

public class StudyTask {
    private int id;
    private int goalId;           // Google flow
    private int userId;            // GitHub flow
    private String repositoryName; // GitHub flow
    private LocalDate taskDate;
    private String description;
    private boolean requiredCommit;
    private String status; // PENDING, COMPLETED, MISSED
    private int plannedHours;      // GitHub flow
    private int actualHours;       // GitHub flow
    private int plannedCommits;    // GitHub flow
    private int actualCommits;     // GitHub flow
    private int topicId;           // Google flow
    private String sessionType;    // Google flow
    private String expectedFiles;  // NEW FIELD: Files expected to be modified for this task

    public StudyTask() {}

    // Constructor for GitHub flow
    public StudyTask(int userId, String repositoryName, LocalDate taskDate,
                     String description, int plannedHours, int plannedCommits) {
        this.userId = userId;
        this.repositoryName = repositoryName;
        this.taskDate = taskDate;
        this.description = description;
        this.plannedHours = plannedHours;
        this.plannedCommits = plannedCommits;
        this.requiredCommit = true;
        this.status = "PENDING";
        this.actualHours = 0;
        this.actualCommits = 0;
        this.expectedFiles = "";
    }

    // Constructor for Google flow
    public StudyTask(int goalId, LocalDate taskDate, String description,
                     boolean requiredCommit, int topicId, String sessionType) {
        this.goalId = goalId;
        this.taskDate = taskDate;
        this.description = description;
        this.requiredCommit = requiredCommit;
        this.topicId = topicId;
        this.sessionType = sessionType;
        this.status = "PENDING";
        this.expectedFiles = "";
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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isRequiredCommit() { return requiredCommit; }
    public void setRequiredCommit(boolean requiredCommit) { this.requiredCommit = requiredCommit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getPlannedHours() { return plannedHours; }
    public void setPlannedHours(int plannedHours) { this.plannedHours = plannedHours; }

    public int getActualHours() { return actualHours; }
    public void setActualHours(int actualHours) { this.actualHours = actualHours; }

    public int getPlannedCommits() { return plannedCommits; }
    public void setPlannedCommits(int plannedCommits) { this.plannedCommits = plannedCommits; }

    public int getActualCommits() { return actualCommits; }
    public void setActualCommits(int actualCommits) { this.actualCommits = actualCommits; }

    public int getTopicId() { return topicId; }
    public void setTopicId(int topicId) { this.topicId = topicId; }

    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    // NEW GETTER AND SETTER
    public String getExpectedFiles() { return expectedFiles; }
    public void setExpectedFiles(String expectedFiles) { this.expectedFiles = expectedFiles; }

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isMissed() {
        return "MISSED".equals(status);
    }
}