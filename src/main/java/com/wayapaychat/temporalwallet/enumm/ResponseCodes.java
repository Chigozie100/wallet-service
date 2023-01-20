package com.wayapaychat.temporalwallet.enumm;

public enum ResponseCodes {

	SUCCESSFUL_CREDIT("ACCOUNT CREDITED SUCCESSFULLY"),
	FAILED_CREDIT("FAILED TO CREDITED ACCOUNT"),
	SUCCESSFUL_DEBIT("ACCOUNT DEBITED SUCCESSFULLY"),
	FAILED_DEBIT("FAILED TO DEBIT ACCOUNT"),
	PROCESSING_ERROR("ERROR WHILE PROCESSING TRANSACTION"),
	INVALID_AMOUNT("INVALID TRANSACTION AMOUNT"),
	SAME_ACCOUNT("DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT"),
	INVALID_BENEFICIARY_ACCOUNT("INVALID BENEFICIARY ACCOUNT"),
	NO_PROVIDER("NO PROVIDER ACTIVATED"),
	INVALID_TOKEN("INVALID TOKEN"),
	INVALID_SOURCE_ACCOUNT("INVALID SOURCE ACCOUNT"),
	INSUFFICIENT_FUNDS("INSUFFICIENT FUNDS"),
	DEBIT_LIMIT_REACHED("DEBIT LIMIT REACHED"),
	TRANSACTION_SUCCESSFUL("TRANSACTION SUCCESSFUL");

	private String value;

	private ResponseCodes(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
