package com.wayapaychat.temporalwallet.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MifosTransactionPojo {

	private Long customerWalletid;
	private Long beneficiaryWalletId;
	private String paymentReference;
	private Float amount;
	private String description;
}
