package service;

import config.AppConfig;
import model.User;
import model.StudyPlan;
import model.StudyTask;
import dao.StudyPlanDAO;
import dao.StudyTaskDAO;
import dao.UserDAO;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AIPlanService {

    private static final String AI_API_KEY;
    private static final String AI_API_URL;
    private static final String AI_MODEL;
    private static final int MAX_TOKENS;

    private StudyPlanDAO planDAO;
    private StudyTaskDAO taskDAO;
    private UserDAO userDAO;

    static {
        AI_API_KEY = AppConfig.AI_API_KEY;
        AI_API_URL = AppConfig.AI_API_URL;
        AI_MODEL = AppConfig.AI_MODEL;
        MAX_TOKENS = AppConfig.AI_MAX_TOKENS;
    }

    public AIPlanService() {
        this.planDAO = new StudyPlanDAO();
        this.taskDAO = new StudyTaskDAO();
        this.userDAO = new UserDAO();
    }

    public static boolean isAIAvailable() {
        return AI_API_KEY != null && !AI_API_KEY.isEmpty() && !AI_API_KEY.equals("YOUR_OPENAI_API_KEY_HERE") && !AI_API_KEY.equals("YOUR_API_KEY_HERE");
    }

    public StudyPlan generatePlanFromIdea(User user, RepositoryAnalysis analysis, LocalDate deadline,
                                          int dailyHours, String userPrompt) {
        try {
            System.out.println("\n🤖 Generating AI-powered study plan from user idea");
            System.out.println("   Idea: " + (userPrompt.length() > 100 ? userPrompt.substring(0, 100) + "..." : userPrompt));

            // Calculate days
            long daysUntilDeadline = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), deadline);
            int totalDays = Math.min((int)daysUntilDeadline, 30);
            System.out.println("   Planning for " + totalDays + " days");

            // Build dynamic prompt - let AI figure out everything
            String prompt = buildDynamicPrompt(userPrompt, totalDays, dailyHours);
            System.out.println("   Prompt size: " + prompt.length() + " characters");

            String aiResponse = callAIApi(prompt);
            System.out.println("   Raw response length: " + aiResponse.length());

            // Extract JSON from response
            String jsonStr = extractJSON(aiResponse);
            System.out.println("   Extracted JSON: " + jsonStr.substring(0, Math.min(500, jsonStr.length())));

            JSONObject aiPlan = new JSONObject(jsonStr);

            // Create study plan
            StudyPlan plan = new StudyPlan();
            plan.setUserId(user.getId());
            plan.setPlanName(aiPlan.getString("plan_name"));
            plan.setRepositoryName(analysis.repoName);
            plan.setDeadline(deadline);
            plan.setDailyHours(dailyHours);
            plan.setDifficulty("AI_GENERATED");
            plan.setCompletionPercentage(0);
            plan.setAiGenerated(true);
            plan.setRole("IT");           // Add this
            plan.setLoginType("GITHUB");  // Add this

            plan = planDAO.save(plan);

            if (plan == null) {
                System.err.println("❌ Failed to save AI plan to database");
                return null;
            }

            System.out.println("✅ Plan saved with ID: " + plan.getId());

            // Create tasks from AI response
            JSONArray dailyTasks = aiPlan.getJSONArray("daily_tasks");
            List<StudyTask> tasks = new ArrayList<>();

            for (int i = 0; i < dailyTasks.length(); i++) {
                JSONObject taskObj = dailyTasks.getJSONObject(i);

                StudyTask task = new StudyTask();
                task.setGoalId(plan.getId());
                task.setUserId(user.getId());
                task.setRepositoryName(analysis.repoName);
                task.setTaskDate(LocalDate.now().plusDays(taskObj.getInt("day") - 1));
                task.setDescription(taskObj.getString("task"));
                task.setPlannedHours(dailyHours);
                task.setPlannedCommits(1);
                task.setRequiredCommit(true);
                task.setStatus("PENDING");
                task.setActualHours(0);
                task.setActualCommits(0);

                if (taskObj.has("file") && !taskObj.isNull("file")) {
                    task.setExpectedFiles(taskObj.getString("file"));
                }

                tasks.add(task);
            }

            taskDAO.saveAll(tasks);

            System.out.println("✅ Created " + tasks.size() + " tasks");

            return plan;

        } catch (JSONException e) {
            System.err.println("❌ AI plan generation failed - JSON parsing error: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("❌ AI plan generation failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String buildDynamicPrompt(String userPrompt, int totalDays, int dailyHours) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert software architect and study planner.\n\n");
        prompt.append("The user has the following project idea:\n");
        prompt.append("```\n");
        prompt.append(userPrompt).append("\n");
        prompt.append("```\n\n");

        prompt.append("Based on this idea, create a detailed ").append(totalDays).append("-day study plan.\n");
        prompt.append("The user has ").append(dailyHours).append(" hours available per day.\n\n");

        prompt.append("Your task:\n");
        prompt.append("1. Understand the project idea\n");
        prompt.append("2. Create a meaningful project name\n");
        prompt.append("3. Break down the project into logical, progressive daily tasks\n");
        prompt.append("4. Each task should be actionable and meaningful\n");
        prompt.append("5. Suggest appropriate file names based on the technology stack mentioned\n");
        prompt.append("6. Tasks should follow a logical development workflow\n\n");

        prompt.append("Return ONLY valid JSON in this exact format (no other text):\n");
        prompt.append("{\n");
        prompt.append("  \"plan_name\": \"Creative project name based on the idea\",\n");
        prompt.append("  \"total_days\": ").append(totalDays).append(",\n");
        prompt.append("  \"daily_tasks\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"day\": 1,\n");
        prompt.append("      \"file\": \"appropriate/file/path/name.ext\",\n");
        prompt.append("      \"task\": \"Clear, actionable task description\",\n");
        prompt.append("      \"commit_message\": \"Short commit message\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n\n");
        prompt.append("Generate ").append(totalDays).append(" meaningful daily tasks. ");
        prompt.append("Each task must include:\n");
        prompt.append("- A specific file name relevant to the project\n");
        prompt.append("- A clear, actionable task description\n");
        prompt.append("- A short commit message\n\n");
        prompt.append("The tasks should be progressive and build toward completing the project idea.");

        return prompt.toString();
    }

    private String extractJSON(String response) {
        // Remove markdown code blocks
        response = response.replaceAll("```json\\s*", "");
        response = response.replaceAll("```\\s*", "");
        response = response.trim();

        // Find JSON object
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');

        if (start >= 0 && end > start) {
            String json = response.substring(start, end + 1);

            // Fix truncated JSON by adding missing closing braces
            int openBraces = 0;
            int closeBraces = 0;
            for (char c : json.toCharArray()) {
                if (c == '{') openBraces++;
                if (c == '}') closeBraces++;
            }

            while (closeBraces < openBraces) {
                json += "}";
                closeBraces++;
            }

            int openBrackets = 0;
            int closeBrackets = 0;
            for (char c : json.toCharArray()) {
                if (c == '[') openBrackets++;
                if (c == ']') closeBrackets++;
            }

            while (closeBrackets < openBrackets) {
                json += "]";
                closeBrackets++;
            }

            return json;
        }

        return response;
    }

    public RepositoryAnalysis analyzeRepository(String accessToken, String repoFullName) throws Exception {
        RepositoryAnalysis analysis = new RepositoryAnalysis();
        analysis.repoName = repoFullName;
        analysis.isEmpty = true;
        analysis.allFiles = new ArrayList<>();
        analysis.fileGroups = new HashMap<>();
        analysis.projectType = "Custom Project";
        analysis.techStack = new ArrayList<>();
        analysis.complexity = "Intermediate";
        analysis.recentCommits = new ArrayList<>();

        return analysis;
    }

    private String callAIApi(String prompt) throws Exception {
        URL url = new URL(AI_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + AI_API_KEY);
        conn.setDoOutput(true);
        conn.setConnectTimeout(60000);
        conn.setReadTimeout(120000);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", AI_MODEL);
        requestBody.put("max_tokens", 2000);
        requestBody.put("temperature", 0.7);

        JSONArray messages = new JSONArray();
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are an expert software architect. Return only valid JSON without any other text.");
        messages.put(systemMessage);

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.put(userMessage);

        requestBody.put("messages", messages);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorResponse.append(line);
            }
            errorReader.close();
            throw new Exception("API error: " + responseCode + " - " + errorResponse.toString());
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONArray choices = jsonResponse.getJSONArray("choices");
        JSONObject choice = choices.getJSONObject(0);
        JSONObject message = choice.getJSONObject("message");
        String content = message.getString("content");

        return content;
    }

    public static class RepositoryAnalysis {
        public String repoName;
        public boolean isEmpty;
        public Map<String, List<String>> fileGroups;
        public List<String> allFiles;
        public String projectType;
        public List<String> techStack;
        public String complexity;
        public List<String> recentCommits;
        public String projectIdea;
        public String userPrompt;
    }
}