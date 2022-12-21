package com.wayapaychat.temporalwallet.service.impl;

import com.wayapaychat.temporalwallet.dto.BillerManagementResponse;
import com.wayapaychat.temporalwallet.entity.Billers;
import com.wayapaychat.temporalwallet.entity.UserPricing;
import com.wayapaychat.temporalwallet.entity.WalletEventCharges;
import com.wayapaychat.temporalwallet.entity.WalletUser;
import com.wayapaychat.temporalwallet.enumm.PriceCategory;
import com.wayapaychat.temporalwallet.enumm.ProductPriceStatus;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.notification.ResponseObj;
import com.wayapaychat.temporalwallet.proxy.BillsPayProxy;
import com.wayapaychat.temporalwallet.repository.BillersRepository;
import com.wayapaychat.temporalwallet.repository.UserPricingRepository;
import com.wayapaychat.temporalwallet.repository.WalletEventRepository;
import com.wayapaychat.temporalwallet.repository.WalletUserRepository;
import com.wayapaychat.temporalwallet.service.UserPricingService;
import static com.wayapaychat.temporalwallet.util.Constant.*;

import com.wayapaychat.temporalwallet.util.ErrorResponse;
import com.wayapaychat.temporalwallet.util.SuccessResponse;
import com.wayapaychat.temporalwallet.util.Util;
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
    private final BillsPayProxy billsPayProxy;
    private final BillersRepository billersRepository;


    @Autowired
    public UserPricingServiceImpl(UserPricingRepository userPricingRepository, WalletUserRepository walletUserRepository, WalletEventRepository walletEventRepository, BillsPayProxy billsPayProxy, BillersRepository billersRepository) {
        this.userPricingRepository = userPricingRepository;
        this.walletUserRepository = walletUserRepository;
        this.walletEventRepository = walletEventRepository;
        this.billsPayProxy = billsPayProxy;
        this.billersRepository = billersRepository;
    }


    @Override
    public ResponseEntity<?> create(Long userId, String fullName, BigDecimal amount, String product, String code) {
        System.out.println("#################### INSIDE HERE #############1 ");
        try{
          Optional<UserPricing> userPricingOptional = userPricingRepository.findDetails(userId,product);
          if(userPricingOptional.isEmpty()){
              System.out.println("#################### INSIDE HERE ############# 2");
              UserPricing userPricing = new UserPricing();
              userPricing.setGeneralAmount(amount);
              userPricing.setUserId(userId);
              userPricing.setFullName(fullName);
              userPricing.setCustomAmount(BigDecimal.valueOf(0.00));
              userPricing.setDiscount(BigDecimal.valueOf(0.00));
              userPricing.setCapPrice(BigDecimal.valueOf(200.00));
              userPricing.setCreatedAt(new Date());
              userPricing.setProduct(product);
              userPricing.setStatus(ProductPriceStatus.GENERAL);
              userPricing.setCode(code);
              userPricing.setPriceType(PriceCategory.FIXED);
              return new ResponseEntity<>(new SuccessResponse(ADD_PRICE, userPricingRepository.save(userPricing)), HttpStatus.OK);

          }
        } catch (Exception e) {
            log.error("UNABLE TO CREATE: {}", e.getMessage());
            throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
        }
        return null;
    }

    @Override
    public ResponseEntity<?> createUserPricing(String userId) {
        WalletUser existingUser = walletUserRepository.findByUserId(Long.parseLong(userId));
        if (existingUser != null) {
            return new ResponseEntity<>(new ErrorResponse("Wallet User already exists"), HttpStatus.BAD_REQUEST);
        }
        ResponseEntity<?> responseEntity = createUserPricing(existingUser);
        log.info("responseEntity" +responseEntity);
        return new ResponseEntity<>(new SuccessResponse(ADD_PRICE, null), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> createUserPricing(WalletUser userx) {
        processUserPricing(new ArrayList<>(), "createUserPricing", userx);
        return new ResponseEntity<>(new SuccessResponse(ADD_PRICE, null), HttpStatus.OK);

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
            userPricing.setStatus(ProductPriceStatus.CUSTOM);
            return new ResponseEntity<>(new SuccessResponse(UPDATE_PRICE, userPricingRepository.save(userPricing)), HttpStatus.OK);
        } catch (Exception e) {
            log.error("UNABLE TO CREATE: {}", e.getMessage());
            throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
        }
    }

    @Override
    public ResponseEntity<?> updateCustomProduct(BigDecimal capAmount, BigDecimal discountAmount, BigDecimal generalAmount, String product, String priceType) {
        CompletableFuture.runAsync(() -> processCustomProduct(capAmount, discountAmount, generalAmount, product, priceType));
        return new ResponseEntity<>(new SuccessResponse(INPROGRESS, null), HttpStatus.OK);
    }

    private void processCustomProduct(BigDecimal capAmount, BigDecimal discountAmount, BigDecimal generalAmount, String product, String priceType){
        try{
            List<UserPricing> userPricingList = userPricingRepository.findByProduct(product);

            for(UserPricing data: userPricingList){
                UserPricing userPricing = userPricingRepository.findById(data.getId()).orElse(null);
                Objects.requireNonNull(userPricing).setGeneralAmount(generalAmount);
                userPricing.setCapPrice(capAmount);
                userPricing.setDiscount(discountAmount);
                userPricing.setPriceType(PriceCategory.valueOf(priceType));
                userPricingRepository.save(userPricing);
            }
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
    public ResponseEntity<?> getAllUserPricingUserId(String userId, String product) {
        try{
        Optional<UserPricing> userPricingOptional = userPricingRepository.findDetails(Long.parseLong(userId),product);
        if(userPricingOptional.isPresent()){
            return new ResponseEntity<>(new SuccessResponse(SUCCESS_MESSAGE, userPricingOptional.get()), HttpStatus.OK);
        }
        } catch (CustomException e) {
            log.error("UNABLE TO APPLY_DISCOUNT: {}", e.getMessage());
            throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
        }
       return null;
    }

    @Override
    public ResponseEntity<?> applyDiscountToAll(BigDecimal discountAmount) {

        try{
            CompletableFuture.runAsync(()-> doApplyDiscountToAll(discountAmount));
            return new ResponseEntity<>(new SuccessResponse(APPLY_DISCOUNT, null), HttpStatus.OK);
        } catch (Exception e) {
            log.error("UNABLE TO APPLY_DISCOUNT: {}", e.getMessage());
            throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
        }
    }

   private void doApplyDiscountToAll(BigDecimal discountAmount){
        List<UserPricing> userPricingList = userPricingRepository.findAll();
        for (UserPricing data: userPricingList){
            data.setDiscount(discountAmount);
            userPricingRepository.save(data);
        }
    }

    @Override
    public ResponseEntity<?> applyCapToAll(BigDecimal capAmount) {
        try{
            CompletableFuture.runAsync(()-> doApplyCapToAll(capAmount));
            return new ResponseEntity<>(new SuccessResponse(APPLY_CAP, null), HttpStatus.OK);
        } catch (Exception e) {
            log.error("UNABLE TO APPLY_CAP: {}", e.getMessage());
            throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
        }
    }

    @Override
    public ResponseEntity<?> deleteAll(String apiKey) {
        if(!apiKey.equals("WAL3890811")){
            throw new CustomException("Invalid authKey", HttpStatus.EXPECTATION_FAILED);
        }
        CompletableFuture.runAsync(this::doDelete);
        return new ResponseEntity<>(new SuccessResponse("Deleting in progress ...", null), HttpStatus.OK);

    }

    private void doDelete(){
        try{
            userPricingRepository.deleteAllInBatch();
        }catch (Exception ex){
            throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
        }
    }

    private void doApplyCapToAll(BigDecimal discountAmount){
        List<UserPricing> userPricingList = userPricingRepository.findAll();
        for (UserPricing data: userPricingList){
            data.setCapPrice(discountAmount);
            userPricingRepository.save(data);
        }
    }

    public ResponseEntity<?> applyGeneralToAll(BigDecimal amount, String productType,BigDecimal capAmount, PriceCategory priceType) {
        try{
            CompletableFuture.runAsync(()-> doApplyGeneralToAll(amount, productType, capAmount, priceType));
            return new ResponseEntity<>(new SuccessResponse(APPLY_GENERAL, null), HttpStatus.OK);
        } catch (Exception e) {
            log.error("UNABLE TO APPLY_CAP: {}", e.getMessage());
            throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
        }
    }

    private void doApplyGeneralToAll(BigDecimal amount, String productType,BigDecimal capAmount, PriceCategory priceType){
        List<UserPricing> userPricingList = userPricingRepository.getAllDetailsByCode(productType);
        for (UserPricing data: userPricingList){
            data.setGeneralAmount(amount);
             data.setCapPrice(capAmount);
             data.setPriceType(priceType);
            userPricingRepository.save(data);
        }
    }


    private void doSync(){
        try{
            List<WalletUser> userList = walletUserRepository.findAll();
            userPricingRepository.deleteAll();
            processUserPricing(userList,"doSync",null);
        }catch (Exception ex){
            throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
        }
        log.info(" ####### User and Changes Sync thread completed ###### ");
    }


    private void processUserPricing(List<WalletUser> userList, String actionType, WalletUser userx){
        try{

        List<Map<String, String>> products = Util.products();
        if(userList.isEmpty()){
            if(userx !=null){
                for (Map<String, String> tmpData : products) {
                    Set<String> key = tmpData.keySet();
                    Iterator it = key.iterator();
                    while (it.hasNext()) {
                        String hmKey = (String) it.next();
                        String hmData = tmpData.get(hmKey);
                        log.info("Key: " + hmKey + " & Data: " + hmData);
                        create(userx.getUserId(), userx.getFirstName() + " " + userx.getLastName(), BigDecimal.valueOf(10.00), hmKey, hmData);
                    }

                }
            }

        }else{

            for (WalletUser list: userList){
                for (Map<String, String> tmpData : products) {
                    Set<String> key = tmpData.keySet();
                    Iterator it = key.iterator();
                    while (it.hasNext()) {
                        String hmKey = (String) it.next();
                        String hmData = tmpData.get(hmKey);
                        if (list.isCorporate() || !list.isCorporate()) {
                            log.info("Corporate uses only");
                            if(actionType.equals("doSync")){
                                create(list.getUserId(), list.getFirstName() + " " + list.getLastName(), BigDecimal.valueOf(10.00), hmKey, hmData);
                            }
                        }
                        log.info("Key: " + hmKey + " & Data: " + hmData);
                        // it.remove(); // avoids a ConcurrentModificationException
                    }
                }
            }
        }

        }catch (Exception ex){
            throw new CustomException("Error in processUserPricing" + ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }


    public ResponseEntity<?> syncWalletUser(String apiKey){
        log.info("Inside syncWalletUser ####");
        try{
            if(!apiKey.equals("WAL3890811")){
                throw new CustomException("Invalid authKey", HttpStatus.EXPECTATION_FAILED);
            }

            CompletableFuture.runAsync(this::doSync);
            return new ResponseEntity<>(new SuccessResponse(USER_PRICE_SYNC, null), HttpStatus.OK);
        } catch (Exception e) {
            log.error("UNABLE TO CREATE: {}", e.getMessage());
            throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
        }
    }


    public ResponseEntity<List<BillerManagementResponse>> syncBillers(String apiKey) throws CustomException {
        if(!apiKey.equals("WAL3890811")){
            throw new CustomException("Invalid authKey",HttpStatus.EXPECTATION_FAILED);
        }
        ResponseEntity<ResponseObj<List<BillerManagementResponse>>> responseEntity = billsPayProxy.getBillers();
        ResponseObj<List<BillerManagementResponse>> responseHelper = responseEntity.getBody();

        List<BillerManagementResponse> response = Objects.requireNonNull(responseHelper).data;

        for (BillerManagementResponse billerManagementResponse : response) {
            saveBillers(billerManagementResponse);
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private void saveBillers(BillerManagementResponse response) throws CustomException {
        Billers billers = new Billers();
        try{
            Optional<Billers> billers1 = billersRepository.findDetails(response.getBillerAggregatorCode(),response.getCategoryName());
            if(billers1.isEmpty()){
                billers.setActive(response.isActive());
                billers.setAggregatorName(response.getAggregatorName());
                billers.setBillerAggregatorCode(response.getBillerAggregatorCode());
                billers.setBillerWayaPayCode(response.getBillerWayaPayCode());
                billers.setCategoryId(response.getCategoryId());
                billers.setCategoryName(response.getCategoryName());
                billers.setName(response.getName());
                billers.setCategoryCode(response.getCategoryCode());
                billersRepository.save(billers);
            }
        }catch (Exception ex){
            throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
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