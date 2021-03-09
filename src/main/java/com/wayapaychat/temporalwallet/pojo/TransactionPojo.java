package com.wayapaychat.temporalwallet.pojo;

import com.wayapaychat.temporalwallet.entity.Account;
import com.wayapaychat.temporalwallet.enumm.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TransactionPojo {

    private long id;
    private TransactionType transactionType;
    private String accountNo;
    private String description;
    double amount;

}