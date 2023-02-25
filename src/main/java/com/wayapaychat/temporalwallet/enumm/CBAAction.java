package com.wayapaychat.temporalwallet.enumm;

import java.util.Optional;

public enum CBAAction {

    MOVE_TO_TRANSIT,
    MOVE_FROM_TRANSIT,
    MOVE_GL_TO_GL,
    MOVE_CUSTOMER_TO_CUSTOMER,
    MOVE_GL_TO_CUSTOMER, 
    MOVE_CUSTOMER_TO_GL, 
    REVERSE_FROM_TRANSIT,
    DEPOSIT, 
    WITHDRAWAL;

    public static Optional<CBAAction> find(String value) {
        if (isEmpty(value)) {
            return Optional.empty();
        }
        try {
            return Optional.of(CBAAction.valueOf(value.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static boolean isEmpty(String value) {
        if (value == null)
            return true;
        return value.isEmpty();
    }

}
