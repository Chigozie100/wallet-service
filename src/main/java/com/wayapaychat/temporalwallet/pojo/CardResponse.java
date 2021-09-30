package com.wayapaychat.temporalwallet.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class CardResponse {
	
	private Date timeStamp;
	
	private boolean status;
	
	private String message;
	
	private CardData data;

}
