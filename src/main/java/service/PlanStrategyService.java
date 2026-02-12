package service;

import model.StudyPlan;
import model.StudyTask;
import model.User;

import java.time.LocalDate;
import java.util.List;

public interface PlanStrategyService {
    StudyPlan generatePlan(User user, String target, LocalDate deadline,
                           int dailyHours, String difficulty);
    List<StudyTask> generateTasks(StudyPlan plan);
    void adjustPlan(StudyPlan plan, boolean missedCommit);
}