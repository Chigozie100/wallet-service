package com.wayapaychat.temporalwallet.notification;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class PaymentTransactionDetail {
    private String transactionId;
    private Double amount;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public PaymentTransactionDetail(String transactionId, Double amount) {
        this.transactionId = transactionId;
        this.amount = amount;
    }
}
