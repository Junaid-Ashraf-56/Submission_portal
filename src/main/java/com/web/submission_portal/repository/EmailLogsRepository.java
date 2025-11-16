package com.web.submission_portal.repository;


import com.web.submission_portal.entity.Email_Logs;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailLogsRepository extends JpaRepository<Email_Logs, Long> {
    List<Email_Logs> findByRecipient(String recipient);
    long countByStatusFalse();
    @Transactional
    void deleteAllBySent_atBefore(LocalDateTime sent_at);
}
