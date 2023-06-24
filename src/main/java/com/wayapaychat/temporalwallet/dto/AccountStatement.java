package com.wayapaychat.temporalwallet.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


@Data
public class AccountStatement {
    private String date;
    private String description;
    private String ref;
    private String withdrawals;
    private String deposits;
    private BigDecimal balance;
    private String valueDate;
    private String sender;
    private String receiver;
}
