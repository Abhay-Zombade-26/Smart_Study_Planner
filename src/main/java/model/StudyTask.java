package model;

import java.time.LocalDate;

public class StudyTask {
    private int id;
    private int goalId;
    private LocalDate taskDate;
    private String description;
    private boolean requiredCommit;
    private String status; // PENDING, COMPLETED, MISSED

    public StudyTask() {}

    public StudyTask(int goalId, LocalDate taskDate, String description, boolean requiredCommit) {
        this.goalId = goalId;
        this.taskDate = taskDate;
        this.description = description;
        this.requiredCommit = requiredCommit;
        this.status = "PENDING";
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getGoalId() { return goalId; }
    public void setGoalId(int goalId) { this.goalId = goalId; }

    public LocalDate getTaskDate() { return taskDate; }
    public void setTaskDate(LocalDate taskDate) { this.taskDate = taskDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isRequiredCommit() { return requiredCommit; }
    public void setRequiredCommit(boolean requiredCommit) { this.requiredCommit = requiredCommit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}