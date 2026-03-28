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

    public RescheduleResult rescheduleMissedTasks(int planId, List<StudyTask> missedTasks) {
        if (missedTasks == null || missedTasks.isEmpty()) {
            return new RescheduleResult(false, "No missed tasks to reschedule", 0, null);
        }

        StudyPlan plan = studyPlanDAO.findById(planId);
        if (plan == null) {
            return new RescheduleResult(false, "Study plan not found", 0, null);
        }

        LocalDate today = LocalDate.now();
        LocalDate deadline = plan.getDeadline();

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

        int maxTasksPerDay = plan.getDailyHours();
        int totalMissed = missedTasks.size();
        long availableDays = daysRemaining;
        long currentTotalTasks = allFutureTasks.size();
        long newTotalTasks = currentTotalTasks + totalMissed;
        double averageTasksPerDay = (double) newTotalTasks / availableDays;

        if (averageTasksPerDay > maxTasksPerDay) {
            return new RescheduleResult(false,
                    String.format("Cannot reschedule: Would require %.1f tasks/day, but max is %d.\n" +
                                    "Current daily hours: %d\n" +
                                    "Please increase daily hours in 'Update Plan' and try again.",
                            averageTasksPerDay, maxTasksPerDay, plan.getDailyHours()),
                    totalMissed, null);
        }

        Map<LocalDate, List<StudyTask>> distribution = distributeTasksIntelligently(
                planId, today, (int) daysRemaining, allFutureTasks, missedTasks, maxTasksPerDay);

        int rescheduled = applyDistribution(planId, distribution);

        Map<LocalDate, Integer> summary = new TreeMap<>();
        for (Map.Entry<LocalDate, List<StudyTask>> entry : distribution.entrySet()) {
            summary.put(entry.getKey(), entry.getValue().size());
        }

        return new RescheduleResult(true,
                String.format("Successfully rescheduled %d missed tasks across %d days",
                        rescheduled, summary.size()),
                rescheduled, summary);
    }

    private Map<LocalDate, List<StudyTask>> distributeTasksIntelligently(
            int planId, LocalDate startDate, int daysRemaining,
            List<StudyTask> existingTasks, List<StudyTask> missedTasks, int maxTasksPerDay) {

        Map<LocalDate, List<StudyTask>> distribution = new TreeMap<>();

        for (StudyTask task : existingTasks) {
            LocalDate date = task.getTaskDate();
            distribution.computeIfAbsent(date, k -> new ArrayList<>()).add(task);
        }

        missedTasks.sort(Comparator.comparing(StudyTask::getTaskDate));

        int totalTasks = existingTasks.size() + missedTasks.size();
        int baseTasksPerDay = totalTasks / daysRemaining;
        int remainderTasks = totalTasks % daysRemaining;

        int missedIndex = 0;
        for (int dayOffset = 0; dayOffset < daysRemaining && missedIndex < missedTasks.size(); dayOffset++) {
            LocalDate currentDate = startDate.plusDays(dayOffset);
            List<StudyTask> dayTasks = distribution.getOrDefault(currentDate, new ArrayList<>());
            int currentCount = dayTasks.size();
            int targetForDay = baseTasksPerDay + (dayOffset < remainderTasks ? 1 : 0);
            int canAdd = Math.max(0, targetForDay - currentCount);

            for (int i = 0; i < canAdd && missedIndex < missedTasks.size(); i++) {
                StudyTask missedTask = missedTasks.get(missedIndex);
                String newDescription;
                if (missedTask.getTaskDate().isBefore(LocalDate.now())) {
                    newDescription = missedTask.getDescription() + " [rescheduled]";
                } else {
                    newDescription = missedTask.getDescription();
                }

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

        if (missedIndex < missedTasks.size()) {
            LocalDate lastDate = startDate.plusDays(daysRemaining - 1);
            List<StudyTask> lastDayTasks = distribution.getOrDefault(lastDate, new ArrayList<>());
            while (missedIndex < missedTasks.size()) {
                StudyTask missedTask = missedTasks.get(missedIndex);
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

    private int applyDistribution(int planId, Map<LocalDate, List<StudyTask>> distribution) {
        studyTaskDAO.deleteByGoalId(planId);
        System.out.println("Deleted existing tasks for plan " + planId);

        int taskCount = 0;
        for (Map.Entry<LocalDate, List<StudyTask>> entry : distribution.entrySet()) {
            for (StudyTask task : entry.getValue()) {
                studyTaskDAO.save(task);
                taskCount++;
            }
        }
        System.out.println("Saved " + taskCount + " tasks after rescheduling");
        return taskCount;
    }

    public boolean canReschedule(int planId, List<StudyTask> missedTasks) {
        StudyPlan plan = studyPlanDAO.findById(planId);
        if (plan == null) return false;

        LocalDate today = LocalDate.now();
        long daysRemaining = ChronoUnit.DAYS.between(today, plan.getDeadline());
        if (daysRemaining <= 0) return false;

        int totalMissed = missedTasks.size();
        int maxTasksPerDay = plan.getDailyHours();
        return (double) totalMissed / daysRemaining <= maxTasksPerDay * 1.5;
    }
}