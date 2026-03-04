package service;

import model.User;
import model.DailyTask;
import dao.StudyTaskDAO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class GitHubCommitChecker {
    
    private GitHubOAuthService gitHubService;
    private StudyTaskDAO taskDAO;
    
    public GitHubCommitChecker() {
        this.gitHubService = new GitHubOAuthService();
        this.taskDAO = new StudyTaskDAO();
    }
    
    public void checkAndUpdateAllTasks(User user) {
        List<DailyTask> tasks = taskDAO.findByUserId(user.getId());
        
        System.out.println("?? Checking GitHub commits for " + tasks.size() + " tasks...");
        
        int completed = 0;
        int missed = 0;
        int pending = 0;
        
        for (DailyTask task : tasks) {
            // Skip future tasks
            if (task.getTaskDate().isAfter(LocalDate.now())) {
                pending++;
                continue;
            }
            
            // Get actual commits from GitHub
            int actualCommits = getCommitsForDate(user, task.getRepositoryName(), task.getTaskDate());
            
            // Update task with actual commits
            task.setActualCommits(actualCommits);
            
            // Determine status based on actual commits
            String oldStatus = task.getStatus();
            String newStatus;
            
            if (actualCommits >= task.getPlannedCommits()) {
                newStatus = "COMPLETED";
                completed++;
                System.out.println("? Task COMPLETED: " + task.getRepositoryName() + " on " + task.getTaskDate() + " - Commits: " + actualCommits);
            } else if (task.getTaskDate().isBefore(LocalDate.now())) {
                newStatus = "MISSED";
                missed++;
                System.out.println("? Task MISSED: " + task.getRepositoryName() + " on " + task.getTaskDate() + " - Commits: " + actualCommits + " (needed " + task.getPlannedCommits() + ")");
            } else {
                newStatus = "PENDING";
                pending++;
            }
            
            // Only update if status changed
            if (!newStatus.equals(oldStatus)) {
                task.setStatus(newStatus);
                taskDAO.update(task);
                System.out.println("   Status updated from " + oldStatus + " to " + newStatus);
            }
        }
        
        System.out.println("?? Summary: " + completed + " completed, " + missed + " missed, " + pending + " pending");
        System.out.println("? GitHub commit check completed");
    }
    
    private int getCommitsForDate(User user, String repoFullName, LocalDate date) {
        try {
            String accessToken = user.getAccessToken();
            
            // Handle repository name format
            String[] parts = repoFullName.split("/");
            if (parts.length < 2) {
                System.err.println("? Invalid repository format: " + repoFullName);
                return 0;
            }
            
            String owner = parts[0];
            String repo = parts[1];
            
            // Format date for GitHub API (YYYY-MM-DD)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String dateStr = date.format(formatter);
            
            // GitHub API: get commits for specific date
            String urlStr = String.format("https://api.github.com/repos/%s/%s/commits?since=%sT00:00:00Z&until=%sT23:59:59Z&per_page=100",
                owner, repo, dateStr, dateStr);
            
            System.out.println("?? Checking: " + owner + "/" + repo + " on " + dateStr);
            
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "token " + accessToken);
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                if (responseCode == 409) {
                    // Empty repository
                    System.out.println("?? Repository is empty: " + owner + "/" + repo);
                    return 0;
                }
                System.err.println("? GitHub API error: " + responseCode + " for " + owner + "/" + repo);
                return 0;
            }
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            JSONArray commits = new JSONArray(response.toString());
            System.out.println("   Found " + commits.length() + " commits");
            return commits.length();
            
        } catch (Exception e) {
            System.err.println("? Error checking commits for " + repoFullName + " on " + date + ": " + e.getMessage());
            return 0;
        }
    }
}
