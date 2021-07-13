package com.wayapaychat.temporalwallet.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class AccountGLDTO {
	
	@NotNull
	@Size(min=4, max=5)
	private String solId;
	
	@NotNull
	@Size(min=3, max=3)
    private String crncyCode;
	
	@NotNull
	@Size(min=5, max=50)
	private String glName;
	
	@NotNull
	@Size(min=5, max=5)
	private String glCode;
	
	@NotNull
	@Size(min=5, max=5)
	private String glSubHeadCode;

}
