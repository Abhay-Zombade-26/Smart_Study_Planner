package model;

import java.time.LocalDate;

public class Goal {
    private int id;
    private int userId;
    private String repositoryName;
    private String priority; // HIGH, MEDIUM, LOW
    private String targetFeatures; // comma-separated list
    private int durationMonths;
    private int dailyHours;
    private String experienceLevel; // BEGINNER, INTERMEDIATE, ADVANCED
    private LocalDate startDate;
    private LocalDate endDate;
    private int targetCommits;
    private int currentCommits;
    private String status;
    
    public Goal() {}
    
    public Goal(int userId, String repositoryName, String priority, String targetFeatures, 
                int durationMonths, int dailyHours, String experienceLevel) {
        this.userId = userId;
        this.repositoryName = repositoryName;
        this.priority = priority;
        this.targetFeatures = targetFeatures;
        this.durationMonths = durationMonths;
        this.dailyHours = dailyHours;
        this.experienceLevel = experienceLevel;
        this.startDate = LocalDate.now();
        this.endDate = startDate.plusMonths(durationMonths);
        this.status = "ACTIVE";
        this.targetCommits = durationMonths * 30;
        this.currentCommits = 0;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getRepositoryName() { return repositoryName; }
    public void setRepositoryName(String repositoryName) { this.repositoryName = repositoryName; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
    public String getTargetFeatures() { return targetFeatures; }
    public void setTargetFeatures(String targetFeatures) { this.targetFeatures = targetFeatures; }
    
    public int getDurationMonths() { return durationMonths; }
    public void setDurationMonths(int durationMonths) { this.durationMonths = durationMonths; }
    
    public int getDailyHours() { return dailyHours; }
    public void setDailyHours(int dailyHours) { this.dailyHours = dailyHours; }
    
    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }
    
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
    
    public double getPriorityWeight() {
        switch(priority) {
            case "HIGH": return 1.5;
            case "MEDIUM": return 1.0;
            case "LOW": return 0.5;
            default: return 1.0;
        }
    }
    
    public double getExperienceMultiplier() {
        switch(experienceLevel) {
            case "BEGINNER": return 1.5; // Takes 50% longer
            case "INTERMEDIATE": return 1.0;
            case "ADVANCED": return 0.7; // Takes 30% less time
            default: return 1.0;
        }
    }
    
    public String[] getFeatureList() {
        if (targetFeatures == null || targetFeatures.isEmpty()) {
            return new String[0];
        }
        return targetFeatures.split(",");
    }
}
