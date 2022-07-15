package com.wayapaychat.temporalwallet.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class EventPaymentSettlementDTO {

    @NotNull
    @Size(min=6, max=50)
    private String eventId;

    @NotNull
    @Size(min=10, max=10)
    private String customerAccountNumber;


    @NotNull
    @Size(min=10, max=10)
    private String customerCommissionNumber;

    @NotNull
    private BigDecimal amount;

    @NotNull
    @Size(min=3, max=5)
    private String tranCrncy;

    @NotNull
    @Size(min=5, max=50)
    private String tranNarration;

    @NotNull
    @Size(min=3, max=50)
    private String paymentReference;

    @NotNull
    @Size(min=3, max=20)
    private String transactionCategory;

}
