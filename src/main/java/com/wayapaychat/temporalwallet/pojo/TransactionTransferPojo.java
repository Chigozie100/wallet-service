package com.wayapaychat.temporalwallet.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TransactionTransferPojo {

    private long id;
    private String fromAccount;
    private String toAccount;
    double amount;

}