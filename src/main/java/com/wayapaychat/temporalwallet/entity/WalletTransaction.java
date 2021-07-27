package com.wayapaychat.temporalwallet.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

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

import com.wayapaychat.temporalwallet.enumm.TransactionTypeEnum;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "m_wallet_transaction" , uniqueConstraints = {
        @UniqueConstraint(name = "UniqueTranIdAndAcctNumberAndDelFlgAndDate", 
        		columnNames = {"tranId", "acctNum", "del_flg","tranDate"})})
public class WalletTransaction {
	
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
    private Date tranDate;
    
    @NotNull
    private String tranCrncyCode;
    
    @Column(nullable = true)
    private String paymentReference;
    
    @NotNull
    private String tranGL;
    
	private String relatedTransId;
    
    @CreationTimestamp
    @ApiModelProperty(hidden = true)
    private LocalDateTime createdAt;

    @CreationTimestamp
    @ApiModelProperty(hidden = true)
    private LocalDateTime updatedAt;

	public WalletTransaction(@NotNull String tranId, @NotNull String acctNum,
			@NotNull BigDecimal tranAmount, @NotNull TransactionTypeEnum tranType, 
			@NotNull String tranNarrate, @NotNull Date tranDate, @NotNull String tranCrncyCode,
			@NotNull String partTranType, String tranGL,String paymentReference) {
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
	}
    
    

}
