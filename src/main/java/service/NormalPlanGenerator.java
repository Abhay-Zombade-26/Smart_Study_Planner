package service;

import model.*;
import dao.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NormalPlanGenerator implements PlanStrategyService {
    
    private StudyTaskDAO studyTaskDAO;
    
    public NormalPlanGenerator() {
        this.studyTaskDAO = new StudyTaskDAO();
    }
    
    @Override
    public StudyPlan generatePlan(User user, String subject, LocalDate deadline, int dailyHours, String difficulty) {
        StudyPlan plan = new StudyPlan();
        plan.setUserId(user.getId());
        plan.setRepositoryName(subject);
        plan.setDeadline(deadline);
        plan.setDailyHours(dailyHours);
        plan.setDifficulty(difficulty);
        plan.setCreatedAt(LocalDate.now());
        plan.setStatus("ACTIVE");
        
        // Calculate total days
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), deadline);
        
        // Create daily tasks
        List<DailyTask> tasks = new ArrayList<>();
        
        for (int i = 0; i < totalDays; i++) {
            LocalDate taskDate = LocalDate.now().plusDays(i);
            DailyTask task = new DailyTask();
            task.setUserId(user.getId());
            task.setRepositoryName(subject);
            task.setTaskDate(taskDate);
            task.setPlannedHours(dailyHours);
            task.setPlannedCommits(0);
            task.setStatus("PENDING");
            task.setDescription("Study " + subject + " - Day " + (i+1));
            tasks.add(task);
        }
        
        // Save all tasks
        studyTaskDAO.saveAll(tasks);
        
        return plan;
    }
    
    @Override
    public void adjustPlan(StudyPlan plan, boolean missed) {
        // Normal students don't need plan adjustment
        System.out.println("No adjustment needed for normal plan");
    }
}
