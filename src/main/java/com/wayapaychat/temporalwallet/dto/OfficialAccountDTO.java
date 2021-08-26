package com.wayapaychat.temporalwallet.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class OfficialAccountDTO {
	
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

}
