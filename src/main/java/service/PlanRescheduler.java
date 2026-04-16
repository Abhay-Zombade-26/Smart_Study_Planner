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

        long daysRemaining = ChronoUnit.DAYS.between(today, deadline) + 1;
        if (daysRemaining <= 0) {
            return new RescheduleResult(false, "Deadline has passed. Cannot reschedule tasks.", 0, null);
        }

        System.out.println("\n=== SMART RESCHEDULING ===");
        System.out.println("Plan ID: " + planId);
        System.out.println("Missed tasks: " + missedTasks.size());
        System.out.println("Days remaining: " + daysRemaining);
        System.out.println("Max tasks/day: " + plan.getDailyHours());

        // Get count of tasks per day for remaining days
        Map<LocalDate, Integer> tasksPerDay = new HashMap<>();
        List<StudyTask> allPendingTasks = new ArrayList<>();

        for (int i = 0; i < daysRemaining; i++) {
            LocalDate date = today.plusDays(i);
            List<StudyTask> dayTasks = studyTaskDAO.findTasksByDate(planId, date);

            // Count only PENDING tasks
            int pendingCount = 0;
            for (StudyTask task : dayTasks) {
                if (!"COMPLETED".equals(task.getStatus())) {
                    pendingCount++;
                    allPendingTasks.add(task);
                }
            }
            tasksPerDay.put(date, pendingCount);
        }

        // Find available slots
        List<LocalDate> availableSlots = new ArrayList<>();
        for (Map.Entry<LocalDate, Integer> entry : tasksPerDay.entrySet()) {
            int currentTasks = entry.getValue();
            int slotsAvailable = plan.getDailyHours() - currentTasks;

            for (int i = 0; i < slotsAvailable; i++) {
                availableSlots.add(entry.getKey());
            }
        }

        System.out.println("Available slots found: " + availableSlots.size());

        // Check if we have enough slots
        if (availableSlots.size() < missedTasks.size()) {
            int shortage = missedTasks.size() - availableSlots.size();
            return new RescheduleResult(false,
                    String.format("❌ Cannot reschedule all missed tasks!\n\n" +
                                    "• Missed tasks: %d\n" +
                                    "• Available slots: %d\n" +
                                    "• Shortage: %d slots\n\n" +
                                    "💡 Solutions:\n" +
                                    "1. Increase daily hours to %d+\n" +
                                    "2. Extend deadline by %d+ days\n" +
                                    "3. Complete some pending tasks first",
                            missedTasks.size(), availableSlots.size(), shortage,
                            plan.getDailyHours() + (int)Math.ceil((double)shortage / daysRemaining),
                            (int)Math.ceil((double)shortage / plan.getDailyHours())),
                    missedTasks.size(), null);
        }

        // MARK ORIGINAL MISSED TASKS AS RESCHEDULED
        for (StudyTask missedTask : missedTasks) {
            studyTaskDAO.updateStatus(missedTask.getId(), "RESCHEDULED");
            System.out.println("  Marked task " + missedTask.getId() + " as RESCHEDULED");
        }

        // Reschedule missed tasks into available slots
        int rescheduled = 0;
        Map<LocalDate, Integer> newTasksAdded = new HashMap<>();

        // Shuffle available slots to distribute evenly
        Collections.shuffle(availableSlots);

        for (StudyTask missedTask : missedTasks) {
            if (availableSlots.isEmpty()) break;

            LocalDate newDate = availableSlots.remove(0);

            // Create new task with clean description (no status text)
            String description = missedTask.getDescription();
            // Remove any existing status text
            description = description.replace(" [rescheduled]", "")
                    .replace(" (Missed)", "")
                    .replace(" (Rescheduled)", "");

            StudyTask newTask = new StudyTask(
                    missedTask.getGoalId(),
                    newDate,
                    description,
                    missedTask.isRequiredCommit(),
                    missedTask.getTopicId(),
                    missedTask.getSessionType()
            );
            newTask.setStatus("PENDING");

            // Save the new task
            studyTaskDAO.save(newTask);
            rescheduled++;

            // Track additions
            newTasksAdded.put(newDate, newTasksAdded.getOrDefault(newDate, 0) + 1);

            System.out.println("  Created new task: " + description + " on " + newDate);
        }

        System.out.println("✅ Successfully rescheduled " + rescheduled + " tasks");
        System.out.println("=== RESCHEDULING COMPLETE ===\n");

        // Build success message
        StringBuilder distribution = new StringBuilder();
        for (Map.Entry<LocalDate, Integer> entry : newTasksAdded.entrySet()) {
            distribution.append(String.format("  • %s: +%d task(s)\n",
                    entry.getKey(), entry.getValue()));
        }

        return new RescheduleResult(true,
                String.format("✅ Successfully rescheduled %d missed task(s)!\n\n" +
                                "📅 New tasks added to:\n%s\n" +
                                "📌 Original missed tasks marked as 'Rescheduled'",
                        rescheduled, distribution.toString()),
                rescheduled, newTasksAdded);
    }

    public boolean canReschedule(int planId, List<StudyTask> missedTasks) {
        StudyPlan plan = studyPlanDAO.findById(planId);
        if (plan == null) return false;

        LocalDate today = LocalDate.now();
        long daysRemaining = ChronoUnit.DAYS.between(today, plan.getDeadline()) + 1;
        if (daysRemaining <= 0) return false;

        // Count available slots
        int availableSlots = 0;
        for (int i = 0; i < daysRemaining; i++) {
            LocalDate date = today.plusDays(i);
            List<StudyTask> dayTasks = studyTaskDAO.findTasksByDate(planId, date);

            int pendingCount = 0;
            for (StudyTask task : dayTasks) {
                if (!"COMPLETED".equals(task.getStatus())) {
                    pendingCount++;
                }
            }

            availableSlots += Math.max(0, plan.getDailyHours() - pendingCount);
        }

        return availableSlots >= missedTasks.size();
    }
}