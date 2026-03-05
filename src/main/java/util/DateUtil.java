package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DISPLAY_FORMATTER) : "";
    }

    public static LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(LocalDate.now());
    }

    public static boolean isYesterday(LocalDate date) {
        return date != null && date.equals(LocalDate.now().minusDays(1));
    }

    public static String getRelativeDate(LocalDate date) {
        if (date == null) return "Never";

        LocalDate today = LocalDate.now();
        if (date.equals(today)) return "Today";
        if (date.equals(today.minusDays(1))) return "Yesterday";
        if (date.equals(today.plusDays(1))) return "Tomorrow";

        long days = daysBetween(today, date);
        if (days > 0) return "In " + days + " days";
        if (days < 0) return Math.abs(days) + " days ago";

        return formatDate(date);
    }
}