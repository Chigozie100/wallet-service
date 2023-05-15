/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.wayapaychat.temporalwallet.dto;

import com.wayapaychat.temporalwallet.entity.WalletTransaction;
import com.wayapaychat.temporalwallet.enumm.CategoryType;
import com.wayapaychat.temporalwallet.enumm.TransactionTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

/**
 *
 * @author Olawale
 */
@Data
public class TransactionDTO {

    private Long id;

    private boolean del_flg;

    private boolean posted_flg;

    private String tranId;
    private String acctNum;
    private BigDecimal tranAmount;
    @Enumerated(EnumType.STRING)
    private TransactionTypeEnum tranType;
    private String partTranType;
    private String tranNarrate;
    private String tranDate;
    private String tranCrncyCode;
    private String paymentReference;
    private String tranGL;
    private Integer tranPart;
    private Long relatedTransId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private CategoryType tranCategory;
    private String createdBy;
    private String createdEmail;
    private String senderName;
    private String receiverName;
    private String transChannel;

    private boolean channel_flg = false;

    public TransactionDTO(WalletTransaction trans, String acct, String transDate) {
        super();
        this.del_flg = trans.isDel_flg();
        this.posted_flg = trans.isPosted_flg();
        this.tranId = trans.getTranId();
        this.acctNum = trans.getAcctNum();
        this.tranAmount = trans.getTranAmount();
        this.tranType = trans.getTranType();
        this.tranNarrate = trans.getTranNarrate();
        this.tranDate = transDate;
        this.tranCrncyCode = trans.getTranCrncyCode();
        this.partTranType = trans.getPartTranType();
        this.tranGL = trans.getTranGL();
        this.paymentReference = trans.getPaymentReference();
        this.createdBy = trans.getCreatedBy();
        this.createdEmail = trans.getCreatedEmail();
        this.tranPart = trans.getTranPart();
        this.senderName = trans.getSenderName();
        this.receiverName = trans.getReceiverName();
        this.relatedTransId = trans.getRelatedTransId();
        this.tranCategory = trans.getTranCategory();
        
    }
}
