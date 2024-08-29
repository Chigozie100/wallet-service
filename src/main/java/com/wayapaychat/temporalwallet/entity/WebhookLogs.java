package com.wayapaychat.temporalwallet.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Table
@Data
@Entity
public class WebhookLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private UUID id;

    private Long merchantId;
    private Long transactionId;
    private String webhookUrl;
    private Integer responseStatus;
    @Column(columnDefinition = "TEXT")
    private String responseBody;
    private Integer attempts;
    private LocalDateTime LastAttemptAt;
    private LocalDateTime createdAt;

}
