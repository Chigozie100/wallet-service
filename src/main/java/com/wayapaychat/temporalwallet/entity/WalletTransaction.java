package com.wayapaychat.temporalwallet.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.wayapaychat.temporalwallet.enumm.CategoryType;
import com.wayapaychat.temporalwallet.enumm.TransactionTypeEnum;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "m_wallet_transaction", uniqueConstraints = {
        @UniqueConstraint(name = "UniqueTranIdAndAcctNumberAndDelFlgAndDate", columnNames = { "tranId", "acctNum",
                "del_flg", "tranDate", "tranPart" }) })
public class WalletTransaction implements Serializable {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Version
    protected int version;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Long id;

    private boolean del_flg;

    private boolean posted_flg;

    @NotNull
    private String tranId;

    @NotNull
    private String acctNum;

    @NotNull
    private BigDecimal tranAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TransactionTypeEnum tranType;

    @NotNull
    private String partTranType;

    @NotNull
    private String tranNarrate;

    @NotNull
    private LocalDate tranDate;

    @Column(nullable = false)
    private String tranCrncyCode;

    @Column(nullable = true)
    private String paymentReference;

    @Column(nullable = false)
    private String tranGL;

    @Column(nullable = true)
    private Integer tranPart;

    @JsonSerialize(using=ToStringSerializer.class)
    private Long relatedTransId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @CreationTimestamp
    @ApiModelProperty(hidden = true)
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @CreationTimestamp
    @ApiModelProperty(hidden = true)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private CategoryType tranCategory;

    private String createdBy;

    private String createdEmail;

    @Column(name = "sender_name")
    private String senderName;

    @Column(name = "receiver_name")
    private String receiverName;

    private String transChannel;

    private boolean channel_flg = false;
    @Column(name = "charge_amount")
    private BigDecimal chargeAmount = BigDecimal.ZERO;
    private BigDecimal vat = BigDecimal.ZERO;

    public WalletTransaction(@NotNull String tranId, @NotNull String acctNum,
            @NotNull BigDecimal tranAmount, @NotNull TransactionTypeEnum tranType,
            @NotNull String tranNarrate, @NotNull LocalDate tranDate, @NotNull String tranCrncyCode,
            @NotNull String partTranType, String tranGL, String paymentReference,
            String createdBy, String createdEmail, Integer tranPart) {
        super();
        this.del_flg = false;
        this.posted_flg = true;
        this.tranId = tranId;
        this.acctNum = acctNum;
        this.tranAmount = tranAmount;
        this.tranType = tranType;
        this.tranNarrate = tranNarrate;
        this.tranDate = tranDate;
        this.tranCrncyCode = tranCrncyCode;
        this.partTranType = partTranType;
        this.tranGL = tranGL;
        this.paymentReference = paymentReference;
        this.createdBy = createdBy;
        this.createdEmail = createdEmail;
        this.tranPart = tranPart;

    }

    public WalletTransaction(@NotNull String tranId, @NotNull String acctNum,
            @NotNull BigDecimal tranAmount, @NotNull TransactionTypeEnum tranType,
            @NotNull String tranNarrate, @NotNull LocalDate tranDate, @NotNull String tranCrncyCode,
            @NotNull String partTranType, String tranGL, String paymentReference,
            String createdBy, String createdEmail, Integer tranPart, String senderName, String receiverName,
                             BigDecimal chargeAmount,BigDecimal vat) {
        super();
        this.del_flg = false;
        this.posted_flg = true;
        this.tranId = tranId;
        this.acctNum = acctNum;
        this.tranAmount = tranAmount;
        this.tranType = tranType;
        this.tranNarrate = tranNarrate;
        this.tranDate = tranDate;
        this.tranCrncyCode = tranCrncyCode;
        this.partTranType = partTranType;
        this.tranGL = tranGL;
        this.paymentReference = paymentReference;
        this.createdBy = createdBy;
        this.createdEmail = createdEmail;
        this.tranPart = tranPart;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.chargeAmount = chargeAmount;
        this.vat = vat;
    }

    public WalletTransaction(@NotNull Long relatedTransId, @NotNull String tranId, @NotNull String acctNum,
            @NotNull BigDecimal tranAmount, @NotNull TransactionTypeEnum tranType,
            @NotNull String tranNarrate, @NotNull LocalDate tranDate, @NotNull String tranCrncyCode,
            @NotNull String partTranType, String tranGL, String paymentReference,
            String createdBy, String createdEmail, Integer tranPart, CategoryType tranCategory, String senderName,
            String receiverName,BigDecimal chargeAmount, BigDecimal vat) {
        super();
        this.del_flg = false;
        this.posted_flg = true;
        this.tranId = tranId;
        this.acctNum = acctNum;
        this.tranAmount = tranAmount;
        this.tranType = tranType;
        this.tranNarrate = tranNarrate;
        this.tranDate = tranDate;
        this.tranCrncyCode = tranCrncyCode;
        this.partTranType = partTranType;
        this.tranGL = tranGL;
        this.paymentReference = paymentReference;
        this.createdBy = createdBy;
        this.createdEmail = createdEmail;
        this.tranPart = tranPart;
        this.tranCategory = tranCategory;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.relatedTransId = relatedTransId;
        this.chargeAmount = chargeAmount;
        this.vat = vat;
    }

    public WalletTransaction(@NotNull String tranId, @NotNull String acctNum,
            @NotNull BigDecimal tranAmount, @NotNull TransactionTypeEnum tranType,
            @NotNull String tranNarrate, @NotNull LocalDate tranDate, @NotNull String tranCrncyCode,
            @NotNull String partTranType, String tranGL, String paymentReference, Long relatedTransId,
            String createdBy, String createdEmail, Integer tranPart, CategoryType tranCategory) {
        super();
        this.del_flg = false;
        this.posted_flg = true;
        this.tranId = tranId;
        this.acctNum = acctNum;
        this.tranAmount = tranAmount;
        this.tranType = tranType;
        this.tranNarrate = tranNarrate;
        this.tranDate = tranDate;
        this.tranCrncyCode = tranCrncyCode;
        this.partTranType = partTranType;
        this.tranGL = tranGL;
        this.paymentReference = paymentReference;
        this.relatedTransId = relatedTransId;
        this.createdBy = createdBy;
        this.createdEmail = createdEmail;
        this.tranPart = tranPart;
        this.tranCategory = tranCategory;
    }

}