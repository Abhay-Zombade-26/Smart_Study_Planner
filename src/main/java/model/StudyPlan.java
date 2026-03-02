package model;

import java.time.LocalDate;

public class StudyPlan {
    private int id;
    private int userId;
    private String repositoryName;
    private LocalDate deadline;
    private int dailyHours;
    private String difficulty;
    private LocalDate createdAt;
    private String status;
    private int completionPercentage; // ADD THIS FIELD
    
    public StudyPlan() {}
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getRepositoryName() { return repositoryName; }
    public void setRepositoryName(String repositoryName) { this.repositoryName = repositoryName; }
    
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    
    public int getDailyHours() { return dailyHours; }
    public void setDailyHours(int dailyHours) { this.dailyHours = dailyHours; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    // ADD THESE TWO METHODS
    public int getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(int completionPercentage) { this.completionPercentage = completionPercentage; }
}
