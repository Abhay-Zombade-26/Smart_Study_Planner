package service;

import model.StudyPlan;
import model.User;
import java.time.LocalDate;

public interface PlanStrategyService {
    StudyPlan generatePlan(User user, String target, LocalDate deadline, int dailyHours, String difficulty);
    
    // Default implementation for adjustPlan to make it optional
    default void adjustPlan(StudyPlan plan, boolean missed) {
        // Default implementation does nothing
        System.out.println("Plan adjustment not implemented");
    }
}
