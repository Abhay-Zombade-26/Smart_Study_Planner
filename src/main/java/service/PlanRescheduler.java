package service;

import model.StudyPlan;
import model.StudyTask;
import dao.StudyTaskDAO;
import dao.StudyPlanDAO;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class PlanRescheduler {

    private final StudyTaskDAO studyTaskDAO;
    private final StudyPlanDAO studyPlanDAO;

    public PlanRescheduler() {
        this.studyTaskDAO = new StudyTaskDAO();
        this.studyPlanDAO = new StudyPlanDAO();
    }

    /**
     * Result object containing rescheduling results
     */
    public static class RescheduleResult {
        public final boolean success;
        public final String message;
        public final int rescheduledCount;
        public final Map<LocalDate, Integer> newDistribution;

        public RescheduleResult(boolean success, String message, int rescheduledCount, Map<LocalDate, Integer> newDistribution) {
            this.success = success;
            this.message = message;
            this.rescheduledCount = rescheduledCount;
            this.newDistribution = newDistribution;
        }
    }

    /**
     * Main method to intelligently reschedule missed tasks
     * @param planId The ID of the plan to reschedule
     * @param missedTasks List of missed tasks to reschedule
     * @return RescheduleResult with details of the operation
     */
    public RescheduleResult rescheduleMissedTasks(int planId, List<StudyTask> missedTasks) {
        if (missedTasks == null || missedTasks.isEmpty()) {
            return new RescheduleResult(false, "No missed tasks to reschedule", 0, null);
        }

        // Get the study plan (fresh from database)
        StudyPlan plan = studyPlanDAO.findById(planId);
        if (plan == null) {
            return new RescheduleResult(false, "Study plan not found", 0, null);
        }

        LocalDate today = LocalDate.now();
        LocalDate deadline = plan.getDeadline();

        // Calculate days remaining
        long daysRemaining = ChronoUnit.DAYS.between(today, deadline);
        if (daysRemaining <= 0) {
            return new RescheduleResult(false, "Deadline has passed. Cannot reschedule tasks.", 0, null);
        }

        // Get all existing tasks from today until deadline
        List<StudyTask> allFutureTasks = new ArrayList<>();
        for (int i = 0; i < daysRemaining; i++) {
            LocalDate date = today.plusDays(i);
            allFutureTasks.addAll(studyTaskDAO.findTasksByDate(planId, date));
        }

        // Calculate maximum capacity per day based on daily hours
        int maxTasksPerDay = plan.getDailyHours();

        // Calculate if we can fit all missed tasks
        int totalMissed = missedTasks.size();
        long availableDays = daysRemaining;
        long currentTotalTasks = allFutureTasks.size();
        long newTotalTasks = currentTotalTasks + totalMissed;

        double averageTasksPerDay = (double) newTotalTasks / availableDays;

        // If workload is too high, return detailed message
        if (averageTasksPerDay > maxTasksPerDay) {
            return new RescheduleResult(false,
                    String.format("Cannot reschedule: Would require %.1f tasks/day, but max is %d.\n" +
                                    "Current daily hours: %d\n" +
                                    "Please increase daily hours in 'Update Plan' and try again.",
                            averageTasksPerDay, maxTasksPerDay, plan.getDailyHours()),
                    totalMissed, null);
        }

        // Intelligent distribution algorithm
        Map<LocalDate, List<StudyTask>> distribution = distributeTasksIntelligently(
                planId, today, (int) daysRemaining, allFutureTasks, missedTasks, maxTasksPerDay);

        // Apply the distribution to database
        int rescheduled = applyDistribution(planId, distribution);

        // Calculate new distribution summary
        Map<LocalDate, Integer> summary = new TreeMap<>();
        for (Map.Entry<LocalDate, List<StudyTask>> entry : distribution.entrySet()) {
            summary.put(entry.getKey(), entry.getValue().size());
        }

        return new RescheduleResult(true,
                String.format("Successfully rescheduled %d missed tasks across %d days",
                        rescheduled, summary.size()),
                rescheduled, summary);
    }
    /**
     * Calculate current task load per day
     */
    private Map<LocalDate, Integer> calculateDailyLoad(List<StudyTask> tasks) {
        Map<LocalDate, Integer> load = new HashMap<>();
        for (StudyTask task : tasks) {
            LocalDate date = task.getTaskDate();
            load.put(date, load.getOrDefault(date, 0) + 1);
        }
        return load;
    }

    /**
     * Intelligently distribute tasks across remaining days
     * Updated to add [rescheduled] tag only to moved tasks
     */
    private Map<LocalDate, List<StudyTask>> distributeTasksIntelligently(
            int planId, LocalDate startDate, int daysRemaining,
            List<StudyTask> existingTasks, List<StudyTask> missedTasks, int maxTasksPerDay) {

        Map<LocalDate, List<StudyTask>> distribution = new TreeMap<>();

        // Initialize distribution with existing tasks
        for (StudyTask task : existingTasks) {
            LocalDate date = task.getTaskDate();
            distribution.computeIfAbsent(date, k -> new ArrayList<>()).add(task);
        }

        // Sort missed tasks by date (oldest first)
        missedTasks.sort(Comparator.comparing(StudyTask::getTaskDate));

        // Calculate target tasks per day (aim for even distribution)
        int totalTasks = existingTasks.size() + missedTasks.size();
        int baseTasksPerDay = totalTasks / daysRemaining;
        int remainderTasks = totalTasks % daysRemaining;

        // Distribute missed tasks
        int missedIndex = 0;
        for (int dayOffset = 0; dayOffset < daysRemaining && missedIndex < missedTasks.size(); dayOffset++) {
            LocalDate currentDate = startDate.plusDays(dayOffset);

            // Get current tasks for this day
            List<StudyTask> dayTasks = distribution.getOrDefault(currentDate, new ArrayList<>());
            int currentCount = dayTasks.size();

            // Calculate target for this day
            int targetForDay = baseTasksPerDay + (dayOffset < remainderTasks ? 1 : 0);
            int canAdd = Math.max(0, targetForDay - currentCount);

            // Add missed tasks up to capacity
            for (int i = 0; i < canAdd && missedIndex < missedTasks.size(); i++) {
                StudyTask missedTask = missedTasks.get(missedIndex);

                // 🔥 UPDATED: Add [rescheduled] tag only to tasks being moved
                String newDescription;
                if (missedTask.getTaskDate().isBefore(LocalDate.now())) {
                    newDescription = missedTask.getDescription() + " [rescheduled]";
                } else {
                    newDescription = missedTask.getDescription();
                }

                // Create a new task for this date
                StudyTask newTask = new StudyTask(
                        missedTask.getGoalId(),
                        currentDate,
                        newDescription,
                        missedTask.isRequiredCommit(),
                        missedTask.getTopicId(),
                        missedTask.getSessionType()
                );

                dayTasks.add(newTask);
                missedIndex++;
            }

            distribution.put(currentDate, dayTasks);
        }

        // If any missed tasks remain, add them to the last day
        if (missedIndex < missedTasks.size()) {
            LocalDate lastDate = startDate.plusDays(daysRemaining - 1);
            List<StudyTask> lastDayTasks = distribution.getOrDefault(lastDate, new ArrayList<>());

            while (missedIndex < missedTasks.size()) {
                StudyTask missedTask = missedTasks.get(missedIndex);

                // 🔥 UPDATED: Add [rescheduled] tag only to tasks being moved
                String newDescription;
                if (missedTask.getTaskDate().isBefore(LocalDate.now())) {
                    newDescription = missedTask.getDescription() + " [rescheduled]";
                } else {
                    newDescription = missedTask.getDescription();
                }

                StudyTask newTask = new StudyTask(
                        missedTask.getGoalId(),
                        lastDate,
                        newDescription,
                        missedTask.isRequiredCommit(),
                        missedTask.getTopicId(),
                        missedTask.getSessionType()
                );
                lastDayTasks.add(newTask);
                missedIndex++;
            }
            distribution.put(lastDate, lastDayTasks);
        }

        return distribution;
    }

    /**
     * Apply the new distribution to the database
     */
    /**
     * Apply the new distribution to the database
     */
    private int applyDistribution(int planId, Map<LocalDate, List<StudyTask>> distribution) {
        // Delete all existing tasks for this plan
        studyTaskDAO.deleteByGoalId(planId);

        int taskCount = 0;

        // Save all tasks from the new distribution
        for (Map.Entry<LocalDate, List<StudyTask>> entry : distribution.entrySet()) {
            for (StudyTask task : entry.getValue()) {
                studyTaskDAO.save(task);
                taskCount++;
            }
        }

        return taskCount;
    }
    /**
     * Helper method to check if rescheduling is possible without changes
     */
    public boolean canReschedule(int planId, List<StudyTask> missedTasks) {
        StudyPlan plan = studyPlanDAO.findById(planId);
        if (plan == null) return false;

        LocalDate today = LocalDate.now();
        long daysRemaining = ChronoUnit.DAYS.between(today, plan.getDeadline());
        if (daysRemaining <= 0) return false;

        int totalMissed = missedTasks.size();
        int maxTasksPerDay = plan.getDailyHours();

        // Rough estimate: can we fit them?
        return (double) totalMissed / daysRemaining <= maxTasksPerDay * 1.5; // Allow 50% buffer
    }
}