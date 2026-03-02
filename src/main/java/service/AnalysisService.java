package service;

import model.*;
import dao.*;
import java.util.List;

public class AnalysisService {
    
    private StudyTaskDAO studyTaskDAO;
    
    public AnalysisService() {
        this.studyTaskDAO = new StudyTaskDAO();
    }
    
    public double getCompletionRate(int userId) {
        List<DailyTask> tasks = studyTaskDAO.findByUserId(userId);
        if (tasks.isEmpty()) return 0;
        
        long completed = tasks.stream().filter(DailyTask::isCompleted).count();
        return (completed * 100.0) / tasks.size();
    }
    
    public int getStreak(int userId) {
        List<DailyTask> tasks = studyTaskDAO.findByUserId(userId);
        int streak = 0;
        
        for (DailyTask task : tasks) {
            if (task.isCompleted()) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }
}
