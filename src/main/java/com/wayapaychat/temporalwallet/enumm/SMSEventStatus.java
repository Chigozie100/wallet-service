package com.wayapaychat.temporalwallet.enumm;

import java.util.Optional;

public enum SMSEventStatus {
	
	TRANSACTION,
	NONWAYA,
    QRCODE,
    DONATE,
    BILLSPAYMENT,
    MESSAGING,
    ADVERT;

    public static Optional<SMSEventStatus> find(String value){
        if (isNonEmpty(value)){
            try {
                return Optional.of(SMSEventStatus.valueOf(value.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
    
    public static boolean isNonEmpty(String value){
        return value != null && !value.isEmpty();
    }

}
