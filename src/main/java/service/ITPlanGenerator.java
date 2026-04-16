package service;

import dao.StudyPlanDAO;
import dao.StudyTaskDAO;
import model.StudyPlan;
import model.StudyTask;
import model.User;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ITPlanGenerator implements PlanStrategyService {

    private final StudyPlanDAO studyPlanDAO = new StudyPlanDAO();
    private final StudyTaskDAO studyTaskDAO = new StudyTaskDAO();

    @Override
    public StudyPlan generatePlan(User user, String repository, LocalDate deadline,
                                  int dailyHours, String difficulty) {

        StudyPlan plan = new StudyPlan(user.getId(), repository, deadline, difficulty, dailyHours);
        plan = studyPlanDAO.save(plan);

        List<StudyTask> tasks = generateTasks(plan);
        studyTaskDAO.saveAll(tasks);
        plan.setTasks(tasks);

        return plan;
    }

    @Override
    public List<StudyTask> generateTasks(StudyPlan plan) {
        List<StudyTask> tasks = new ArrayList<>();
        LocalDate today = LocalDate.now();
        long daysRemaining = ChronoUnit.DAYS.between(today, plan.getDeadline());

        if (daysRemaining <= 0) {
            return tasks;
        }

        int totalDays = (int) daysRemaining;
        int difficultyFactor = getDifficultyFactor(plan.getDifficulty());
        int dailyHours = plan.getDailyHours();

        // Generate tasks for each day
        for (int i = 0; i < totalDays; i++) {
            LocalDate taskDate = today.plusDays(i);

            // Generate main task
            String taskDescription = generateTaskDescription(i, totalDays, dailyHours, difficultyFactor);
            boolean requiresCommit = shouldRequireCommit(i, totalDays, dailyHours);

            StudyTask task = new StudyTask(
                    plan.getId(),
                    taskDate,
                    taskDescription,
                    requiresCommit
            );

            tasks.add(task);

            // Add milestone checkpoints
            if (isMilestoneDay(i, totalDays)) {
                StudyTask milestoneTask = new StudyTask(
                        plan.getId(),
                        taskDate,
                        generateMilestoneDescription(i, totalDays),
                        false
                );
                tasks.add(milestoneTask);
            }
        }

        return tasks;
    }

    @Override
    public void adjustPlan(StudyPlan plan, boolean commitMissed) {
        List<StudyTask> pendingTasks = studyTaskDAO.findByGoalId(plan.getId())
                .stream()
                .filter(t -> t.getStatus().equals("PENDING"))
                .toList();

        if (pendingTasks.isEmpty()) return;

        long remainingDays = ChronoUnit.DAYS.between(LocalDate.now(), plan.getDeadline());
        if (remainingDays <= 0) return;

        // Redistribute remaining tasks
        int tasksPerDay = (int) Math.ceil((double) pendingTasks.size() / remainingDays);

        // Clear old tasks and regenerate
        for (StudyTask task : pendingTasks) {
            task.setTaskDate(LocalDate.now().plusDays(
                    (int) (pendingTasks.indexOf(task) / tasksPerDay)
            ));

            if (commitMissed) {
                task.setDescription("[ADJUSTED] " + task.getDescription() +
                        " (Increased workload due to missed commit)");
            }

            studyTaskDAO.save(task);
        }
    }

    private String generateTaskDescription(int dayIndex, int totalDays, int dailyHours, int difficultyFactor) {
        String[] tasks = {
                "Implement core functionality",
                "Write unit tests",
                "Fix bugs and optimize code",
                "Add documentation",
                "Refactor code",
                "Implement new feature",
                "Review pull requests",
                "Update dependencies",
                "Improve performance",
                "Add error handling"
        };

        String[] algorithms = {
                "Implement Binary Search algorithm",
                "Solve Array manipulation problems",
                "Work on String algorithms",
                "Practice Dynamic Programming",
                "Implement Sorting algorithms",
                "Work on Graph algorithms",
                "Practice Tree traversals",
                "Implement Stack and Queue operations",
                "Work on Hash Table problems",
                "Practice Recursion problems"
        };

        String baseTask;
        if (dailyHours >= 4) {
            baseTask = algorithms[dayIndex % algorithms.length];
        } else {
            baseTask = tasks[dayIndex % tasks.length];
        }

        int intensity = difficultyFactor * dailyHours;
        if (intensity > 6) {
            baseTask = "Advanced: " + baseTask + " with optimizations";
        } else if (intensity > 3) {
            baseTask = "Standard: " + baseTask;
        } else {
            baseTask = "Basic: " + baseTask;
        }

        return baseTask + " (Est. time: " + dailyHours + " hours)";
    }

    private String generateMilestoneDescription(int dayIndex, int totalDays) {
        double progress = (double) (dayIndex + 1) / totalDays * 100;

        if (progress >= 75) {
            return "🎯 FINAL MILESTONE: Complete final testing and prepare for submission";
        } else if (progress >= 50) {
            return "⭐ MID-PROJECT: Review all completed features and plan remaining work";
        } else if (progress >= 25) {
            return "📊 WEEKLY CHECKPOINT: Evaluate progress and adjust schedule if needed";
        }

        return "✅ CHECKPOINT: Verify today's commits and update project documentation";
    }

    private boolean shouldRequireCommit(int dayIndex, int totalDays, int dailyHours) {
        // Require commit every 1-2 days based on intensity
        int commitFrequency = Math.max(1, 3 - (dailyHours / 3));
        return dayIndex % commitFrequency == 0;
    }

    private boolean isMilestoneDay(int dayIndex, int totalDays) {
        return dayIndex == 0 ||
                dayIndex == totalDays / 4 ||
                dayIndex == totalDays / 2 ||
                dayIndex == (totalDays * 3) / 4 ||
                dayIndex == totalDays - 1;
    }

    private int getDifficultyFactor(String difficulty) {
        return switch (difficulty.toUpperCase()) {
            case "EASY" -> 1;
            case "MODERATE" -> 2;
            case "HARD" -> 3;
            default -> 2;
        };
    }
}