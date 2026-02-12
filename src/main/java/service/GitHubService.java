package service;

import model.GitHubActivity;
import model.User;
import org.json.JSONArray;
import org.json.JSONObject;
import util.HttpUtil;
import util.JsonUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class GitHubService {

    private final String accessToken;

    public GitHubService(String accessToken) {
        this.accessToken = accessToken;
    }

    public List<Map<String, String>> getRepositories() throws IOException {
        List<Map<String, String>> repos = new ArrayList<>();

        Map<String, String> headers = getHeaders();
        String response = HttpUtil.sendGet("https://api.github.com/user/repos?sort=updated&per_page=100", headers);

        JSONArray reposArray = JsonUtil.parseArray(response);

        for (int i = 0; i < reposArray.length(); i++) {
            JSONObject repo = reposArray.getJSONObject(i);
            Map<String, String> repoInfo = new HashMap<>();
            repoInfo.put("name", repo.getString("full_name"));
            repoInfo.put("private", String.valueOf(repo.getBoolean("private")));
            repoInfo.put("updated_at", repo.getString("updated_at"));
            repoInfo.put("html_url", repo.getString("html_url"));
            repos.add(repoInfo);
        }

        return repos;
    }

    public Map<String, Object> getCommitActivity(String owner, String repo) throws IOException {
        Map<String, String> headers = getHeaders();

        try {
            String response = HttpUtil.sendGet(
                    String.format("https://api.github.com/repos/%s/%s/commits?per_page=100", owner, repo),
                    headers
            );

            JSONArray commits = JsonUtil.parseArray(response);

            int totalCommits = commits.length();
            LocalDate lastCommitDate = null;
            int commitsLastWeek = 0;
            LocalDate weekAgo = LocalDate.now().minusDays(7);

            if (totalCommits > 0) {
                JSONObject lastCommit = commits.getJSONObject(0);
                String dateStr = lastCommit.getJSONObject("commit")
                        .getJSONObject("committer")
                        .getString("date");
                lastCommitDate = LocalDate.parse(dateStr.substring(0, 10));

                for (int i = 0; i < commits.length(); i++) {
                    JSONObject commit = commits.getJSONObject(i);
                    String commitDateStr = commit.getJSONObject("commit")
                            .getJSONObject("committer")
                            .getString("date");
                    LocalDate commitDate = LocalDate.parse(commitDateStr.substring(0, 10));

                    if (commitDate.isAfter(weekAgo) || commitDate.equals(weekAgo)) {
                        commitsLastWeek++;
                    }
                }
            }

            Map<String, Object> activity = new HashMap<>();
            activity.put("total_commits", totalCommits);
            activity.put("last_commit_date", lastCommitDate);
            activity.put("commits_last_week", commitsLastWeek);
            activity.put("active_days", calculateActiveDays(commits));
            activity.put("commit_gaps", calculateCommitGaps(commits));

            return activity;

        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public GitHubActivity fetchAndUpdateActivity(User user, String repoName) throws IOException {
        String[] parts = repoName.split("/");
        String owner = parts[0];
        String repo = parts[1];

        Map<String, Object> activity = getCommitActivity(owner, repo);

        GitHubActivity gitHubActivity = new GitHubActivity(user.getId(), repoName);
        gitHubActivity.setCommitCount((int) activity.get("total_commits"));
        gitHubActivity.setLastCommitDate((LocalDate) activity.get("last_commit_date"));
        gitHubActivity.setStreakCount(calculateStreak(activity));

        return gitHubActivity;
    }

    private int calculateStreak(Map<String, Object> activity) {
        LocalDate lastCommit = (LocalDate) activity.get("last_commit_date");
        if (lastCommit == null) return 0;

        LocalDate today = LocalDate.now();
        if (lastCommit.equals(today)) return 1;
        if (lastCommit.equals(today.minusDays(1))) return 2;
        return 0;
    }

    private int calculateActiveDays(JSONArray commits) {
        Set<String> dates = new HashSet<>();
        for (int i = 0; i < commits.length(); i++) {
            JSONObject commit = commits.getJSONObject(i);
            String dateStr = commit.getJSONObject("commit")
                    .getJSONObject("committer")
                    .getString("date");
            dates.add(dateStr.substring(0, 10));
        }
        return dates.size();
    }

    private int calculateCommitGaps(JSONArray commits) {
        if (commits.length() < 2) return 0;

        int gaps = 0;
        LocalDate prevDate = null;

        for (int i = 0; i < commits.length(); i++) {
            JSONObject commit = commits.getJSONObject(i);
            String dateStr = commit.getJSONObject("commit")
                    .getJSONObject("committer")
                    .getString("date");
            LocalDate currentDate = LocalDate.parse(dateStr.substring(0, 10));

            if (prevDate != null) {
                long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(currentDate, prevDate);
                if (daysBetween > 2) {
                    gaps++;
                }
            }
            prevDate = currentDate;
        }

        return gaps;
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        headers.put("Accept", "application/vnd.github.v3+json");
        return headers;
    }
}