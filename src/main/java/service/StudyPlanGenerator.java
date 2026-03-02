package service;

import model.User;
import model.Goal;
import model.DailyTask;
import dao.GoalDAO;
import dao.StudyTaskDAO;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudyPlanGenerator {
    
    private StudyTaskDAO taskDAO;
    private GoalDAO goalDAO;
    
    public StudyPlanGenerator() {
        this.taskDAO = new StudyTaskDAO();
        this.goalDAO = new GoalDAO();
    }
    
    public void generatePlan(User user, List<String> selectedRepos, int durationMonths, int dailyHours) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(durationMonths);
        
        int totalDays = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        int reposCount = selectedRepos.size();
        
        // First, create a goal for each selected repository
        for (String repoName : selectedRepos) {
            Goal goal = new Goal(user.getId(), repoName, durationMonths, dailyHours);
            goalDAO.save(goal);
            System.out.println("? Goal created for: " + repoName + " (ID: " + goal.getId() + ")");
        }
        
        // Then create daily tasks rotating through repositories
        for (int day = 0; day < totalDays; day++) {
            LocalDate currentDate = startDate.plusDays(day);
            String repoForToday = selectedRepos.get(day % reposCount);
            
            DailyTask task = new DailyTask();
            task.setUserId(user.getId());
            task.setRepositoryName(repoForToday);
            task.setTaskDate(currentDate);
            task.setPlannedHours(dailyHours);
            task.setPlannedCommits(1);
            task.setStatus("PENDING");
            task.setDescription("Work on " + repoForToday);
            
            taskDAO.save(task);
            
            System.out.println("?? Day " + (day+1) + ": " + repoForToday);
        }
        
        System.out.println("? Study plan generated for " + selectedRepos.size() + " repositories over " + durationMonths + " months");
    }
}
