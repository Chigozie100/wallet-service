package com.wayapaychat.temporalwallet.enumm;

public enum ResponseCodes {

	SUCCESSFUL_CREDIT("ACCOUNT CREDITED SUCCESSFULLY"),
	FAILED_CREDIT("FAILED TO CREDITED ACCOUNT"),
	SUCCESSFUL_DEBIT("ACCOUNT DEBITED SUCCESSFULLY"),
	FAILED_DEBIT("FAILED TO DEBIT ACCOUNT"),
	PROCESSING_ERROR("ERROR WHILE PROCESSING TRANSACTION"),
	TRANSACTION_SUCCESSFUL("TRANSACTION SUCCESSFUL");

	private String value;

	private ResponseCodes(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
