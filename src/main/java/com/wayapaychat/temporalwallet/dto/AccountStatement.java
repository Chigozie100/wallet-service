package com.wayapaychat.temporalwallet.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


@Data
public class AccountStatement {
    private Date date;
    private String description;
    private String ref;
    private BigDecimal withdrawals;
    private BigDecimal deposits;
    private BigDecimal balance;
}
