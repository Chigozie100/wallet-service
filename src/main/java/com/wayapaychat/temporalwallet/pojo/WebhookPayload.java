package com.wayapaychat.temporalwallet.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WebhookPayload {
    private String transactionId;
    private String merchantId;
    private BigDecimal amount;
    private String currency;
    private String transactionType;
    private String status;
    private LocalDateTime transactionDate;
    private String reference;
}
