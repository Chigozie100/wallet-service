package com.wayapaychat.temporalwallet.entity;

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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.wayapaychat.temporalwallet.enumm.PaymentStatus;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "m_wallet_nonwaya_payment" , uniqueConstraints = {
        @UniqueConstraint(name = "UniqueTranIdAndAcctNoAndDelFlgAndDateToken", 
        		columnNames = {"tranId", "debitAccountNo", "del_flg","tranDate","tokenId"})})
public class WalletNonWayaPayment {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Long id;
	
	@Column(unique = true, nullable = false)
    private String tokenId;
	
	@Column(nullable = false)
    private String emailOrPhone;
	
	private boolean del_flg = false;

	private boolean process_flg = false;
	
	@NotNull
	private String tranId;
    
	@NotNull
    private String debitAccountNo;
    
    @NotNull
    private BigDecimal tranAmount;
    
    @NotNull
    private String tranNarrate;
    
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@JsonDeserialize(using = LocalDateDeserializer.class)
	@JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate tranDate;
    
    @Column(nullable = false)
    private String crncyCode;
    
    @Column(nullable = true)
    private String paymentReference;
    
    @CreationTimestamp
    @ApiModelProperty(hidden = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdAt;

    @CreationTimestamp
    @ApiModelProperty(hidden = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime updatedAt;
    
    private String createdBy;
    
    private String createdEmail;
    
    private String redeemedBy;
    
    private String redeemedEmail;
    
    private LocalDateTime redeemedAt;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    
    private String confirmPIN;
    
    @Column(nullable = false)
    private String fullName;
    
    private Long merchantId;

	public WalletNonWayaPayment(String tokenId, String emailOrPhone, String tranId,
			String debitAccountNo, BigDecimal tranAmount, String tranNarrate,
			String crncyCode, String paymentReference, String createdBy, 
			String createdEmail, PaymentStatus status, String fullName) {
		super();
		this.tokenId = tokenId;
		this.emailOrPhone = emailOrPhone;
		this.tranId = tranId;
		this.debitAccountNo = debitAccountNo;
		this.tranAmount = tranAmount;
		this.tranNarrate = tranNarrate;
		this.tranDate = LocalDate.now();
		this.crncyCode = crncyCode;
		this.paymentReference = paymentReference;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
		this.createdBy = createdBy;
		this.createdEmail = createdEmail;
		this.status = status;
		this.fullName = fullName;
	}

    public BigDecimal sumTranAmount(BigDecimal tranAmount){

//        BigDecimal sum = BigDecimal.ZERO;/
//        for (Accounting value : values) {
//
//            sum = sum.add(value.getAmountBal());
//        }
//        return sum;
        return this.tranAmount.add(tranAmount);
    }
}
