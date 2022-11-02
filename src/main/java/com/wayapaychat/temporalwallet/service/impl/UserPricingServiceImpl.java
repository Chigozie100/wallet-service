package com.wayapaychat.temporalwallet.service.impl;

import com.wayapaychat.temporalwallet.dto.BillerManagementResponse;
import com.wayapaychat.temporalwallet.entity.Billers;
import com.wayapaychat.temporalwallet.entity.UserPricing;
import com.wayapaychat.temporalwallet.entity.WalletEventCharges;
import com.wayapaychat.temporalwallet.entity.WalletUser;
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
    public ResponseEntity<?> create(Long userId, String fullName, BigDecimal amount, String product) {
        System.out.println("#################### INSIDE HERE #############33");
        try{
          Optional<UserPricing> userPricingOptional = userPricingRepository.findDetails(userId,product);
          if(userPricingOptional.isEmpty()){
              System.out.println("#################### INSIDE HERE #############33");
              UserPricing userPricing = new UserPricing();
              userPricing.setGeneralAmount(amount);
              userPricing.setUserId(userId);
              userPricing.setFullName(fullName);
              userPricing.setCustomAmount(BigDecimal.valueOf(0.00));
              userPricing.setDiscount(BigDecimal.valueOf(0.00));
              userPricing.setCapPrice(BigDecimal.valueOf(0.00));
              userPricing.setCreatedAt(new Date());
              userPricing.setProduct(product);
              userPricing.setStatus(ProductPriceStatus.GENERAL);
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
            userPricing.setStatus(ProductPriceStatus.CUSTOM);
            return new ResponseEntity<>(new SuccessResponse(UPDATE_PRICE, userPricingRepository.save(userPricing)), HttpStatus.OK);
        } catch (Exception e) {
            log.error("UNABLE TO CREATE: {}", e.getMessage());
            throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
        }
    }

    @Override
    public ResponseEntity<?> updateCustomProduct(BigDecimal capAmount, BigDecimal discountAmount, BigDecimal generalAmount, String product) {
        CompletableFuture.runAsync(() -> processCustomProduct(capAmount, discountAmount, generalAmount, product));
        return new ResponseEntity<>(new SuccessResponse(INPROGRESS, null), HttpStatus.OK);
    }

    private void processCustomProduct(BigDecimal capAmount, BigDecimal discountAmount, BigDecimal generalAmount, String product){
        try{
            List<UserPricing> userPricingList = userPricingRepository.findByProduct(product);

            for(UserPricing data: userPricingList){
                UserPricing userPricing = userPricingRepository.findById(data.getId()).orElse(null);
                Objects.requireNonNull(userPricing).setGeneralAmount(generalAmount);
                userPricing.setCapPrice(capAmount);
                userPricing.setDiscount(discountAmount);
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

    private void doApplyCapToAll(BigDecimal discountAmount){
        List<UserPricing> userPricingList = userPricingRepository.findAll();
        for (UserPricing data: userPricingList){
            data.setCapPrice(discountAmount);
            userPricingRepository.save(data);
        }
    }

    public ResponseEntity<?> applyGeneralToAll(BigDecimal amount) {
        try{
            CompletableFuture.runAsync(()-> doApplyGeneralToAll(amount));
            return new ResponseEntity<>(new SuccessResponse(APPLY_GENERAL, null), HttpStatus.OK);
        } catch (Exception e) {
            log.error("UNABLE TO APPLY_CAP: {}", e.getMessage());
            throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
        }
    }

    private void doApplyGeneralToAll(BigDecimal amount){
        List<UserPricing> userPricingList = userPricingRepository.findAll();
        for (UserPricing data: userPricingList){
            data.setGeneralAmount(amount);
            userPricingRepository.save(data);
        }
    }


    private void doSync(){
        try{

        List<WalletUser> userList = walletUserRepository.findAll();

//        BillsUtil billsUtils = new BillsUtil();
//        List<BillsUtil> airtimeList = billsUtils.getAirtimeList();
//        List<BillsUtil> cableTV = billsUtils.getCableTV();
//        List<BillsUtil> dataList = billsUtils.getDataList();
//        List<BillsUtil> utility = billsUtils.getUtility();

        List<WalletEventCharges> walletEventCharges = getWalletEventCharges();
        for (WalletUser list: userList){
            for(WalletEventCharges obj: walletEventCharges){
                if(list.isCorporate() || !list.isCorporate()){
                    log.info("Corporate uses only");
                    create(list.getUserId(), list.getFirstName() +" " +list.getLastName(), obj.getTranAmt(), obj.getEventId());
                }
            }
//            for (BillsUtil airtime: airtimeList){
//                create(list.getUserId(), BigDecimal.valueOf(0.00), airtime.getBiller());
//            }
//            for (BillsUtil cable: cableTV){
//                create(list.getUserId(), BigDecimal.valueOf(0.00), cable.getBiller());
//            }
//            for (BillsUtil datalist: dataList){
//                create(list.getUserId(), BigDecimal.valueOf(0.00), datalist.getBiller());
//            }
//            for (BillsUtil utilityList: utility){
//                create(list.getUserId(), BigDecimal.valueOf(0.00), utilityList.getBiller());
//            }
        }
        }catch (Exception ex){
            throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
        }
        log.info(" ####### User and Changes Sync thread completed ###### ");
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
