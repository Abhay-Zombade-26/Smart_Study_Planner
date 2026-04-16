package model;

import java.time.LocalDate;

public class StudyPlan {
    private int id;
    private int userId;
    private String planName;
    private String repositoryName;
    private String subjectName;
    private String subjects;
    private LocalDate deadline;
    private int dailyHours;
    private String difficulty;
    private LocalDate createdAt;
    private String status;
    private int completionPercentage;
    private boolean aiGenerated;
    private String userRole;
    private String role;
    private String loginType;

    public StudyPlan() {}

    // Constructor for GitHub flow (IT Student)
    public StudyPlan(int userId, String planName, String repositoryNames, LocalDate deadline,
                     String difficulty, int dailyHours) {
        this.userId = userId;
        this.planName = planName;
        this.repositoryName = repositoryNames;
        this.deadline = deadline;
        this.difficulty = difficulty;
        this.dailyHours = dailyHours;
        this.completionPercentage = 0;
        this.aiGenerated = true;
        this.userRole = "IT";
        this.role = "IT";
        this.loginType = "GITHUB";
    }

    // Constructor for Google flow (Normal Student)
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
        this.userRole = "NORMAL";
        this.role = "NORMAL";
        this.loginType = "GOOGLE";
    }

    // Legacy constructor
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
        this.userRole = "IT";
        this.role = "IT";
        this.loginType = "GITHUB";
    }

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
        this.userRole = "NORMAL";
        this.role = "NORMAL";
        this.loginType = "GOOGLE";
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

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getLoginType() { return loginType; }
    public void setLoginType(String loginType) { this.loginType = loginType; }

    @Override
    public String toString() {
        return "StudyPlan{" +
                "id=" + id +
                ", userId=" + userId +
                ", planName='" + planName + '\'' +
                ", role='" + role + '\'' +
                ", loginType='" + loginType + '\'' +
                '}';
    }
}