package model;

import java.time.LocalDate;

public class Goal {
    private int id;
    private int userId;
    private String repositoryName;
    private int durationMonths;
    private int dailyHours;
    private LocalDate startDate;
    private LocalDate endDate;
    private int targetCommits;
    private int currentCommits;
    private String status;
    
    public Goal() {}
    
    public Goal(int userId, String repositoryName, int durationMonths, int dailyHours) {
        this.userId = userId;
        this.repositoryName = repositoryName;
        this.durationMonths = durationMonths;
        this.dailyHours = dailyHours;
        this.startDate = LocalDate.now();
        this.endDate = startDate.plusMonths(durationMonths);
        this.status = "ACTIVE";
        this.targetCommits = durationMonths * 30; // Rough estimate
        this.currentCommits = 0;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getRepositoryName() { return repositoryName; }
    public void setRepositoryName(String repositoryName) { this.repositoryName = repositoryName; }
    
    public int getDurationMonths() { return durationMonths; }
    public void setDurationMonths(int durationMonths) { this.durationMonths = durationMonths; }
    
    public int getDailyHours() { return dailyHours; }
    public void setDailyHours(int dailyHours) { this.dailyHours = dailyHours; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public int getTargetCommits() { return targetCommits; }
    public void setTargetCommits(int targetCommits) { this.targetCommits = targetCommits; }
    
    public int getCurrentCommits() { return currentCommits; }
    public void setCurrentCommits(int currentCommits) { this.currentCommits = currentCommits; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public int getProgressPercentage() {
        if (targetCommits == 0) return 0;
        return (currentCommits * 100) / targetCommits;
    }
}
