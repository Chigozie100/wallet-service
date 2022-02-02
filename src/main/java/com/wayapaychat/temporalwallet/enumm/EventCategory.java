package com.wayapaychat.temporalwallet.enumm;

import java.util.Optional;

public enum EventCategory {
	
	WELCOME,
    DISPUTE,
    TRANSACTION,
    PASSWORD_RESET,
    PASSWORD_CREATE,
    PIN_RESET,
    NON_WAYA,
    LOGIN_ATTEMPT,
    CARD_TRANSFER,
    WITHDRAWAL,
    INVITE_TEAM;
    public static Optional<EventCategory> find(String value){
        if (isNonEmpty(value)){
            try {
                return Optional.of(EventCategory.valueOf(value.toUpperCase()));
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
