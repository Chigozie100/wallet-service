package com.wayapaychat.temporalwallet.response;

import java.util.List;

import lombok.Data;

@Data
public class Datum {
	
	public int id;
	
    public boolean del_flg;
    
    public String codeName;
    
    public List<BankConfig> bankConfig;

}
