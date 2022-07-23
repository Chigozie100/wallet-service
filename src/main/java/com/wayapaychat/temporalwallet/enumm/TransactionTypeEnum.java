package com.wayapaychat.temporalwallet.enumm;

public enum TransactionTypeEnum {
	
   CARD("CARD"), MONEY("MONEY"), LOCAL("LOCAL"),TRANSFER("TRANSFER"), CASH("CASH"), 
   BANK("BANK"), REVERSAL("REVERSAL"), CHARGES("CHARGES"), WITHDRAW("WITHDRAW");
	
	private String value;
	
	private TransactionTypeEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
