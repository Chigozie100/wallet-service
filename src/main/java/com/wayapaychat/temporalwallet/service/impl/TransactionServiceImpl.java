
 package com.wayapaychat.temporalwallet.service.impl;

 import com.wayapaychat.temporalwallet.entity.WalletAccount;
 import com.wayapaychat.temporalwallet.exception.CustomException;
 import com.wayapaychat.temporalwallet.interceptor.TokenImpl;
 import com.wayapaychat.temporalwallet.pojo.MyData;
 import com.wayapaychat.temporalwallet.pojo.TransactionPojo;
 import com.wayapaychat.temporalwallet.pojo.TransactionTransferPojo;
 import com.wayapaychat.temporalwallet.pojo.TransactionTransferPojo2;
 import com.wayapaychat.temporalwallet.repository.WalletAccountRepository;
 import com.wayapaychat.temporalwallet.repository.WalletTransactionRepository;
 import com.wayapaychat.temporalwallet.service.TransactionService;
 import com.wayapaychat.temporalwallet.util.ParamDefaultValidation;
 import com.wayapaychat.temporalwallet.util.ReqIPUtils;
 import lombok.extern.slf4j.Slf4j;
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Service;

 import java.math.BigDecimal;
 import java.util.Map;
 import java.util.regex.Pattern;

 @Service
 @Slf4j
public class TransactionServiceImpl implements TransactionService {

     private final WalletTransactionRepository walletTransactionRepository;
     private final ParamDefaultValidation paramValidation;
     private final WalletAccountRepository walletAccountRepository;
     private final ReqIPUtils reqIPUtils;
     private final TokenImpl tokenService;

     @Autowired
     public TransactionServiceImpl(WalletTransactionRepository walletTransactionRepository, ParamDefaultValidation paramValidation, WalletAccountRepository walletAccountRepository, ReqIPUtils reqIPUtils, TokenImpl tokenService) {
         this.walletTransactionRepository = walletTransactionRepository;
         this.paramValidation = paramValidation;
         this.walletAccountRepository = walletAccountRepository;
         this.reqIPUtils = reqIPUtils;
         this.tokenService = tokenService;
     }


     @Override
     public ResponseEntity<?> transactAmount(TransactionPojo transactionPojo) {
         return null;
     }

     @Override
     public ResponseEntity<?> transferTransaction(TransactionTransferPojo transactionTransferPojo) {
         return null;
     }

     @Override
     public ResponseEntity<?> transferTransactionWithId(TransactionTransferPojo2 transactionTransferPojo2) {
         return null;
     }


