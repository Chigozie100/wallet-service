package com.wayapaychat.temporalwallet.service.impl;

import com.wayapaychat.temporalwallet.dto.AccountDetailDTO;
import com.wayapaychat.temporalwallet.dto.BankPaymentDTO;
import com.wayapaychat.temporalwallet.dto.WalletUserDTO;
import com.wayapaychat.temporalwallet.entity.VirtualAccountHook;
import com.wayapaychat.temporalwallet.entity.WalletAccount;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.pojo.AppendToVirtualAccount;
import com.wayapaychat.temporalwallet.pojo.VirtualAccountHookRequest;
import com.wayapaychat.temporalwallet.pojo.VirtualAccountRequest;
import com.wayapaychat.temporalwallet.repository.VirtualAccountRepository;
import com.wayapaychat.temporalwallet.repository.WalletAccountRepository;
import com.wayapaychat.temporalwallet.service.UserAccountService;
import com.wayapaychat.temporalwallet.service.VirtualService;
import com.wayapaychat.temporalwallet.util.SuccessResponse;
import com.wayapaychat.temporalwallet.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class VirtualServiceImpl implements VirtualService {

    private final UserAccountService userAccountService;
    private final WalletAccountRepository walletAccountRepository;
    private final VirtualAccountRepository virtualAccountRepository;

    @Autowired
    public VirtualServiceImpl(UserAccountService userAccountService, WalletAccountRepository walletAccountRepository, VirtualAccountRepository virtualAccountRepository) {
        this.userAccountService = userAccountService;
        this.walletAccountRepository = walletAccountRepository;
        this.virtualAccountRepository = virtualAccountRepository;
    }


    @Override
    public ResponseEntity<?> registerWebhookUrl(VirtualAccountHookRequest request) {
        try{

            VirtualAccountHook virtualAccountHook = new VirtualAccountHook();
            virtualAccountHook.setBank(request.getBank());
            virtualAccountHook.setBankCode(request.getBankCode());
            virtualAccountHook.setVirtualAccountCode(Util.generateRandomNumber(4));
            virtualAccountHook.setUsername(request.getUsername());
            virtualAccountHook.setPassword(encode(request.getPassword()));
            virtualAccountHook.setCallbackUrl(request.getCallbackUrl());
            virtualAccountRepository.save(virtualAccountHook);
            return new ResponseEntity<>(new SuccessResponse("Account created successfully", virtualAccountHook), HttpStatus.OK);

        }catch (Exception ex){
            throw new CustomException("Error " +ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    @Override
    public void transactionWebhookData() {

    }

    @Override
    public ResponseEntity<SuccessResponse> createVirtualAccount(VirtualAccountRequest account) {
        /**
         * get request from Aggregator
         * build
         */
        WalletUserDTO walletUserDTO = getUserWalletData(account);
        // send request to create account
        WalletAccount walletAccount = userAccountService.createNubanAccount(walletUserDTO);
        AccountDetailDTO accountDetailDTO = new AccountDetailDTO();
        if(walletAccount !=null){
            accountDetailDTO  = getResponse(walletAccount.getNubanAccountNo());
        }
        return new ResponseEntity<>(new SuccessResponse("Account created successfully", accountDetailDTO), HttpStatus.OK);
    }

    private AccountDetailDTO getResponse(String accountNo){
        WalletAccount acct = walletAccountRepository.findByNubanAccountNo(accountNo);
        if (acct == null) {
            return null;
        }
        return new AccountDetailDTO(acct.getId(), acct.getSol_id(), acct.getNubanAccountNo(),
                acct.getAcct_name(), acct.getProduct_code(), new BigDecimal(acct.getClr_bal_amt()),
                acct.getAcct_crncy_code());
    }


    private WalletUserDTO getUserWalletData(VirtualAccountRequest account){
        WalletUserDTO walletUserDTO = new WalletUserDTO();
        walletUserDTO.setUserId(Long.parseLong(account.getUserId()));
        walletUserDTO.setAccountType("savings");
        walletUserDTO.setCustDebitLimit(new BigDecimal("50000.00").doubleValue());
        LocalDateTime time = LocalDateTime.of(2099, Month.DECEMBER, 30, 0, 0);
        ZonedDateTime zdt = time.atZone(ZoneId.systemDefault());
        Date output = Date.from(zdt.toInstant());
        walletUserDTO.setCustExpIssueDate(output);
        walletUserDTO.setCustIssueId(Util.generateRandomNumber(9));
        walletUserDTO.setCustSex("MALE");
        walletUserDTO.setCustTitleCode("MR");
        walletUserDTO.setDob(new Date());
        String[] keyCredit = account.getAccountName().split(Pattern.quote(" "));
        walletUserDTO.setEmailId(keyCredit[0] + "@gmail.com");
        walletUserDTO.setFirstName(keyCredit[0]);
        walletUserDTO.setLastName(keyCredit[1]);
        walletUserDTO.setMobileNo("234");
        walletUserDTO.setSolId("0000");

        return walletUserDTO;

    }

    @Override
    public void appendNameToVirtualAccount(AppendToVirtualAccount account) {

    }

    @Override
    public SuccessResponse accountTransactionQuery(String accountNumber, LocalDate startDate, LocalDate endDate) {

        return new SuccessResponse("Data retrieved successfully", null);

    }

    @Override
    public SuccessResponse nameEnquiry(String accountNumber, String bankCode) {
        return null;
    }

    @Override
    public SuccessResponse balanceEnquiry(String accountNumber) {
        return null;
    }

    @Override
    public SuccessResponse fundTransfer(BankPaymentDTO paymentDTO) {
        //String reference, String amount, String narration, String crAccountName, String bankName, String drAccountName, String crAccount, String bankCode
        return null;
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

    public boolean validateBasicAuth(String token) throws Exception {

        String username = "";
        String password = "";
        final String credentials = Util.WayaDecrypt(token);
        String[] keyDebit = credentials.split(Pattern.quote(" "));
        Optional<VirtualAccountHook> virtualAccountHook = virtualAccountRepository.findByUsernameAndPassword(keyDebit[0],keyDebit[1]);
        if(!virtualAccountHook.isPresent()){
            return false;
        }

        return (keyDebit[0].equals(virtualAccountHook.get().getUsername())) && (keyDebit[1].equals(virtualAccountHook.get().getPassword()));
    }


}
