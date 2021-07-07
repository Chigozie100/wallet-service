package com.wayapaychat.temporalwallet.response;

import lombok.Data;

@Data
public class BankConfig {
	
	public int id;
	
    public boolean del_flg;
    
    public String codeDesc;
    
    public String codeValue;
    
    public String codeSymbol;

}
