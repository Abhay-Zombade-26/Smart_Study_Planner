package util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpUtil {
    
    public static String sendGet(String urlString, Map<String, String> headers) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                conn.setRequestProperty(header.getKey(), header.getValue());
            }
        }
        
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        
        int responseCode = conn.getResponseCode();
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(responseCode >= 200 && responseCode < 300 ? 
                    conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } finally {
            conn.disconnect();
        }
        
        return response.toString();
    }
    
    public static String sendPost(String urlString, Map<String, String> headers, String body) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                conn.setRequestProperty(header.getKey(), header.getValue());
            }
        }
        
        if (body != null) {
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = body.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }
        
        int responseCode = conn.getResponseCode();
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(responseCode >= 200 && responseCode < 300 ? 
                    conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } finally {
            conn.disconnect();
        }
        
        return response.toString();
    }
    
    public static String encodeParams(Map<String, String> params) {
        StringBuilder result = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (result.length() > 0) result.append("&");
                result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name()));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}