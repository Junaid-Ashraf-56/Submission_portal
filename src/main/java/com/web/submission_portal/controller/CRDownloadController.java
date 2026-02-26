package com.web.submission_portal.controller;

import com.web.submission_portal.entity.Assignment;
import com.web.submission_portal.service.AssignmentService;
import com.web.submission_portal.service.BulkDownloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/cr")
@PreAuthorize("hasRole('CR')")
@RequiredArgsConstructor
@Slf4j
public class CRDownloadController {

    private final BulkDownloadService bulkDownloadService;
    private final AssignmentService assignmentService;


    @GetMapping("/assignments/{id}/download-all")
    public ResponseEntity<Resource> downloadAllSubmissions(
            @PathVariable("id") Long assignmentId) {

        try {
            log.info("Download request for assignment: {}", assignmentId);

            Assignment assignment = assignmentService.findById(assignmentId);

            if (!bulkDownloadService.hasSubmissions(assignmentId)) {
                log.warn("No submissions found for assignment: {}", assignmentId);
                return ResponseEntity.noContent().build();
            }

            ByteArrayOutputStream zipFile = bulkDownloadService.downloadAllSubmissions(assignmentId);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String fileName = sanitizeFileName(assignment.getSubjectTitle()) + "_" + timestamp + ".zip";

            ByteArrayResource resource = new ByteArrayResource(zipFile.toByteArray());

            log.info("Downloaded {} submissions for assignment: {}",
                    bulkDownloadService.getSubmissionCount(assignmentId), assignmentId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.contentLength())
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading submissions: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}