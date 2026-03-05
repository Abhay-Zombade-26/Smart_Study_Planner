package service;

import model.User;
import model.Goal;
import model.DailyTask;
import model.FeatureTemplate;
import dao.GoalDAO;
import dao.StudyTaskDAO;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudyPlanGenerator {
    
    private StudyTaskDAO taskDAO;
    private GoalDAO goalDAO;
    private ProjectTypeDetector typeDetector;
    private TemplateLibrary templateLibrary;
    
    public StudyPlanGenerator() {
        this.taskDAO = new StudyTaskDAO();
        this.goalDAO = new GoalDAO();
        this.typeDetector = new ProjectTypeDetector();
        this.templateLibrary = new TemplateLibrary();
    }
    
    public void generatePlan(User user, List<String> selectedRepos, 
                            List<String> priorities, List<String> features,
                            int durationMonths, int dailyHours, 
                            List<String> experienceLevels) {
        
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(durationMonths);
        int totalDays = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        int totalAvailableHours = totalDays * dailyHours;
        
        // Calculate total priority weight
        double totalWeight = 0;
        for (String priority : priorities) {
            double weight = getPriorityWeight(priority);
            totalWeight += weight;
        }
        
        // Create goal for each repository
        for (int i = 0; i < selectedRepos.size(); i++) {
            String repoName = selectedRepos.get(i);
            String priority = priorities.get(i);
            String featureList = features.get(i);
            String experienceLevel = experienceLevels.get(i);
            
            // Detect project type
            ProjectTypeDetector.ProjectType projectType = 
                typeDetector.detectProjectType(user.getAccessToken(), repoName);
            
            // Create goal
            Goal goal = new Goal(user.getId(), repoName, priority, featureList, 
                                durationMonths, dailyHours, experienceLevel);
            goalDAO.save(goal);
            System.out.println("? Goal created for: " + repoName + " (Type: " + projectType + ")");
            
            // Parse features
            String[] featureArray = featureList.split(",");
            
            // Allocate hours per feature based on priority
            double repoWeight = getPriorityWeight(priority);
            double repoHours = (repoWeight / totalWeight) * totalAvailableHours;
            
            // Generate tasks for each feature
            generateTasksForFeatures(user, goal, featureArray, projectType, 
                                    repoHours, startDate, endDate, dailyHours);
        }
    }
    
    private void generateTasksForFeatures(User user, Goal goal, String[] features,
                                         ProjectTypeDetector.ProjectType projectType,
                                         double totalHours, LocalDate startDate, LocalDate endDate,
                                         int dailyHours) {  // FIXED: Added dailyHours parameter
        
        int totalDays = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        int currentDay = 0;
        
        for (String feature : features) {
            feature = feature.trim();
            
            // Find template for this feature
            FeatureTemplate template = templateLibrary.findTemplate(feature, projectType.toString());
            
            if (template != null) {
                // Use template to generate subtasks
                double featureHours = (template.getBaseHours() * goal.getExperienceMultiplier());
                
                for (FeatureTemplate.TaskTemplate subTask : template.getSubTasks()) {
                    if (currentDay >= totalDays) break;
                    
                    LocalDate taskDate = startDate.plusDays(currentDay);
                    
                    DailyTask task = new DailyTask();
                    task.setUserId(user.getId());
                    task.setGoalId(goal.getId());
                    task.setRepositoryName(goal.getRepositoryName());
                    task.setTaskDate(taskDate);
                    task.setPlannedHours((int) Math.ceil(featureHours * subTask.getWeight()));
                    task.setPlannedCommits(1);
                    task.setStatus("PENDING");
                    task.setDescription(subTask.getDescription() + " for " + feature);
                    
                    // Store expected file paths (will be used for verification)
                    String[] possibleFiles = subTask.getPossibleFiles();
                    if (possibleFiles.length > 0) {
                        task.setDescription(task.getDescription() + " - Expected files: " + 
                                          String.join(", ", possibleFiles));
                    }
                    
                    taskDAO.save(task);
                    System.out.println("?? Day " + (currentDay+1) + ": " + task.getDescription());
                    
                    currentDay++;
                }
            } else {
                // No template found - create generic tasks
                String[] genericTasks = {
                    "Design and plan " + feature,
                    "Implement core functionality for " + feature,
                    "Test and debug " + feature,
                    "Document " + feature,
                    "Review and refactor " + feature
                };
                
                for (String genericTask : genericTasks) {
                    if (currentDay >= totalDays) break;
                    
                    LocalDate taskDate = startDate.plusDays(currentDay);
                    
                    DailyTask task = new DailyTask();
                    task.setUserId(user.getId());
                    task.setGoalId(goal.getId());
                    task.setRepositoryName(goal.getRepositoryName());
                    task.setTaskDate(taskDate);
                    task.setPlannedHours(dailyHours);
                    task.setPlannedCommits(1);
                    task.setStatus("PENDING");
                    task.setDescription(genericTask);
                    
                    taskDAO.save(task);
                    System.out.println("?? Day " + (currentDay+1) + ": " + genericTask);
                    
                    currentDay++;
                }
            }
        }
    }
    
    private double getPriorityWeight(String priority) {
        switch(priority) {
            case "HIGH": return 1.5;
            case "MEDIUM": return 1.0;
            case "LOW": return 0.5;
            default: return 1.0;
        }
    }
}
