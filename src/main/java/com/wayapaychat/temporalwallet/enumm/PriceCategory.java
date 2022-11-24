package com.wayapaychat.temporalwallet.enumm;

import java.util.Optional;

public enum PriceCategory {
    FIXED,
    PERCENTAGE;

    public static Optional<PriceCategory> find(String value){
        if (isNonEmpty(value)){
            try {
                return Optional.of(PriceCategory.valueOf(value.toUpperCase()));
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
