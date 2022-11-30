package com.wayapaychat.temporalwallet.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TransactionPojo {

    private long id;
    private String transactionType;
    private String accountNo;
    private String description;
    double amount;

    public TransactionPojo(String transactionType, String accountNo, String description, double amount) {
        this.transactionType = transactionType;
        this.accountNo = accountNo;
        this.description = description;
        this.amount = amount;
    }

    

}