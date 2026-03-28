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
        long daysRemaining = ChronoUnit.DAYS.between(today, plan.getDeadline());

        if (daysRemaining <= 0) {
            System.out.println("Deadline passed!");
            return tasks;
        }

        int totalHours = (int) (daysRemaining * plan.getDailyHours());
        System.out.println("Total hours: " + totalHours);

        Map<Integer, Double> hoursPerTopic = weightCalculator.distributeHours(topics, totalHours);

        for (Topic topic : topics) {
            double topicHours = hoursPerTopic.get(topic.getId());
            int sessionCount = (int) Math.ceil(topicHours);
            if (sessionCount <= 0) sessionCount = 1;

            System.out.println("Topic: " + topic.getName() + " - Sessions: " + sessionCount);

            long interval = daysRemaining / sessionCount;
            if (interval < 1) interval = 1;

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
                        false,
                        topic.getId(),
                        sessionType
                );
                tasks.add(task);
                sessionIndex++;
            }
        }

        System.out.println("Generated " + tasks.size() + " tasks from topics.");

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