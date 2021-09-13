package com.wayapaychat.temporalwallet.response;

import lombok.Data;

@Data
public class ValidateResponse {
	
	private Boolean status;

    private Integer code;

    private String message;

	public ValidateResponse(Boolean status, Integer code, String message) {
		super();
		this.status = status;
		this.code = code;
		this.message = message;
	}
    

}
