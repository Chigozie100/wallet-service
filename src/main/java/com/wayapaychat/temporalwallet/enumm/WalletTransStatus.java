package com.wayapaychat.temporalwallet.enumm;

import java.util.Optional;

public enum WalletTransStatus {
    //// PENDING  // REVERSED  // SUCCESSFUL

    PENDING,
    REVERSED,
    SUCCESSFUL;

    public static Optional<WalletTransStatus> find(String value){
        if (isNonEmpty(value)){
            try {
                return Optional.of(WalletTransStatus.valueOf(value.toUpperCase()));
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
