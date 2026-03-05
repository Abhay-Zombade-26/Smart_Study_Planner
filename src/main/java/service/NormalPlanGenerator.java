package service;

import dao.StudyPlanDAO;
import dao.StudyTaskDAO;
import model.StudyPlan;
import model.StudyTask;
import model.User;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class NormalPlanGenerator implements PlanStrategyService {

    private final StudyPlanDAO studyPlanDAO = new StudyPlanDAO();
    private final StudyTaskDAO studyTaskDAO = new StudyTaskDAO();

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

            List<StudyTask> tasks = generateTasks(plan);
            System.out.println("Generated " + tasks.size() + " tasks");

            studyTaskDAO.saveAll(tasks);
            System.out.println("Tasks saved to database");

            plan.setTasks(tasks);
            return plan;

        } catch (Exception e) {
            System.err.println("Exception in generatePlan: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<StudyTask> generateTasks(StudyPlan plan) {
        List<StudyTask> tasks = new ArrayList<>();
        LocalDate today = LocalDate.now();
        long daysRemaining = ChronoUnit.DAYS.between(today, plan.getDeadline());

        System.out.println("Days remaining: " + daysRemaining);

        if (daysRemaining <= 0) {
            System.out.println("Deadline is in the past!");
            return tasks;
        }

        String[] subjects = plan.getRepositoryName().split(",");
        int subjectCount = subjects.length;
        System.out.println("Subjects array: " + String.join(", ", subjects));

        for (int i = 0; i < daysRemaining; i++) {
            LocalDate taskDate = today.plusDays(i);
            String subject = subjects[i % subjectCount].trim();

            StudyTask task = new StudyTask(
                    plan.getId(),
                    taskDate,
                    "Study " + subject,
                    false
            );
            tasks.add(task);
        }

        return tasks;
    }

    @Override
    public void adjustPlan(StudyPlan plan, boolean missedDay) {
        // No auto-adjust for normal students
    }
}
