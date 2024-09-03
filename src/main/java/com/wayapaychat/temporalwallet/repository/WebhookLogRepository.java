package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.WebhookLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhookLogRepository extends JpaRepository<WebhookLogs, Long> {
}
