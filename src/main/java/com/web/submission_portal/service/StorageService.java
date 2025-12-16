package com.web.submission_portal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class StorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket.name}")
    private String bucketName;


    public Map<String, String> uploadFile(MultipartFile file, Long assignmentId, Long studentId) throws IOException {

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String originalFileName = file.getOriginalFilename();
        String storedFileName = timestamp + "_" + sanitizeFileName(originalFileName);

        String filePath = String.format("assignments/%d/%d/%s", assignmentId, studentId, storedFileName);

        String fileUrl = uploadToSupabase(file.getBytes(), filePath, file.getContentType());

        Map<String, String> fileInfo = new HashMap<>();
        fileInfo.put("fileUrl", fileUrl);
        fileInfo.put("filePath", filePath);
        fileInfo.put("storedFileName", storedFileName);
        fileInfo.put("originalFileName", originalFileName);
        fileInfo.put("fileSize", String.valueOf(file.getSize()));
        fileInfo.put("fileType", file.getContentType());

        log.info("File uploaded successfully: {}", filePath);
        return fileInfo;
    }

    public byte[] downloadFile(String filePath) throws IOException {
        String downloadUrl = String.format("%s/storage/v1/object/%s/%s",
                supabaseUrl, bucketName, encodeUrlPath(filePath));

        log.debug("Downloading file from: {}", downloadUrl);

        HttpURLConnection connection = null;
        try {
            URL url = new URL(downloadUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + supabaseKey);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream is = connection.getInputStream();
                     ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

                    byte[] data = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(data)) != -1) {
                        buffer.write(data, 0, bytesRead);
                    }

                    log.debug("File downloaded successfully: {} bytes", buffer.size());
                    return buffer.toByteArray();
                }
            } else {
                String errorMessage = readErrorStream(connection);
                throw new IOException("Failed to download file. Response code: " + responseCode + " - " + errorMessage);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String uploadToSupabase(byte[] fileData, String filePath, String contentType) throws IOException {
        String uploadUrl = String.format("%s/storage/v1/object/%s/%s",
                supabaseUrl, bucketName, encodeUrlPath(filePath));

        log.debug("Uploading to: {}", uploadUrl);

        HttpURLConnection connection = null;
        try {
            URL url = new URL(uploadUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Bearer " + supabaseKey);
            connection.setRequestProperty("Content-Type", contentType);
            connection.setRequestProperty("Content-Length", String.valueOf(fileData.length));
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);

            // Write file data
            try (OutputStream os = connection.getOutputStream()) {
                os.write(fileData);
                os.flush();
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                // Return public URL
                String publicUrl = String.format("%s/storage/v1/object/public/%s/%s",
                        supabaseUrl, bucketName, encodeUrlPath(filePath));
                log.info("Upload successful. File available at: {}", publicUrl);
                return publicUrl;
            } else {
                String errorMessage = readErrorStream(connection);
                throw new IOException("Failed to upload file. Response code: " + responseCode + " - " + errorMessage);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    public boolean deleteFile(String filePath) {
        try {
            String deleteUrl = String.format("%s/storage/v1/object/%s/%s",
                    supabaseUrl, bucketName, encodeUrlPath(filePath));

            HttpURLConnection connection = (HttpURLConnection) new URL(deleteUrl).openConnection();
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Authorization", "Bearer " + supabaseKey);

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                log.info("File deleted successfully: {}", filePath);
                return true;
            } else {
                log.error("Failed to delete file. Response code: {}", responseCode);
                return false;
            }
        } catch (Exception e) {
            log.error("Error deleting file: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if file exists
     */
    public boolean fileExists(String filePath) {
        try {
            String checkUrl = String.format("%s/storage/v1/object/%s/%s",
                    supabaseUrl, bucketName, encodeUrlPath(filePath));

            HttpURLConnection connection = (HttpURLConnection) new URL(checkUrl).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setRequestProperty("Authorization", "Bearer " + supabaseKey);

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sanitize filename to remove special characters
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) return "file";
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * Encode URL path segments
     */
    private String encodeUrlPath(String path) {
        try {
            // Split by / and encode each segment
            String[] segments = path.split("/");
            StringBuilder encoded = new StringBuilder();
            for (int i = 0; i < segments.length; i++) {
                if (i > 0) encoded.append("/");
                encoded.append(URLEncoder.encode(segments[i], StandardCharsets.UTF_8)
                        .replace("+", "%20"));
            }
            return encoded.toString();
        } catch (Exception e) {
            return path;
        }
    }

    /**
     * Read error message from connection
     */
    private String readErrorStream(HttpURLConnection connection) {
        try (InputStream errorStream = connection.getErrorStream()) {
            if (errorStream != null) {
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = errorStream.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                return result.toString(StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.error("Failed to read error stream", e);
        }
        return "Unknown error";
    }
}