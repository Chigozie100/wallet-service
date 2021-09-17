package com.wayapaychat.temporalwallet.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class AccountResponse {
	
    private Date timeStamp;
	
	private boolean status;
	
	private String message;
	
	private AccountData data;

}
