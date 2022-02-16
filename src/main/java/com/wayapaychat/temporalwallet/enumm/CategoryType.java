package com.wayapaychat.temporalwallet.enumm;

public enum CategoryType {
	
	FUNDING("FUNDING"),TRANSFER("TRANSFER"), COMMISSION("COMMISSION"), 
	   BONUS("BONUS"), AIRTIME_TOPUP("AIRTIME TOPUP"), WITHDRAW("WITHDRAW"), PAYMENT_RECEIVED("PAYMENT RECEIVED")
	   ,PAYMENT_REQUEST("PAYMENT REQUEST"), DATA_TOPUP("DATA TOPUP"), CABLE("CABLE"), 
	   UTILITY("UTILITY"), BETTING("BETTING");
		
		private String value;
		
		private CategoryType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

}
