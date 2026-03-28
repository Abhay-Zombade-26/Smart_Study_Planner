package service;

import model.User;
import model.Goal;
import model.DailyTask;
import model.StudyPlan;
import model.StudyTask;
import model.FeatureTemplate;
import dao.GoalDAO;
import dao.StudyTaskDAO;
import dao.StudyPlanDAO;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudyPlanGenerator {

    private StudyTaskDAO taskDAO;
    private GoalDAO goalDAO;
    private StudyPlanDAO planDAO;
    private ProjectTypeDetector typeDetector;
    private TemplateLibrary templateLibrary;

    public StudyPlanGenerator() {
        this.taskDAO = new StudyTaskDAO();
        this.goalDAO = new GoalDAO();
        this.planDAO = new StudyPlanDAO();
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
            System.out.println("✅ Goal created for: " + repoName + " (Type: " + projectType + ")");

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

    public void generatePlanWithName(User user, String planName, String repositoryNames,
                                     List<String> priorities, List<String> features,
                                     int durationMonths, int dailyHours,
                                     List<String> experienceLevels) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(durationMonths);

        // Create a single plan with multiple repositories
        StudyPlan plan = new StudyPlan();
        plan.setUserId(user.getId());
        plan.setPlanName(planName);
        plan.setRepositoryName(repositoryNames);
        plan.setDeadline(endDate);
        plan.setDifficulty(experienceLevels.get(0)); // Use first experience level
        plan.setDailyHours(dailyHours);
        plan.setCompletionPercentage(0);
        plan.setAiGenerated(false);

        StudyPlan savedPlan = planDAO.save(plan);

        if (savedPlan != null) {
            // Generate tasks for each repository
            String[] repos = repositoryNames.split(", ");
            for (int i = 0; i < repos.length; i++) {
                String repoName = repos[i];
                String featureList = i < features.size() ? features.get(i) : "";
                List<StudyTask> tasks = generateTasksForPlan(savedPlan, featureList, durationMonths, dailyHours);
                taskDAO.saveAll(tasks);
                System.out.println("✅ Generated " + tasks.size() + " tasks for repository: " + repoName);
            }
        }
    }

    private List<StudyTask> generateTasksForPlan(StudyPlan plan, String features, int durationMonths, int dailyHours) {
        List<StudyTask> tasks = new ArrayList<>();

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(durationMonths);
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);

        String[] featureArray = features.split(",");

        int taskIndex = 0;
        for (String feature : featureArray) {
            feature = feature.trim();
            if (feature.isEmpty()) continue;

            // Allocate 3-5 days per feature
            int daysPerFeature = 4;

            for (int day = 0; day < daysPerFeature && taskIndex < totalDays; day++) {
                LocalDate taskDate = startDate.plusDays(taskIndex);

                StudyTask task = new StudyTask();
                task.setGoalId(plan.getId());
                task.setTaskDate(taskDate);
                task.setDescription("Implement " + feature + " - Day " + (day + 1));
                task.setRequiredCommit(true);
                task.setStatus("PENDING");
                task.setTopicId(0);
                task.setSessionType("CODING");

                tasks.add(task);
                taskIndex++;
            }
        }

        // Fill remaining days with general tasks
        while (taskIndex < totalDays) {
            LocalDate taskDate = startDate.plusDays(taskIndex);

            StudyTask task = new StudyTask();
            task.setGoalId(plan.getId());
            task.setTaskDate(taskDate);
            task.setDescription("General development and testing");
            task.setRequiredCommit(true);
            task.setStatus("PENDING");
            task.setTopicId(0);
            task.setSessionType("CODING");

            tasks.add(task);
            taskIndex++;
        }

        return tasks;
    }

    private void generateTasksForFeatures(User user, Goal goal, String[] features,
                                          ProjectTypeDetector.ProjectType projectType,
                                          double totalHours, LocalDate startDate, LocalDate endDate,
                                          int dailyHours) {

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
                    System.out.println("📅 Day " + (currentDay+1) + ": " + task.getDescription());

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
                    System.out.println("📅 Day " + (currentDay+1) + ": " + genericTask);

                    currentDay++;
                }
            }
        }
    }

    public StudyPlan generatePlan(User user, String target, LocalDate deadline, int dailyHours, String difficulty) {
        StudyPlan plan = new StudyPlan(user.getId(), target, deadline, difficulty, dailyHours);
        return planDAO.save(plan);
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