package com.wayapaychat.temporalwallet.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayapaychat.temporalwallet.dto.AccountDetailDTO;
import com.wayapaychat.temporalwallet.dto.WalletUserDTO;
import com.wayapaychat.temporalwallet.entity.VirtualAccountSettings;
import com.wayapaychat.temporalwallet.entity.VirtualAccountTransactions;
import com.wayapaychat.temporalwallet.entity.WalletAccount;
import com.wayapaychat.temporalwallet.entity.WalletUser;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.pojo.VirtualAccountHookRequest;
import com.wayapaychat.temporalwallet.pojo.VirtualAccountRequest;
import com.wayapaychat.temporalwallet.repository.VirtualAccountRepository;
import com.wayapaychat.temporalwallet.repository.VirtualAccountSettingRepository;
import com.wayapaychat.temporalwallet.repository.WalletAccountRepository;
import com.wayapaychat.temporalwallet.service.UserAccountService;
import com.wayapaychat.temporalwallet.service.VirtualService;
import com.wayapaychat.temporalwallet.util.ReqIPUtils;
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
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Slf4j
public class VirtualServiceImpl implements VirtualService {

    private final UserAccountService userAccountService;
    private final WalletAccountRepository walletAccountRepository;
    private final VirtualAccountRepository virtualAccountRepository;

    private final VirtualAccountSettingRepository virtualAccountSettingRepository;

    private final Util utils;

    @Autowired
    public VirtualServiceImpl(UserAccountService userAccountService, WalletAccountRepository walletAccountRepository, VirtualAccountRepository virtualAccountRepository, VirtualAccountSettingRepository virtualAccountSettingRepository, Util utils) {
        this.userAccountService = userAccountService;
        this.walletAccountRepository = walletAccountRepository;
        this.virtualAccountRepository = virtualAccountRepository;
        this.virtualAccountSettingRepository = virtualAccountSettingRepository;
        this.utils = utils;
    }


