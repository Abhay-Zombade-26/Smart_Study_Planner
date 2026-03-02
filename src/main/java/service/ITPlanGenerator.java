package service;

import model.*;
import dao.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ITPlanGenerator implements PlanStrategyService {
    
    private StudyTaskDAO studyTaskDAO;
    private GoalDAO goalDAO;
    
    public ITPlanGenerator() {
        this.studyTaskDAO = new StudyTaskDAO();
        this.goalDAO = new GoalDAO();
    }
    
    @Override
    public StudyPlan generatePlan(User user, String repoName, LocalDate deadline, int dailyHours, String difficulty) {
        StudyPlan plan = new StudyPlan();
        plan.setUserId(user.getId());
        plan.setRepositoryName(repoName);
        plan.setDeadline(deadline);
        plan.setDailyHours(dailyHours);
        plan.setDifficulty(difficulty);
        plan.setCreatedAt(LocalDate.now());
        plan.setStatus("ACTIVE");
        
        // Calculate total days
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), deadline);
        
        // Create daily tasks using DailyTask
        List<DailyTask> tasks = new ArrayList<>();
        
        for (int i = 0; i < totalDays; i++) {
            LocalDate taskDate = LocalDate.now().plusDays(i);
            DailyTask task = new DailyTask();
            task.setUserId(user.getId());
            task.setRepositoryName(repoName);
            task.setTaskDate(taskDate);
            task.setPlannedHours(dailyHours);
            task.setPlannedCommits(1);
            task.setStatus("PENDING");
            task.setDescription("Work on " + repoName + " - Day " + (i+1));
            tasks.add(task);
        }
        
        // Save all tasks
        studyTaskDAO.saveAll(tasks);
        
        return plan;
    }
    
    @Override
    public void adjustPlan(StudyPlan plan, boolean missed) {
        if (missed) {
            System.out.println("Adjusting plan for missed tasks");
            // Implement plan adjustment logic here
        }
    }
}
