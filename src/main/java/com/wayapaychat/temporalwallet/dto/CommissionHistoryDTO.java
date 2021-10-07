package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class CommissionHistoryDTO {
	
	private Long id;
	
	private String acct_name;
	
	private String account_no;
	
	private String email;
	
	private BigDecimal balance;
	
	private Date custDate;

	public CommissionHistoryDTO(Long id, String acct_name, String account_no, String email, BigDecimal balance, Date custDate) {
		super();
		this.id = id;
		this.acct_name = acct_name;
		this.account_no = account_no;
		this.email = email;
		this.balance = balance;
		this.custDate = custDate;
	}
	

}
