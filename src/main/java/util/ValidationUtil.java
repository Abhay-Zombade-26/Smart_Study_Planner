package util;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern GITHUB_REPO_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidFutureDate(LocalDate date) {
        return date != null && !date.isBefore(LocalDate.now());
    }

    public static boolean isValidGithubRepo(String repo) {
        return repo != null && GITHUB_REPO_PATTERN.matcher(repo).matches();
    }

    public static boolean isWithinRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    public static String sanitizeInput(String input) {
        if (input == null) return null;
        return input.trim().replaceAll("\\s+", " ");
    }
}