package service;

import dao.GitHubActivityDAO;
import dao.StudyPlanDAO;
import dao.StudyTaskDAO;
import model.GitHubActivity;
import model.StudyPlan;
import model.StudyTask;
import model.User;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalysisService {

    private final StudyPlanDAO studyPlanDAO = new StudyPlanDAO();
    private final StudyTaskDAO studyTaskDAO = new StudyTaskDAO();
    private final GitHubActivityDAO gitHubActivityDAO = new GitHubActivityDAO();

    public Map<String, Object> getDashboardStats(User user) {
        Map<String, Object> stats = new HashMap<>();

        List<StudyPlan> plans = studyPlanDAO.findByUserId(user.getId());
        List<GitHubActivity> activities = gitHubActivityDAO.findByUserId(user.getId());
        List<StudyTask> todayTasks = studyTaskDAO.findTodayTasks(user.getId());

        // Calculate overall progress
        int totalCompletion = 0;
        for (StudyPlan plan : plans) {
            totalCompletion += plan.getCompletionPercentage();
        }
        stats.put("overallProgress", plans.isEmpty() ? 0 : totalCompletion / plans.size());

        // Calculate commit streak
        int maxStreak = 0;
        for (GitHubActivity activity : activities) {
            maxStreak = Math.max(maxStreak, activity.getStreakCount());
        }
        stats.put("commitStreak", maxStreak);

        // Count today's tasks
        long pendingToday = todayTasks.stream()
                .filter(t -> t.getStatus().equals("PENDING"))
                .count();
        stats.put("todayTasksCount", todayTasks.size());
        stats.put("pendingTodayTasks", (int) pendingToday);

        // Get nearest deadline
        LocalDate nearestDeadline = null;
        for (StudyPlan plan : plans) {
            if (nearestDeadline == null || plan.getDeadline().isBefore(nearestDeadline)) {
                nearestDeadline = plan.getDeadline();
            }
        }
        stats.put("nearestDeadline", nearestDeadline);

        if (nearestDeadline != null) {
            long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), nearestDeadline);
            stats.put("daysRemaining", Math.max(0, daysRemaining));
        }

        return stats;
    }

    public double calculateCommitConsistency(User user, String repoName) {
        GitHubActivity activity = gitHubActivityDAO.findByUserAndRepo(user.getId(), repoName);
        if (activity == null || activity.getCommitCount() == 0) return 0;

        StudyPlan plan = studyPlanDAO.findByUserId(user.getId())
                .stream()
                .filter(p -> repoName.equals(p.getRepositoryName()))
                .findFirst()
                .orElse(null);

        if (plan == null) return 0;

        long totalDays = ChronoUnit.DAYS.between(plan.getDeadline().minusDays(30), plan.getDeadline());
        if (totalDays <= 0) return 100;

        return Math.min(100, (activity.getCommitCount() * 100.0 / totalDays));
    }

    public String estimateCompletion(User user, StudyPlan plan) {
        GitHubActivity activity = gitHubActivityDAO.findByUserAndRepo(user.getId(), plan.getRepositoryName());

        if (activity == null || activity.getCommitCount() == 0) {
            return "Not Started";
        }

        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), plan.getDeadline());
        if (daysRemaining <= 0) return "Overdue";

        int commitsPerDay = activity.getCommitCount() /
                Math.max(1, (int) ChronoUnit.DAYS.between(plan.getDeadline().minusDays(30), LocalDate.now()));

        if (commitsPerDay >= plan.getDailyHours() / 2) {
            return "On Track";
        } else if (commitsPerDay >= plan.getDailyHours() / 4) {
            return "Behind Schedule";
        } else {
            return "At Risk";
        }
    }
}