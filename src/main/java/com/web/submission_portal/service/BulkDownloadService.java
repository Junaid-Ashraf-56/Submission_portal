package com.web.submission_portal.service;

import com.web.submission_portal.entity.Submission;
import com.web.submission_portal.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkDownloadService {

    private final SubmissionRepository submissionRepository;
    private final StorageService StorageService;

    public ByteArrayOutputStream downloadAllSubmissions(Long assignmentId) throws IOException {
        // Get all submissions
        List<Submission> submissions = submissionRepository.findByAssignmentAssignmentId(assignmentId);

        if (submissions.isEmpty()) {
            throw new IOException("No submissions found");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (Submission submission : submissions) {
                try {

                    byte[] fileData = StorageService.downloadFile(submission.getFilePath());

                    String folder = submission.getStudent().getName() + "_" +
                            submission.getStudent().getStudentId();
                    String zipPath = folder + "/" + submission.getOriginalFileName();

                    ZipEntry zipEntry = new ZipEntry(zipPath);
                    zos.putNextEntry(zipEntry);
                    zos.write(fileData);
                    zos.closeEntry();

                } catch (Exception e) {
                    log.error("Failed to add submission to ZIP: {}", e.getMessage());
                }
            }
        }

        return baos;
    }

    public boolean hasSubmissions(Long assignmentId) {
        return submissionRepository.existsByAssignmentAssignmentId(assignmentId);
    }

    public long getSubmissionCount(Long assignmentId) {
        return submissionRepository.countByAssignmentAssignmentId(assignmentId);
    }
}