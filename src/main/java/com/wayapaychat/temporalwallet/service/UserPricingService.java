package com.wayapaychat.temporalwallet.service;

import com.wayapaychat.temporalwallet.dto.BillerManagementResponse;
import com.wayapaychat.temporalwallet.entity.UserPricing;
import com.wayapaychat.temporalwallet.entity.WalletUser;
import com.wayapaychat.temporalwallet.enumm.PriceCategory;
import com.wayapaychat.temporalwallet.exception.CustomException;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

public interface UserPricingService {
    ResponseEntity<?> create(Long userId, String fullName, BigDecimal amount, String product, String code);
    ResponseEntity<?> createUserPricing(String userId);
    ResponseEntity<?> createUserPricing(WalletUser userx);
    ResponseEntity<?> update(Long userId, BigDecimal discountAmount, BigDecimal customAmount, BigDecimal capAmount, String product, PriceCategory priceType);
    ResponseEntity<?> updateCustomProduct(BigDecimal capAmount, BigDecimal discountAmount, BigDecimal customAmount, String product, PriceCategory priceType);
    ResponseEntity<?> getAllUserPricing(int page, int size);
    ResponseEntity<?> getAllUserPricingUserId(String userId, String product);
    ResponseEntity<?> applyDiscountToAll(BigDecimal discountAmount);
    ResponseEntity<?> applyCapToAll(BigDecimal capAmount);
    ResponseEntity<?> deleteAll(String apiKey);
    ResponseEntity<?> applyGeneralToAll(BigDecimal amount, String productType,BigDecimal capAmount, PriceCategory priceType);
    ResponseEntity<?> syncWalletUser(String apiKey);
    ResponseEntity<List<BillerManagementResponse>> syncBillers(String apiKey) throws CustomException;
    ResponseEntity<List<UserPricing>> distinctSearch();
    ResponseEntity<List<UserPricing>> search(String fullName);
    ResponseEntity<?> createProducts(String code, String name, String description);
    ResponseEntity<?> editProducts(Long id, String code, String name, String description);
    ResponseEntity<?> getAllProducts();
    //
}
