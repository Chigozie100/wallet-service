package com.wayapaychat.temporalwallet.enumm;

public enum TransactionTypeEnum {
	
   CARD("CARD"), MONEY("MONEY"), LOCAL("LOCAL"), CASH("CASH"), BANK("BANK"), REVERSAL("REVERSAL");
	
	private String value;
	
	private TransactionTypeEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
