package com.web.submission_portal.repository;


import com.web.submission_portal.entity.EmailLog;
import com.web.submission_portal.enums.EmailStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailLogsRepository extends JpaRepository<EmailLog, Long> {
    List<EmailLog> findByRecipient(String recipient);
    long countByStatus(EmailStatus status);
    boolean deleteAllBySentAtBefore(LocalDateTime sentAtBefore);
}
