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

    /**
     * Generates study tasks based on the topics associated with the plan.
     * Called from PlanManagementFrame after topics are added.
     */
    public List<StudyTask> generateTasksFromTopics(StudyPlan plan) {
        List<StudyTask> tasks = new ArrayList<>();

        // Retrieve topics for this plan
        List<Topic> topics = topicDAO.findByPlanId(plan.getId());
        if (topics.isEmpty()) {
            System.out.println("No topics found for plan " + plan.getId() + ". Cannot generate tasks.");
            return tasks;
        }

        // Calculate weights for each topic
        weightCalculator.calculateWeights(topics);

        // Calculate total available study hours until deadline
        LocalDate today = LocalDate.now();
        long daysRemaining = ChronoUnit.DAYS.between(today, plan.getDeadline());
        if (daysRemaining <= 0) {
            System.out.println("Deadline is in the past or today!");
            return tasks;
        }
        int totalHours = (int) (daysRemaining * plan.getDailyHours());

        // Distribute hours proportionally to topic weights
        Map<Integer, Double> hoursPerTopic = weightCalculator.distributeHours(topics, totalHours);

        // For each topic, generate tasks spread across the available days
        for (Topic topic : topics) {
            double topicHours = hoursPerTopic.get(topic.getId());
            // Convert hours to number of study sessions (assuming each session is 1 hour)
            int sessionCount = (int) Math.ceil(topicHours);
            // Distribute sessions evenly across the days remaining
            long interval = daysRemaining / sessionCount;
            if (interval < 1) interval = 1;

            // Simple session type distribution: 30% Learn, 40% Practice, 30% Review
            int learnCount = (int) Math.ceil(sessionCount * 0.3);
            int practiceCount = (int) Math.ceil(sessionCount * 0.4);
            int reviewCount = sessionCount - learnCount - practiceCount;

            int sessionIndex = 0;
            for (int dayOffset = 0; dayOffset < daysRemaining && sessionIndex < sessionCount; dayOffset += interval) {
                if (dayOffset >= daysRemaining) break;
                LocalDate taskDate = today.plusDays(dayOffset);
                String sessionType;
                if (sessionIndex < learnCount) {
                    sessionType = "LEARN";
                } else if (sessionIndex < learnCount + practiceCount) {
                    sessionType = "PRACTICE";
                } else {
                    sessionType = "REVIEW";
                }

                String description = String.format("%s: %s", topic.getName(), sessionType.toLowerCase());
                StudyTask task = new StudyTask(
                        plan.getId(),
                        taskDate,
                        description,
                        false,           // requiredCommit false for normal mode
                        topic.getId(),
                        sessionType
                );
                tasks.add(task);
                sessionIndex++;
            }
        }

        System.out.println("Generated " + tasks.size() + " tasks from topics.");
        return tasks;
    }

    // Legacy method – not used in new workflow, kept for compatibility
    @Override
    public List<StudyTask> generateTasks(StudyPlan plan) {
        // This is the old simple rotation method; we return empty list as we use generateTasksFromTopics now.
        return new ArrayList<>();
    }

    @Override
    public void adjustPlan(StudyPlan plan, boolean missedDay) {
        // No auto-adjust for normal students
    }
}