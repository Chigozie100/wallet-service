package com.wayapaychat.temporalwallet.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TransactionTransferPojo {

    private long id;
    private String fromAccount;
    private String toAccount;
    double amount;

}