     @Override
     public boolean processPayment(Map<String, Object> map) {

         // Token Fetch
         MyData tokenData = tokenService.getUserInformation();
         String email = tokenData != null ? tokenData.getEmail() : "";
         String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";


         String fromAccountNumber = (String) map.get("debitAccountNumber");
         String toAccountNumber = (String) map.get("benefAccountNumber");
         String eventId = (String) map.get("eventId");
         String transType = (String) map.get("transType");
         String transCategory = (String) map.get("transCategory");
         String tranCrncy = (String) map.get("tranCrncy");
         BigDecimal amount = (BigDecimal) map.get("amount");

         boolean validate = paramValidation.validateDefaultCode(tranCrncy, "Currency");
         if (!validate)
             throw new CustomException("DJGO|Currency Code Validation Failed", HttpStatus.EXPECTATION_FAILED);

         if(fromAccountNumber.equals(toAccountNumber))
             throw new CustomException("DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT", HttpStatus.EXPECTATION_FAILED);

         if (fromAccountNumber.trim().equals(toAccountNumber.trim())){
             log.info(toAccountNumber + "|" + fromAccountNumber);
             throw new CustomException("DEBIT AND CREDIT ON THE SAME ACCOUNT",HttpStatus.EXPECTATION_FAILED);
         }

         if(StringUtils.isNumeric(fromAccountNumber) && StringUtils.isNumeric(toAccountNumber)){
             // To fetch BankAcccount and Does it exist
             WalletAccount accountDebit = walletAccountRepository.findByAccountNo(fromAccountNumber);
             WalletAccount accountCredit = walletAccountRepository.findByAccountNo(toAccountNumber);
             if (accountDebit == null || accountCredit == null) {
                 throw new CustomException("DJGO|DEBIT ACCOUNT OR BENEFICIARY ACCOUNT DOES NOT EXIST",HttpStatus.EXPECTATION_FAILED);
             }

             // Check for account security
             log.info(accountDebit.getHashed_no());
             // String compareDebit = tempwallet.GetSecurityTest(debitAcctNo);
             // log.info(compareDebit);
             try{
                 String secureDebit = reqIPUtils.WayaDecrypt(accountDebit.getHashed_no());
                 log.info(secureDebit);
                 String[] keyDebit = secureDebit.split(Pattern.quote("|"));
                 if ((!keyDebit[1].equals(accountDebit.getAccountNo()))
                         || (!keyDebit[2].equals(accountDebit.getProduct_code()))
                         || (!keyDebit[3].equals(accountDebit.getAcct_crncy_code()))) {
                     throw new CustomException("DJGO|DEBIT ACCOUNT DATA INTEGRITY ISSUE",HttpStatus.EXPECTATION_FAILED);
                 }
             }catch (Exception ex){
                 log.error(ex.getMessage());
             }

             log.info(accountCredit.getHashed_no());
             // String compareCredit = tempwallet.GetSecurityTest(creditAcctNo);
             // log.info(compareCredit);
             try{
                 String secureCredit = reqIPUtils.WayaDecrypt(accountCredit.getHashed_no());
                 log.info(secureCredit);
                 String[] keyCredit = secureCredit.split(Pattern.quote("|"));
                 if ((!keyCredit[1].equals(accountCredit.getAccountNo()))
                         || (!keyCredit[2].equals(accountCredit.getProduct_code()))
                         || (!keyCredit[3].equals(accountCredit.getAcct_crncy_code()))) {
                     throw new CustomException("DJGO|CREDIT ACCOUNT DATA INTEGRITY ISSUE",HttpStatus.EXPECTATION_FAILED);
                 }
             }catch (Exception ex){
                 log.error(ex.getMessage());
             }

             // AUth Security check
             // **********************************************
             if (!accountDebit.getAcct_ownership().equals("O")) {
                 if (accountDebit.isAcct_cls_flg())
                    throw new CustomException("DJGO|DEBIT ACCOUNT IS CLOSED",HttpStatus.EXPECTATION_FAILED);
                 log.info("Debit Account is: {}", accountDebit.getAccountNo());
                 log.info("Debit Account Freeze Code is: {}", accountDebit.getFrez_code());
                 if (accountDebit.getFrez_code() != null) {
                     if (accountDebit.getFrez_code().equals("D"))
                         throw new CustomException("DJGO|DEBIT ACCOUNT IS ON DEBIT FREEZE",HttpStatus.EXPECTATION_FAILED);
                 }

                 if (accountDebit.getLien_amt() != 0) {
                     double oustbal = accountDebit.getClr_bal_amt() - accountDebit.getLien_amt();
                     if (new BigDecimal(oustbal).compareTo(BigDecimal.ONE) != 1) {
                         throw new CustomException("DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE",HttpStatus.EXPECTATION_FAILED);
                     }
                     if (new BigDecimal(oustbal).compareTo(amount) == -1) {
                         throw new CustomException("DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE",HttpStatus.EXPECTATION_FAILED);
                     }
                 }
                 // 600,000  -1, 0, or 1 as this {@code BigDecimal} is numerically  less than, equal to, or greater than {@code val}.
                 BigDecimal userLim = new BigDecimal(tokenData.getTransactionLimit());
                 if (userLim.compareTo(amount) == -1) {
                     throw new CustomException("DJGO|DEBIT TRANSACTION AMOUNT LIMIT EXCEEDED",HttpStatus.EXPECTATION_FAILED);
                 }
             }

             if (!accountCredit.getAcct_ownership().equals("O")) {
                 if (accountCredit.isAcct_cls_flg())
                     throw new CustomException("DJGO|CREDIT ACCOUNT IS CLOSED",HttpStatus.EXPECTATION_FAILED);

                 log.info("Credit Account is: {}", accountCredit.getAccountNo());
                 log.info("Credit Account Freeze Code is: {}", accountCredit.getFrez_code());
                 if (accountCredit.getFrez_code() != null) {
                     if (accountCredit.getFrez_code().equals("C"))
                         throw new CustomException("DJGO|CREDIT ACCOUNT IS ON CREDIT FREEZE",HttpStatus.EXPECTATION_FAILED);
                 }
             }


         }







         // save transaction
         // check KYC
         // check
         // check fraud rules
         return false;
     }
 }
