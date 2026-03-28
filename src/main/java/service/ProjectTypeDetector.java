package service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class ProjectTypeDetector {

    private final String GITHUB_API = "https://api.github.com/repos/";

    public enum ProjectType {
        JAVA_BACKEND, SPRING_BOOT, NODE_BACKEND, REACT_FRONTEND,
        STATIC_WEBSITE, PYTHON_BACKEND, FLASK, DJANGO,
        ANDROID, IOS, LIBRARY, OTHER
    }

    public ProjectType detectProjectType(String accessToken, String repoFullName) {
        try {
            String[] parts = repoFullName.split("/");
            String owner = parts[0];
            String repo = parts[1];

            // Check for pom.xml (Java/Spring)
            if (fileExists(accessToken, owner, repo, "pom.xml")) {
                String content = getFileContent(accessToken, owner, repo, "pom.xml");
                if (content.contains("spring-boot")) {
                    return ProjectType.SPRING_BOOT;
                }
                return ProjectType.JAVA_BACKEND;
            }

            // Check for package.json (Node/React)
            if (fileExists(accessToken, owner, repo, "package.json")) {
                String content = getFileContent(accessToken, owner, repo, "package.json");
                if (content.contains("react") || content.contains("next")) {
                    return ProjectType.REACT_FRONTEND;
                }
                return ProjectType.NODE_BACKEND;
            }

            // Check for requirements.txt (Python)
            if (fileExists(accessToken, owner, repo, "requirements.txt")) {
                String content = getFileContent(accessToken, owner, repo, "requirements.txt");
                if (content.contains("django")) {
                    return ProjectType.DJANGO;
                }
                if (content.contains("flask")) {
                    return ProjectType.FLASK;
                }
                return ProjectType.PYTHON_BACKEND;
            }

            // Check for index.html only
            if (fileExists(accessToken, owner, repo, "index.html") &&
                    !fileExists(accessToken, owner, repo, "package.json")) {
                return ProjectType.STATIC_WEBSITE;
            }

            return ProjectType.OTHER;

        } catch (Exception e) {
            e.printStackTrace();
            return ProjectType.OTHER;
        }
    }

    private boolean fileExists(String accessToken, String owner, String repo, String filePath) {
        try {
            String urlStr = String.format("https://api.github.com/repos/%s/%s/contents/%s",
                    owner, repo, filePath);
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "token " + accessToken);

            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private String getFileContent(String accessToken, String owner, String repo, String filePath) {
        try {
            String urlStr = String.format("https://api.github.com/repos/%s/%s/contents/%s",
                    owner, repo, filePath);
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "token " + accessToken);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(response.toString());
            String content = json.getString("content");
            // Decode base64 content
            byte[] decoded = java.util.Base64.getDecoder().decode(content);
            return new String(decoded);

        } catch (Exception e) {
            return "";
        }
    }
}