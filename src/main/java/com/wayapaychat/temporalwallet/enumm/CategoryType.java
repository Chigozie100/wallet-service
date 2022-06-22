package com.wayapaychat.temporalwallet.enumm;

public enum CategoryType {
	
	FUNDING("FUNDING"),TRANSFER("TRANSFER"), COMMISSION("COMMISSION"), 
	   BONUS("BONUS"), AIRTIME_TOPUP("AIRTIME_TOPUP"), WITHDRAW("WITHDRAW"),
	PAYMENT_RECEIVED("PAYMENT_RECEIVED"), PAYMENT_REQUEST("PAYMENT_REQUEST"), DATA_TOPUP("DATA_TOPUP"), CABLE("CABLE"),
	   UTILITY("UTILITY"), BETTING("BETTING"),REVERSAL("REVERSAL");
		
		private String value;
		
		private CategoryType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

}
