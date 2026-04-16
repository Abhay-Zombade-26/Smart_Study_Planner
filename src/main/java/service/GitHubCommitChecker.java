package service;

import model.User;
import model.StudyTask;
import model.StudyPlan;
import dao.StudyTaskDAO;
import dao.StudyPlanDAO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class GitHubCommitChecker {

    private StudyTaskDAO taskDAO;
    private StudyPlanDAO planDAO;

    public GitHubCommitChecker() {
        this.taskDAO = new StudyTaskDAO();
        this.planDAO = new StudyPlanDAO();
    }

    public void checkAndUpdateAllTasks(User user) {
        if (!"GITHUB".equals(user.getOauthProvider())) {
            return;
        }

        Integer activePlanId = user.getActivePlanId();
        if (activePlanId == null) {
            System.out.println("📋 No active plan selected. Skipping commit check.");
            return;
        }

        StudyPlan activePlan = planDAO.findById(activePlanId);
        if (activePlan == null || activePlan.getRepositoryName() == null) {
            System.out.println("📋 No valid active plan found.");
            return;
        }

        List<StudyTask> tasks = taskDAO.findByGoalId(activePlanId);

        if (tasks.isEmpty()) {
            System.out.println("📋 No tasks found for active plan.");
            return;
        }

        System.out.println("\n🔍 Checking GitHub commits for active plan: " + activePlan.getPlanName());
        System.out.println("   Repository: " + activePlan.getRepositoryName());
        System.out.println("   Total tasks: " + tasks.size());

        int completed = 0;
        int missed = 0;
        int pending = 0;

        for (StudyTask task : tasks) {
            // Skip future tasks
            if (task.getTaskDate().isAfter(LocalDate.now())) {
                pending++;
                continue;
            }

            // Get repository name for this task
            String repoName = task.getRepositoryName();
            if (repoName == null || repoName.isEmpty()) {
                repoName = activePlan.getRepositoryName();
            }

            // Check if commit exists for this task
            int actualCommits = getCommitsForDate(user, repoName, task.getTaskDate());

            // CRITICAL: Set planned_commits to 1 if it's 0 (for display)
            if (task.getPlannedCommits() == 0) {
                task.setPlannedCommits(1);
                taskDAO.updatePlannedCommits(task.getId(), 1);
            }

            // Update task with actual commits
            task.setActualCommits(actualCommits);
            taskDAO.updateCommitCount(task.getId(), actualCommits);

            String oldStatus = task.getStatus();
            String newStatus;

            if (actualCommits > 0) {
                newStatus = "COMPLETED";
                completed++;
                System.out.println("✅ Task COMPLETED: " + task.getDescription() + " on " + task.getTaskDate() + " - Commits: " + actualCommits + "/1");
            } else if (task.getTaskDate().isBefore(LocalDate.now())) {
                newStatus = "MISSED";
                missed++;
                System.out.println("❌ Task MISSED: " + task.getDescription() + " on " + task.getTaskDate() + " - Commits: 0/1");
            } else {
                newStatus = "PENDING";
                pending++;
                System.out.println("⏳ Task PENDING: " + task.getDescription() + " on " + task.getTaskDate() + " - Commits: 0/1");
            }

            if (!newStatus.equals(oldStatus)) {
                task.setStatus(newStatus);
                taskDAO.updateStatus(task.getId(), newStatus);
            }
        }

        System.out.println("📊 Summary: " + completed + " completed, " + missed + " missed, " + pending + " pending");
        System.out.println("✅ GitHub commit check completed\n");
    }

    private int getCommitsForDate(User user, String repoFullName, LocalDate date) {
        try {
            String accessToken = user.getAccessToken();
            if (accessToken == null || accessToken.isEmpty()) {
                return 0;
            }

            String[] parts = repoFullName.split("/");
            if (parts.length < 2) return 0;

            String owner = parts[0];
            String repo = parts[1];
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            String urlStr = String.format("https://api.github.com/repos/%s/%s/commits?since=%sT00:00:00Z&until=%sT23:59:59Z&per_page=100",
                    owner, repo, dateStr, dateStr);

            System.out.println("   Checking commits for " + owner + "/" + repo + " on " + dateStr);

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "token " + accessToken);
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.out.println("   API response code: " + responseCode);
                return 0;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONArray commits = new JSONArray(response.toString());
            int commitCount = commits.length();
            System.out.println("   Found " + commitCount + " commits on " + dateStr);
            return commitCount;

        } catch (Exception e) {
            System.err.println("Error checking commit: " + e.getMessage());
            return 0;
        }
    }

    public void manualCheck(User user) {
        System.out.println("🔄 Manual commit check triggered...");
        checkAndUpdateAllTasks(user);
    }
}