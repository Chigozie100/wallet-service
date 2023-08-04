package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AccountDetailDTO {
	
	private long id;
	
	private String branchId;
	
	private String accountNo;
	
	private String accountName;
	
	private String productCode;
	
	private BigDecimal balance;
	
	private String currencyCode;
	
	private boolean accountDefault;

	private String nubanAccountNo;

	private boolean acctClosureFlag;
	private boolean acctDeleteFlag;

	public AccountDetailDTO(long id, String branchId, String accountNo, String accountName, String productCode,
							BigDecimal balance, String currencyCode, boolean accountDefault,boolean acctClosureFlag,
							boolean acctDeleteFlag,String nubanAccountNo) {
		super();
		this.id = id;
		this.branchId = branchId;
		this.accountNo = accountNo;
		this.accountName = accountName;
		this.productCode = productCode;
		this.balance = balance;
		this.currencyCode = currencyCode;
		this.accountDefault = accountDefault;
		this.acctClosureFlag = acctClosureFlag;
		this.acctDeleteFlag = acctDeleteFlag;
		this.nubanAccountNo = nubanAccountNo;
	}

	public AccountDetailDTO(long id, String branchId, String accountNo, String accountName, String productCode,
			BigDecimal balance, String currencyCode, boolean accountDefault) {
		super();
		this.id = id;
		this.branchId = branchId;
		this.accountNo = accountNo;
		this.accountName = accountName;
		this.productCode = productCode;
		this.balance = balance;
		this.currencyCode = currencyCode;
		this.accountDefault = accountDefault;
	}


	public AccountDetailDTO(long id, String branchId, String nubanAccountNo, String accountName, String productCode,
							BigDecimal balance, String currencyCode) {
		super();
		this.id = id;
		this.branchId = branchId;
		this.nubanAccountNo = nubanAccountNo;
		this.accountName = accountName;
		this.productCode = productCode;
		this.balance = balance;
		this.currencyCode = currencyCode;
	}

	public AccountDetailDTO(long id, String branchId, String nubanAccountNo, String accountName,
							BigDecimal balance, String currencyCode) {
		super();
		this.id = id;
		this.branchId = branchId;
		this.nubanAccountNo = nubanAccountNo;
		this.accountName = accountName;
		this.balance = balance;
		this.currencyCode = currencyCode;
	}

	public AccountDetailDTO() {
	}

}
