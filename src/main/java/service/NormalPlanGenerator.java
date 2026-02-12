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
    public StudyPlan generatePlan(User user, String subject, LocalDate deadline,
                                  int dailyHours, String difficulty) {

        StudyPlan plan = new StudyPlan(user.getId(), subject, deadline, difficulty, dailyHours);
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

        String[] subjects = plan.getRepositoryName().split(",");
        int subjectIndex = 0;

        for (int i = 0; i < daysRemaining; i++) {
            LocalDate taskDate = today.plusDays(i);
            String subject = subjects[subjectIndex % subjects.length].trim();

            String taskDescription = String.format(
                    "Study %s for %d hours - Chapter %d",
                    subject,
                    plan.getDailyHours(),
                    (i / subjects.length) + 1
            );

            StudyTask task = new StudyTask(
                    plan.getId(),
                    taskDate,
                    taskDescription,
                    false
            );

            tasks.add(task);
            subjectIndex++;
        }

        return tasks;
    }

    @Override
    public void adjustPlan(StudyPlan plan, boolean missedDay) {
        // Normal students don't get automatic adjustment
        // This remains untouched as per requirements
    }
}