/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 *
 * @author Olawale
 */
@Data
public class CustomerStatement {
    
    private BigDecimal openingBal;
    private BigDecimal closingBal;
    private BigDecimal clearedal;
    private BigDecimal unclearedBal;
    private String accountName;
    private String accountNumber;
    private BigDecimal blockedAmount;

    private List<AccountStatement> transaction;
    
}
