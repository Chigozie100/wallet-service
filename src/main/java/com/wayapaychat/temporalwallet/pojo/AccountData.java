package com.wayapaychat.temporalwallet.pojo;

import lombok.Data;

@Data
public class AccountData {
	
	private Long id;
	
    private String bankName;
    
    private String bankCode;
    
    private String accountNumber;
    
    private String accountName;
    
    private String userId;
    
    private boolean deleted;

}