    @Override
    public ResponseEntity<SuccessResponse> registerWebhookUrl(VirtualAccountHookRequest request) {
        try {
           Optional<VirtualAccountSettings> optional = virtualAccountSettingRepository.findByAccountNoORCallbackUrl(request.getAccountNo(), request.getCallbackUrl());

           if(optional.isPresent()){
               throw new CustomException("Webhook already created for this merchant ", new Throwable(), HttpStatus.EXPECTATION_FAILED);
           }
            VirtualAccountSettings virtualAccountHook = new VirtualAccountSettings();
            virtualAccountHook.setVirtualAccountCode(utils.generateRandomNumber(4));
            virtualAccountHook.setEmail(request.getEmail());
            virtualAccountHook.setBusinessId(request.getBusinessId());
            virtualAccountHook.setCallbackUrl(request.getCallbackUrl());
            virtualAccountHook.setAccountNo(request.getAccountNo());
            virtualAccountSettingRepository.save(virtualAccountHook);
            log.info("Webhook URL registered successfully.");
            return new ResponseEntity<>(new SuccessResponse("VirtualAccountHook created successfully", virtualAccountHook), HttpStatus.OK);

        } catch (Exception ex) {
            log.error("Error registering webhook URL: {}", ex.getMessage());
            throw new CustomException("Error " + ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }


    @Override
    public ResponseEntity<SuccessResponse> createVirtualAccount(VirtualAccountRequest account) {
        try {
            log.info("Creating virtual account...");
            WalletUserDTO walletUserDTO = getUserWalletData(account);
            WalletAccount walletAccount = userAccountService.createNubanAccount(walletUserDTO);
            AccountDetailDTO accountDetailDTO = new AccountDetailDTO();
            if (walletAccount != null) {
                accountDetailDTO = getResponse(walletAccount.getNubanAccountNo());
            }
            log.info("Virtual account created successfully.");
            return new ResponseEntity<>(new SuccessResponse("Account created successfully", accountDetailDTO), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Error creating virtual account: {}", ex.getMessage());
            throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public ResponseEntity<SuccessResponse> createVirtualAccountVersion2(VirtualAccountRequest account) {
        try {
            // get wallet details by account number
            // create a sub account for this user
            // get the WalletUser
            log.info("Creating virtual account...", account);
            WalletUser businessObj = new WalletUser();

            // Get Wayagram Wallet
            WalletAccount wayaGramAcctNo = getWayaGrammAccount(account);

            log.info("WAYAGRAM User ID {} ", Objects.requireNonNull(wayaGramAcctNo).getUser().getId());
            if(wayaGramAcctNo == null){
                throw new CustomException("provide WayaGrammAccount", HttpStatus.EXPECTATION_FAILED);
            }
            WalletUser wayaGramUser = getUserWalletById(wayaGramAcctNo.getUser().getId());

            // create a sub account under the wayagram user
            log.info("wayaGramUser {}", wayaGramUser);

            WalletAccount walletAccount = userAccountService.createNubanAccountVersion2(wayaGramUser, businessObj, account);
            AccountDetailDTO accountDetailDTO = new AccountDetailDTO();
            if (walletAccount != null) {
                accountDetailDTO = getResponse(walletAccount.getNubanAccountNo());
            }

            log.info("Virtual account created successfully.", accountDetailDTO);
            return new ResponseEntity<>(new SuccessResponse("Virtual Account created successfully", accountDetailDTO), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Error creating virtual account: {}", ex.getMessage());
            System.out.println("ex-->" + ex.getMessage());
            throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    @Override
    public ResponseEntity<SuccessResponse> searchVirtualTransactions(LocalDateTime fromdate,LocalDateTime todate, String accountNo, int page, int size) {
        // with pagination

        Pageable pagable = PageRequest.of(page, size);
        Map<String, Object> response = new HashMap<>();


        Page<VirtualAccountTransactions> virtualAccountTransactionsPage = virtualAccountRepository.findAllByDateRange(fromdate,todate,accountNo, pagable);
        List<VirtualAccountTransactions> transaction = virtualAccountTransactionsPage.getContent();
        response.put("transaction", transaction);
        response.put("currentPage", virtualAccountTransactionsPage.getNumber());
        response.put("totalItems", virtualAccountTransactionsPage.getTotalElements());
        response.put("totalPages", virtualAccountTransactionsPage.getTotalPages());
        return new ResponseEntity<>(new SuccessResponse("Virtual Account Transactions", response), HttpStatus.OK);

    }





    private AccountDetailDTO getResponse(String accountNo){
        WalletAccount acct = walletAccountRepository.findByAccountNoOrNubanAccountNo(accountNo);
        System.out.println("Account ==" + acct);
        if (acct == null) {
            return null;
        }
        return new AccountDetailDTO(acct.getId(), acct.getSol_id(), acct.getAccountNo(),
                acct.getAcct_name(), acct.getNubanAccountNo(), BigDecimal.valueOf(acct.getClr_bal_amt()));
    }


    private WalletUserDTO getUserWalletData(VirtualAccountRequest account){
//        Util util = new Util();

        WalletUserDTO walletUserDTO = new WalletUserDTO();
        walletUserDTO.setUserId(Long.parseLong(account.getUserId()));
        walletUserDTO.setAccountType("savings");
        walletUserDTO.setCustDebitLimit(new BigDecimal("50000.00").doubleValue());
        LocalDateTime time = LocalDateTime.of(2099, Month.DECEMBER, 30, 0, 0);
        ZonedDateTime zdt = time.atZone(ZoneId.systemDefault());
        Date output = Date.from(zdt.toInstant());
        walletUserDTO.setCustExpIssueDate(output);
        walletUserDTO.setCustIssueId(utils.generateRandomNumber(9));
        walletUserDTO.setCustSex("MALE");
        walletUserDTO.setCustTitleCode("MR");
        walletUserDTO.setDob(new Date());
        String[] keyCredit = account.getAccountName().split(Pattern.quote(" "));
        walletUserDTO.setEmailId(keyCredit[0] + "@gmail.com");
        walletUserDTO.setFirstName(keyCredit[0]);
        walletUserDTO.setLastName(keyCredit[1]);
        walletUserDTO.setMobileNo("234");
        walletUserDTO.setSolId("0000");
        log.info("Wallet user --->>> {}", walletUserDTO);

        return walletUserDTO;

    }

    @Override
    public AccountDetailDTO nameEnquiry(String accountNumber) {
        return getResponse(accountNumber);

    }

    @Override
    public SuccessResponse balanceEnquiry(String accountNumber) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString;

        try {
            log.info("Performing balance enquiry...");
            WalletAccount walletAccount = walletAccountRepository.findByNubanAccountNo(accountNumber);
            AccountDetailDTO account = new AccountDetailDTO(walletAccount.getId(), walletAccount.getSol_id(), walletAccount.getNubanAccountNo(),
                    walletAccount.getAcct_name(), BigDecimal.valueOf(walletAccount.getClr_bal_amt()),
                    walletAccount.getAcct_crncy_code());

            jsonString = mapper.writeValueAsString(account);
            log.info("Account details retrieved successfully: {}", jsonString);

            String encryptedString = ReqIPUtils.encrypt(jsonString);
            log.info("Encrypted account details: {}", encryptedString);

            String decryptedString = String.valueOf(decryptString(encryptedString));
            log.info("Decrypted account details: {}", decryptedString);

            return new SuccessResponse("Data retrieved successfully", encryptedString);
        } catch (Exception ex) {
            log.error("Error performing balance enquiry: {}", ex.getMessage());
            throw new CustomException("Error " + ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public SuccessResponse decryptString(String obj) {
        //String reference, String amount, String narration, String crAccountName, String bankName, String drAccountName, String crAccount, String bankCode
        try{

            String str = ReqIPUtils.decrypt(obj);
            return new SuccessResponse("Data retrieved successfully", str);
        }catch (Exception ex){
            throw new CustomException("Error " +ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }



    public String encode(String password) {
        try {
            return Util.WayaEncrypt(password);
        }catch (Exception ex){
            throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }


    public String getAuthCredentials(String username, String password) {
        try {
            String credentials = Util.WayaEncrypt(username + "." + password);
            return "Basic "+credentials;
        }catch (Exception ex){
            throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }

    }

//    public boolean validateBasicAuth(String token) throws Exception {
//        final String credentials = Util.WayaDecrypt(token);
//        String[] keyDebit = credentials.split(Pattern.quote(" "));
//        Optional<VirtualAccountHook> virtualAccountHook = virtualAccountRepository.findByUsernameAndPassword(keyDebit[0],keyDebit[1]);
//        return virtualAccountHook.filter(accountHook -> (keyDebit[0].equals(accountHook.getUsername())) && (keyDebit[1].equals(accountHook.getPassword()))).isPresent();
//    }

    private WalletUser getUserWallet(VirtualAccountRequest account){
        try {
            return userAccountService.findUserWalletByEmailAndPhone(account.getEmail());
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    private WalletUser getUserWalletById(Long id){
        try {
            return userAccountService.findById(id);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    private WalletAccount getWayaGrammAccount(VirtualAccountRequest account){
        try {
            return userAccountService.findUserAccount(account.getWayaGramAccountNumber());
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
}