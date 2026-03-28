package service;

import model.*;
import dao.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ITPlanGenerator implements PlanStrategyService {

    private StudyTaskDAO studyTaskDAO;

    public ITPlanGenerator() {
        this.studyTaskDAO = new StudyTaskDAO();
    }

    @Override
    public StudyPlan generatePlan(User user, String target, LocalDate deadline, int dailyHours, String difficulty) {
        // Use constructor with repository name
        StudyPlan plan = new StudyPlan(user.getId(), target, deadline, difficulty, dailyHours);
        return plan;
    }

    // New method with plan name
    public StudyPlan generatePlanWithName(User user, String planName, String repositoryName,
                                          LocalDate deadline, int dailyHours, String difficulty) {
        StudyPlan plan = new StudyPlan(user.getId(), planName, repositoryName, deadline, difficulty, dailyHours);
        return plan;
    }

    @Override
    public List<StudyTask> generateTasks(StudyPlan plan) {
        List<StudyTask> tasks = new ArrayList<>();

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = plan.getDeadline();
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);

        for (int i = 0; i < totalDays; i++) {
            LocalDate taskDate = startDate.plusDays(i);
            StudyTask task = new StudyTask();
            task.setGoalId(plan.getId());
            task.setTaskDate(taskDate);
            task.setDescription("Work on " + plan.getRepositoryName() + " - Day " + (i+1));
            task.setRequiredCommit(true);
            task.setStatus("PENDING");
            task.setTopicId(0);
            task.setSessionType("CODING");
            tasks.add(task);
        }

        studyTaskDAO.saveAll(tasks);
        return tasks;
    }

    @Override
    public void adjustPlan(StudyPlan plan, boolean missedCommit) {
        if (missedCommit) {
            System.out.println("Adjusting plan for missed commits");
        }
    }
}