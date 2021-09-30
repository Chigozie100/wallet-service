package com.wayapaychat.temporalwallet.pojo;

import lombok.Data;

@Data
public class CardData {
	
	private String authorization_url;
	
    private String access_code;
    
    private String reference;

}
