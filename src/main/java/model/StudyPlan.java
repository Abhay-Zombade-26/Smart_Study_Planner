package model;

import java.time.LocalDate;
import java.util.List;

public class StudyPlan {
    private int id;
    private int userId;
    private String repositoryName;   // IT mode (GitHub repository)
    private String subjectName;      // Legacy single subject (may be null)
    private String subjects;         // Normal mode: comma-separated subjects
    private LocalDate deadline;
    private String difficulty;
    private int dailyHours;
    private int completionPercentage;
    private List<StudyTask> tasks;

    public StudyPlan() {}

    // Constructor for IT mode (with dummy boolean to differentiate)
    public StudyPlan(int userId, String repositoryName, LocalDate deadline, String difficulty, int dailyHours, boolean isIT) {
        this.userId = userId;
        this.repositoryName = repositoryName;
        this.deadline = deadline;
        this.difficulty = difficulty;
        this.dailyHours = dailyHours;
        this.completionPercentage = 0;
    }

    // Constructor for Normal mode with multiple subjects
    public StudyPlan(int userId, String subjects, LocalDate deadline, String difficulty, int dailyHours) {
        this.userId = userId;
        this.subjects = subjects;
        this.deadline = deadline;
        this.difficulty = difficulty;
        this.dailyHours = dailyHours;
        this.completionPercentage = 0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getRepositoryName() { return repositoryName; }
    public void setRepositoryName(String repositoryName) { this.repositoryName = repositoryName; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getSubjects() { return subjects; }
    public void setSubjects(String subjects) { this.subjects = subjects; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getDailyHours() { return dailyHours; }
    public void setDailyHours(int dailyHours) { this.dailyHours = dailyHours; }

    public int getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(int completionPercentage) { this.completionPercentage = completionPercentage; }

    public List<StudyTask> getTasks() { return tasks; }
    public void setTasks(List<StudyTask> tasks) { this.tasks = tasks; }
}