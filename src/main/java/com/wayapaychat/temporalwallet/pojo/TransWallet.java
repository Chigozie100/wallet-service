package com.wayapaychat.temporalwallet.pojo;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class TransWallet {
	
	private String acctNo;
	
	private String paymentRef;
	
	private BigDecimal tranAmount;
	
	private String tranCrncy;
	
	private Date tranDate;
	
	private String tranNarrate;
	
	private String tranType;
	
	private String partTranType;
	
	private String tranId;
	
	private String status;

	public TransWallet(String acctNo, String paymentRef, BigDecimal tranAmount, String tranCrncy, Date tranDate,
			String tranNarrate, String tranType, String partTranType, String tranId, String status) {
		super();
		this.acctNo = acctNo;
		this.paymentRef = paymentRef;
		this.tranAmount = tranAmount;
		this.tranCrncy = tranCrncy;
		this.tranDate = tranDate;
		this.tranNarrate = tranNarrate;
		this.tranType = tranType;
		this.partTranType = partTranType;
		this.tranId = tranId;
		this.status = status;
	}
	
	

}
