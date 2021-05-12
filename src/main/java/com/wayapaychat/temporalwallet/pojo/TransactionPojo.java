package com.wayapaychat.temporalwallet.pojo;

import com.wayapaychat.temporalwallet.enumm.TransactionType;
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

}