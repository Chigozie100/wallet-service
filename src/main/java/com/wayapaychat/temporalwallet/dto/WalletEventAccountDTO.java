package com.wayapaychat.temporalwallet.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;
@Data
public class WalletEventAccountDTO {
	
	@NotNull
	@Size(min=5, max=10)
    private String placeholderCode;
	
	@NotNull
	@Size(min=3, max=10)
    private String crncyCode;
	
	@NotNull
	@Size(min=5, max=10)
    private String productCode;
	
	@NotNull
	@Size(min=5, max=50)
    private String accountName;
	
	@NotNull
	@Size(min=5, max=10)
    private String productGL;
	
	private String eventId;

	public WalletEventAccountDTO(@NotNull @Size(min = 5, max = 10) String placeholderCode,
			@NotNull @Size(min = 3, max = 10) String crncyCode, @NotNull @Size(min = 5, max = 10) String productCode,
			@NotNull @Size(min = 5, max = 50) String accountName, @NotNull @Size(min = 5, max = 10) String productGL,
			String eventId) {
		super();
		this.placeholderCode = placeholderCode;
		this.crncyCode = crncyCode;
		this.productCode = productCode;
		this.accountName = accountName;
		this.productGL = productGL;
		this.eventId = eventId;
	}

	public WalletEventAccountDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	

}
