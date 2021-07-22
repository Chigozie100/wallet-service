package com.wayapaychat.temporalwallet.enumm;

public enum TransactionTypeEnum {
	
   CARD("CARD"), MONEY("MONEY"), LOCAL("LOCAL"), BANK("BANK");
	
	private String value;
	
	private TransactionTypeEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
