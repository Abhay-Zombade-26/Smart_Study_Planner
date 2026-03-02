package model;

import java.time.LocalDate;

public class DailyTask {
    private int id;
    private int userId;
    private int goalId;
    private String repositoryName;
    private LocalDate taskDate;
    private int plannedHours;
    private int actualHours;
    private int plannedCommits;
    private int actualCommits;
    private String status; // PENDING, COMPLETED, MISSED, ADJUSTED
    private String description;
    private LocalDate createdAt;
    
    public DailyTask() {}
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getGoalId() { return goalId; }
    public void setGoalId(int goalId) { this.goalId = goalId; }
    
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
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
    
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
    
    public boolean isMissed() {
        return "MISSED".equals(status);
    }
    
    public String getStatusEmoji() {
        switch(status) {
            case "COMPLETED": return "?";
            case "MISSED": return "?";
            case "PENDING": return "?";
            case "ADJUSTED": return "??";
            default: return "?";
        }
    }
}
