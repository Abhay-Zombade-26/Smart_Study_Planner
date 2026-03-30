package service;

import dao.StudyPlanDAO;
import dao.StudyTaskDAO;
import dao.TopicDAO;
import model.StudyPlan;
import model.StudyTask;
import model.Topic;
import model.User;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class NormalPlanGenerator implements PlanStrategyService {

    private final StudyPlanDAO studyPlanDAO = new StudyPlanDAO();
    private final StudyTaskDAO studyTaskDAO = new StudyTaskDAO();
    private final TopicDAO topicDAO = new TopicDAO();
    private final TopicWeightCalculator weightCalculator = new TopicWeightCalculator();

    @Override
    public StudyPlan generatePlan(User user, String subjects, LocalDate deadline,
                                  int dailyHours, String difficulty) {
        System.out.println("NormalPlanGenerator.generatePlan() called");
        System.out.println("User: " + user.getEmail());
        System.out.println("Subjects: " + subjects);
        System.out.println("Deadline: " + deadline);
        System.out.println("Daily hours: " + dailyHours);
        System.out.println("Difficulty: " + difficulty);

        try {
            StudyPlan plan = new StudyPlan(user.getId(), subjects, deadline, difficulty, dailyHours);
            plan.setRole("NORMAL");
            plan.setLoginType("GOOGLE");
            plan = studyPlanDAO.save(plan);
            System.out.println("Plan saved to DB with ID: " + (plan != null ? plan.getId() : "null"));

            if (plan == null) {
                System.err.println("Failed to save plan to database!");
                return null;
            }

            return plan;

        } catch (Exception e) {
            System.err.println("Exception in generatePlan: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<StudyTask> generateTasksFromTopics(StudyPlan plan) {
        List<StudyTask> tasks = new ArrayList<>();

        List<Topic> topics = topicDAO.findByPlanId(plan.getId());
        if (topics.isEmpty()) {
            System.out.println("No topics found for plan " + plan.getId());
            return tasks;
        }

        System.out.println("Found " + topics.size() + " topics for plan " + plan.getId());

        weightCalculator.calculateWeights(topics);

        LocalDate today = LocalDate.now();
        // Include the deadline day itself
        long daysRemainingLong = ChronoUnit.DAYS.between(today, plan.getDeadline()) + 1;
        int daysRemaining = (int) daysRemainingLong; // Convert to int safely

        System.out.println("Today: " + today);
        System.out.println("Deadline: " + plan.getDeadline());
        System.out.println("Days remaining (including deadline): " + daysRemaining);

        if (daysRemaining <= 0) {
            System.out.println("Deadline passed or is today!");
            return tasks;
        }

        int totalHours = (int) (daysRemaining * plan.getDailyHours());
        System.out.println("Total hours: " + totalHours);

        Map<Integer, Double> hoursPerTopic = weightCalculator.distributeHours(topics, totalHours);

        // For each topic, generate tasks spread across the available days
        for (Topic topic : topics) {
            double topicHours = hoursPerTopic.get(topic.getId());
            int sessionCount = (int) Math.ceil(topicHours);
            if (sessionCount <= 0) sessionCount = 1;

            System.out.println("Topic: " + topic.getName() + " - Hours: " + topicHours + " - Sessions: " + sessionCount);

            // Calculate session types distribution
            int learnCount = (int) Math.ceil(sessionCount * 0.3);
            int practiceCount = (int) Math.ceil(sessionCount * 0.4);
            int reviewCount = sessionCount - learnCount - practiceCount;

            // Create a list of all available dates
            List<LocalDate> availableDates = new ArrayList<>();
            for (int i = 0; i < daysRemaining; i++) {
                availableDates.add(today.plusDays(i));
            }

            // Distribute sessions evenly across available dates
            // This ensures tasks are spread across the entire period, not bunched up
            for (int i = 0; i < sessionCount; i++) {
                // Calculate which date index this session should go to
                int dateIndex = (int) ((long) i * daysRemaining / sessionCount);
                if (dateIndex >= daysRemaining) dateIndex = daysRemaining - 1;

                LocalDate taskDate = availableDates.get(dateIndex);

                // Determine session type
                String sessionType;
                if (i < learnCount) {
                    sessionType = "LEARN";
                } else if (i < learnCount + practiceCount) {
                    sessionType = "PRACTICE";
                } else {
                    sessionType = "REVIEW";
                }

                String description = String.format("%s: %s", topic.getName(), sessionType.toLowerCase());

                StudyTask task = new StudyTask(
                        plan.getId(),
                        taskDate,
                        description,
                        false,
                        topic.getId(),
                        sessionType
                );
                tasks.add(task);
            }
        }

        System.out.println("Generated " + tasks.size() + " tasks from topics.");

        // Print distribution by date for verification
        Map<LocalDate, Integer> dateDistribution = new HashMap<>();
        for (StudyTask task : tasks) {
            dateDistribution.put(task.getTaskDate(), dateDistribution.getOrDefault(task.getTaskDate(), 0) + 1);
        }
        System.out.println("Task distribution by date:");
        for (Map.Entry<LocalDate, Integer> entry : dateDistribution.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue() + " tasks");
        }

        // Save all tasks to database
        if (!tasks.isEmpty()) {
            studyTaskDAO.saveAll(tasks);
            System.out.println("✅ Saved " + tasks.size() + " tasks to database");
        }

        return tasks;
    }

    @Override
    public List<StudyTask> generateTasks(StudyPlan plan) {
        return new ArrayList<>();
    }

    @Override
    public void adjustPlan(StudyPlan plan, boolean missedDay) {
        // No auto-adjust for normal students
    }
}