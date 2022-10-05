package com.wayapaychat.temporalwallet.service;

import com.wayapaychat.temporalwallet.entity.UserPricing;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

public interface UserPricingService {
    ResponseEntity<?> create(Long userId, BigDecimal amount, String product);

    ResponseEntity<?> update(Long userId, BigDecimal discountAmount, BigDecimal customAmount, BigDecimal capAmount, String product);

    ResponseEntity<?> getAllUserPricing(int page, int size);

    UserPricing getUserPricing(Long userId);

    ResponseEntity<?> syncWalletUser();
}
