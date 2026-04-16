package service;

import model.StudyTask;
import dao.StudyTaskDAO;
import java.util.List;

public class AnalysisService {

    private StudyTaskDAO studyTaskDAO;

    public AnalysisService() {
        this.studyTaskDAO = new StudyTaskDAO();
    }

    public double getCompletionRate(int userId) {
        List<StudyTask> tasks = studyTaskDAO.findAllTasksByUser(userId);
        if (tasks.isEmpty()) return 0;

        long completed = tasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
        return (completed * 100.0) / tasks.size();
    }

    public int getStreak(int userId) {
        List<StudyTask> tasks = studyTaskDAO.findAllTasksByUser(userId);
        int streak = 0;

        for (StudyTask task : tasks) {
            if ("COMPLETED".equals(task.getStatus())) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }
}