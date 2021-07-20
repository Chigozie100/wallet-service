package com.wayapaychat.temporalwallet.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class WalletTellerDTO {
	
	@NotNull
	@Size(min=3, max=5)
	private String crncyCode;
	
	@NotNull
	private Long userId;
	
	@NotNull
	@Size(min=5, max=10)
	private String cashAccountCode;
	
	@NotNull
	@Size(min=4, max=4)
	private String solId;

}
