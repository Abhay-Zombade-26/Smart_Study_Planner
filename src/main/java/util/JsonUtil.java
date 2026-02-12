package util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class JsonUtil {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public static JSONObject parseObject(String json) {
        return new JSONObject(json);
    }

    public static JSONArray parseArray(String json) {
        return new JSONArray(json);
    }

    public static String getString(JSONObject obj, String key) {
        return obj.has(key) && !obj.isNull(key) ? obj.getString(key) : null;
    }

    public static int getInt(JSONObject obj, String key, int defaultValue) {
        return obj.has(key) && !obj.isNull(key) ? obj.getInt(key) : defaultValue;
    }

    public static LocalDate getDate(JSONObject obj, String key) {
        if (obj.has(key) && !obj.isNull(key)) {
            String dateStr = obj.getString(key);
            return LocalDate.parse(dateStr.substring(0, 10), ISO_FORMATTER);
        }
        return null;
    }

    public static Map<String, String> toMap(JSONObject obj) {
        Map<String, String> map = new HashMap<>();
        if (obj != null) {
            for (String key : obj.keySet()) {
                Object value = obj.get(key);
                map.put(key, value != null ? value.toString() : null);
            }
        }
        return map;
    }
}