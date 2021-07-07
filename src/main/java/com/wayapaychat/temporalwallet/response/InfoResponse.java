package com.wayapaychat.temporalwallet.response;

import java.util.Date;
import java.util.List;

import com.wayapaychat.temporalwallet.entity.WalletConfig;

import lombok.Data;

@Data
public class InfoResponse {
	
	public Date timeStamp;
	
    public boolean status;
    
    public String message;
    
    public List<WalletConfig> data;

	public InfoResponse(Date timeStamp, boolean status, String message, List<WalletConfig> data) {
		super();
		this.timeStamp = timeStamp;
		this.status = status;
		this.message = message;
		this.data = data;
	}
    
}
