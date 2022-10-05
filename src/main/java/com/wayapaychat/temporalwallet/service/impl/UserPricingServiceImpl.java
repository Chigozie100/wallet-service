package com.wayapaychat.temporalwallet.service.impl;

import com.wayapaychat.temporalwallet.entity.UserPricing;
import com.wayapaychat.temporalwallet.entity.WalletEventCharges;
import com.wayapaychat.temporalwallet.entity.WalletUser;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.repository.UserPricingRepository;
import com.wayapaychat.temporalwallet.repository.WalletEventRepository;
import com.wayapaychat.temporalwallet.repository.WalletUserRepository;
import com.wayapaychat.temporalwallet.service.UserPricingService;
import static com.wayapaychat.temporalwallet.util.Constant.*;
import com.wayapaychat.temporalwallet.util.SuccessResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class UserPricingServiceImpl implements UserPricingService {


    private final UserPricingRepository userPricingRepository;
    private final WalletUserRepository walletUserRepository;
    private final WalletEventRepository walletEventRepository;


    @Autowired
    public UserPricingServiceImpl(UserPricingRepository userPricingRepository, WalletUserRepository walletUserRepository, WalletEventRepository walletEventRepository) {
        this.userPricingRepository = userPricingRepository;
        this.walletUserRepository = walletUserRepository;
        this.walletEventRepository = walletEventRepository;
    }


    @Override
    public ResponseEntity<?> create(Long userId, BigDecimal amount, String product) {
        System.out.println("#################### INSIDE HERE #############33");
        try{
          Optional<UserPricing> userPricingOptional = userPricingRepository.findDetails(userId,product);
          if(userPricingOptional.isEmpty()){
              System.out.println("#################### INSIDE HERE #############33");
              UserPricing userPricing = new UserPricing();
              userPricing.setGeneralAmount(amount);
              userPricing.setUserId(userId);
              userPricing.setCustomAmount(BigDecimal.valueOf(0.00));
              userPricing.setDiscount(BigDecimal.valueOf(0.00));
              userPricing.setCapPrice(BigDecimal.valueOf(0.00));
              userPricing.setCreatedAt(new Date());
              userPricing.setProduct(product);
              return new ResponseEntity<>(new SuccessResponse(ADD_PRICE, userPricingRepository.save(userPricing)), HttpStatus.OK);

          }
        } catch (Exception e) {
            log.error("UNABLE TO CREATE: {}", e.getMessage());
            throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
        }
        return null;
    }

    @Override
    public ResponseEntity<?> update(Long userId, BigDecimal discountAmount, BigDecimal customAmount, BigDecimal capAmount, String product) {
        try{
            Optional<UserPricing> userPricingOptional = userPricingRepository.findDetails(userId,product);
            if(userPricingOptional.isEmpty()){
                throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
            }
            UserPricing userPricing = userPricingOptional.get();
            userPricing.setUserId(userId);
            userPricing.setCustomAmount(customAmount);
            userPricing.setDiscount(discountAmount);
            userPricing.setCapPrice(capAmount);
            userPricing.setUpdatedAt(new Date());
            return new ResponseEntity<>(new SuccessResponse(UPDATE_PRICE, userPricingRepository.save(userPricing)), HttpStatus.OK);
        } catch (Exception e) {
            log.error("UNABLE TO CREATE: {}", e.getMessage());
            throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
        }
    }

    @Override
    public ResponseEntity<?> getAllUserPricing(int page, int size) {

        try{
            Pageable pageable = PageRequest.of(page,size);

            Page<UserPricing> userPricingPage = userPricingRepository.findAll(pageable);
            List<UserPricing> userPricingList = userPricingPage.getContent();

            Map<String, Object> response = new HashMap<>();
            response.put("userPricingList", userPricingList);
            response.put("currentPage", userPricingPage.getNumber());
            response.put("totalItems", userPricingPage.getTotalElements());
            response.put("totalPages", userPricingPage.getTotalPages());

            return new ResponseEntity<>(new SuccessResponse(SUCCESS_MESSAGE, response), HttpStatus.OK);
        } catch (Exception e) {
            log.error("UNABLE TO GET: {}", e.getMessage());
            throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
        }
    }

    @Override
    public UserPricing getUserPricing(Long userId) {
        try{
            Optional<UserPricing> userPricing =  userPricingRepository.findById(userId);
            if (userPricing.isEmpty()){
                throw new CustomException("Not Found", HttpStatus.NOT_FOUND);
            }
            return userPricing.get();
        }catch (Exception ex){
            throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
        }
    }


    private void doSync(){
        try{

        List<WalletUser> userList = walletUserRepository.findAll();

        List<WalletEventCharges> walletEventCharges = getWalletEventCharges();
        for (WalletUser list: userList){
            for(WalletEventCharges obj: walletEventCharges){
                if(list.isCorporate() || !list.isCorporate()){
                    log.info("Corporate uses only");
                    create(list.getUserId(), obj.getTranAmt(), obj.getEventId());
                }
            }
        }
        }catch (Exception ex){
            throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
        }
        log.info(" ####### User and Changes Sync thread completed ###### ");
    }


    public ResponseEntity<?> syncWalletUser(){
        System.out.println("Inside syncWalletUser ####");
        try{
            CompletableFuture.runAsync(this::doSync);
            return new ResponseEntity<>(new SuccessResponse(USER_PRICE_SYNC, null), HttpStatus.OK);
        } catch (Exception e) {
            log.error("UNABLE TO CREATE: {}", e.getMessage());
            throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
        }
    }

    private List<WalletEventCharges> getWalletEventCharges(){

        try{
           return walletEventRepository.findAll();
        }catch (Exception ex){
            throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
        }
    }




}
