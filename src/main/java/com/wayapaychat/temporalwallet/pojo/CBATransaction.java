package com.wayapaychat.temporalwallet.pojo;

import java.math.BigDecimal;

import com.wayapaychat.temporalwallet.entity.Provider;
import com.wayapaychat.temporalwallet.enumm.CBAAction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class CBATransaction {

    private String senderName;

    private String receiverName;

    private MyData userToken;

    private String paymentReference;
    
    private String transitGLAccount;
    
    private String creditGLAccount;
    
    private String debitGLAccount;
    
    private String customerAccount;
    
    private String narration;
    
    private String category;

    private String type;
    
    private BigDecimal amount;

    private BigDecimal charge;

    private BigDecimal vat;
    
    private Provider provider;

    private String channelEvent;

    private Long sessionID;

    private CBAAction action;

    private BigDecimal fee = BigDecimal.ZERO;
    private String transactionChannel;

}