package com.wayapaychat.temporalwallet.service;

import com.wayapaychat.temporalwallet.dto.BillerManagementResponse;
import com.wayapaychat.temporalwallet.exception.CustomException;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

public interface UserPricingService {
    ResponseEntity<?> create(Long userId, String fullName, BigDecimal amount, String product);

    ResponseEntity<?> update(Long userId, BigDecimal discountAmount, BigDecimal customAmount, BigDecimal capAmount, String product);


    ResponseEntity<?> updateCustomProduct(BigDecimal capAmount, BigDecimal discountAmount, BigDecimal customAmount, String product);

    ResponseEntity<?> getAllUserPricing(int page, int size);


    ResponseEntity<?> applyDiscountToAll(BigDecimal discountAmount);

    ResponseEntity<?> applyCapToAll(BigDecimal capAmount);

    ResponseEntity<?> deleteAll(String apiKey);

    ResponseEntity<?> applyGeneralToAll(BigDecimal amount);

    ResponseEntity<?> syncWalletUser(String apiKey);

    ResponseEntity<List<BillerManagementResponse>> syncBillers(String apiKey) throws CustomException;
}
