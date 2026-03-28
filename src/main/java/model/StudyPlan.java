package model;

import java.time.LocalDate;

public class StudyPlan {
    private int id;
    private int userId;
    private String planName;           // User-defined plan name
    private String repositoryName;      // GitHub flow (can be comma-separated for multiple repos)
    private String subjectName;         // Google flow (single subject - legacy)
    private String subjects;            // Google flow (multiple subjects)
    private LocalDate deadline;
    private int dailyHours;
    private String difficulty;
    private LocalDate createdAt;
    private String status;
    private int completionPercentage;
    private boolean aiGenerated;

    public StudyPlan() {}

    // Constructor for GitHub flow with plan name and multiple repositories
    public StudyPlan(int userId, String planName, String repositoryNames, LocalDate deadline,
                     String difficulty, int dailyHours) {
        this.userId = userId;
        this.planName = planName;
        this.repositoryName = repositoryNames;
        this.deadline = deadline;
        this.difficulty = difficulty;
        this.dailyHours = dailyHours;
        this.completionPercentage = 0;
        this.aiGenerated = false;
    }

    // Constructor for GitHub flow (legacy - single repository)
    public StudyPlan(int userId, String repositoryName, LocalDate deadline,
                     String difficulty, int dailyHours) {
        this.userId = userId;
        this.repositoryName = repositoryName;
        this.planName = repositoryName;
        this.deadline = deadline;
        this.difficulty = difficulty;
        this.dailyHours = dailyHours;
        this.completionPercentage = 0;
        this.aiGenerated = false;
    }

    // Constructor for Google flow with plan name
    public StudyPlan(int userId, String planName, LocalDate deadline, String subjects,
                     String difficulty, int dailyHours) {
        this.userId = userId;
        this.planName = planName;
        this.subjects = subjects;
        this.deadline = deadline;
        this.difficulty = difficulty;
        this.dailyHours = dailyHours;
        this.completionPercentage = 0;
        this.aiGenerated = false;
    }

    // Constructor for Google flow (legacy - without plan name)
    public StudyPlan(int userId, LocalDate deadline, String subjects,
                     String difficulty, int dailyHours) {
        this.userId = userId;
        this.subjects = subjects;
        this.planName = subjects;
        this.deadline = deadline;
        this.difficulty = difficulty;
        this.dailyHours = dailyHours;
        this.completionPercentage = 0;
        this.aiGenerated = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public String getRepositoryName() { return repositoryName; }
    public void setRepositoryName(String repositoryName) { this.repositoryName = repositoryName; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getSubjects() { return subjects; }
    public void setSubjects(String subjects) { this.subjects = subjects; }

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

    public int getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(int completionPercentage) { this.completionPercentage = completionPercentage; }

    public boolean isAiGenerated() { return aiGenerated; }
    public void setAiGenerated(boolean aiGenerated) { this.aiGenerated = aiGenerated; }
}