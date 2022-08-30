package com.wayapaychat.temporalwallet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WebHooksNotification {
    private Double amount;

    @JsonProperty("payer_account_name")
    private String payerAccountName;

    private String channel;

    private String type;

    @JsonProperty("paid_at")
    private String paidAt;

    @JsonProperty("payer_bank_code")
    private String payerBankCode;

    @JsonProperty("recipient_account_number")
    private String recipientAccountNumber;

    @JsonProperty("payer_account_number")
    private String payerAccountNumber;

    private String details;

    @JsonProperty("transaction_reference")
    private String transactionReference;
}
