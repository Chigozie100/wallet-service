package com.wayapaychat.temporalwallet.enumm;

import java.util.Optional;

public enum EventCharge {
    BANKPMT,
    FUNDINGVIACARD,
    MANAGEMENTACCOUNT,
    ADDCARD,
    INCOME_,
    VAT_,
    DISBURS_,
    COLLECTION_,
    _PAYOUT,
    _FUNDING,
    CUSTOMER_DEPOSIT,
    WAYATRAN,
    PAYSTACK;
    public static Optional<EventCharge> find(String value){
        if (isNonEmpty(value)){
            try {
                return Optional.of(EventCharge.valueOf(value.toUpperCase()));
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
