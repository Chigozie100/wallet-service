package com.wayapaychat.temporalwallet.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionPojo2 {

	private String accountNo;
	private Double amount;
	private String description;
	private Integer id;
	private String transactionType;
}
