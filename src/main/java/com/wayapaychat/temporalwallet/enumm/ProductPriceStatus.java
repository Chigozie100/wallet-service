package com.wayapaychat.temporalwallet.enumm;

import java.util.Optional;

public enum ProductPriceStatus {
    GENERAL,
    CUSTOM;

    public static Optional<ProductPriceStatus> find(String value){
        if (isNonEmpty(value)){
            try {
                return Optional.of(ProductPriceStatus.valueOf(value.toUpperCase()));
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
