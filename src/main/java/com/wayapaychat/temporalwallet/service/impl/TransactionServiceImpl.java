
 package com.wayapaychat.temporalwallet.service.impl;

 import com.wayapaychat.temporalwallet.dto.AccountLienDTO;
 import com.wayapaychat.temporalwallet.dto.TemporalToOfficialWalletDTO;
 import com.wayapaychat.temporalwallet.entity.*;
 import com.wayapaychat.temporalwallet.enumm.ProductPriceStatus;
 import com.wayapaychat.temporalwallet.enumm.WalletTransStatus;
 import com.wayapaychat.temporalwallet.exception.CustomException;
 import com.wayapaychat.temporalwallet.interceptor.TokenImpl;
 import com.wayapaychat.temporalwallet.pojo.MyData;
 import com.wayapaychat.temporalwallet.pojo.TransactionPojo;
 import com.wayapaychat.temporalwallet.pojo.TransactionTransferPojo;
 import com.wayapaychat.temporalwallet.pojo.TransactionTransferPojo2;
 import com.wayapaychat.temporalwallet.repository.*;
 import com.wayapaychat.temporalwallet.service.TransAccountService;
 import com.wayapaychat.temporalwallet.service.TransactionService;
 import com.wayapaychat.temporalwallet.service.UserAccountService;
 import com.wayapaychat.temporalwallet.util.ParamDefaultValidation;
 import com.wayapaychat.temporalwallet.util.ReqIPUtils;
 import com.wayapaychat.temporalwallet.util.ResponseHelper;
 import com.wayapaychat.temporalwallet.util.Util;
 import lombok.extern.slf4j.Slf4j;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Service;

 import javax.servlet.http.HttpServletRequest;
 import java.math.BigDecimal;
 import java.time.LocalDateTime;
 import java.util.List;
 import java.util.Map;
 import java.util.Objects;
 import java.util.Optional;
 import java.util.regex.Pattern;

 @Service
 @Slf4j
public class TransactionServiceImpl implements TransactionService {

     private final ParamDefaultValidation paramValidation;
     private final WalletAccountRepository walletAccountRepository;
     private final ReqIPUtils reqIPUtils;
     private final TokenImpl tokenService;
     private final UserAccountService userAccountService;
     private final WalletEventRepository walletEventRepository;
     private final WalletUserRepository walletUserRepository;
     private final UserPricingRepository userPricingRepository;
     private final WalletTransAccountRepository walletTransAccountRepository;
     private final TransAccountService transAccountService;

     @Autowired
     public TransactionServiceImpl(ParamDefaultValidation paramValidation, WalletAccountRepository walletAccountRepository, ReqIPUtils reqIPUtils, TokenImpl tokenService, UserAccountService userAccountService, WalletEventRepository walletEventRepository, WalletUserRepository walletUserRepository, UserPricingRepository userPricingRepository, WalletTransAccountRepository walletTransAccountRepository, TransAccountService transAccountService) {
         this.paramValidation = paramValidation;
         this.walletAccountRepository = walletAccountRepository;
         this.reqIPUtils = reqIPUtils;
         this.tokenService = tokenService;
         this.userAccountService = userAccountService;
         this.walletEventRepository = walletEventRepository;
         this.walletUserRepository = walletUserRepository;
         this.userPricingRepository = userPricingRepository;
         this.walletTransAccountRepository = walletTransAccountRepository;
         this.transAccountService = transAccountService;
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



     private WalletAccount getAccountNumber(String debitAccountNumber){
       return walletAccountRepository.findByAccountNo(debitAccountNumber);
     }


     private void initLien(String debitAccountNumber, BigDecimal actualAmount){
         System.out.println("############### INSIDE initLien ############## " + debitAccountNumber);
         // get user current Lien
         WalletAccount accountDebit = getAccountNumber(debitAccountNumber);

         // check if lien was placed previously
         double lienActualAmount = accountDebit.getLien_amt() + actualAmount.doubleValue();

         System.out.println("accountDebit.getClr_bal_amt() :: " + accountDebit.getClr_bal_amt());
         System.out.println("lienActualAmount :: " + lienActualAmount);
         if(accountDebit.getClr_bal_amt() < lienActualAmount){
             throw new CustomException("DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE",HttpStatus.EXPECTATION_FAILED);
         }

//         if (accountDebit.getLien_amt() == actualAmount.doubleValue()) {
//             throw new CustomException("DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE",HttpStatus.EXPECTATION_FAILED);
//         }

         AccountLienDTO accountLienDTO = new AccountLienDTO();
         accountLienDTO.setCustomerAccountNo(debitAccountNumber);
         accountLienDTO.setLien(true);
         accountLienDTO.setLienAmount(actualAmount);

         ResponseEntity<?> responseEntity;
         try{
             System.out.println("################## BEFORE LIEN REQUEST ########### "+ accountLienDTO);
             responseEntity = userAccountService.AccountAccessLien(accountLienDTO);
         }catch (CustomException ex){
             throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
         }
            System.out.println("############### RESPONSE FROM LIEN INIT :: ###############"  + responseEntity);

     }



     private UserPricing getUserProduct(WalletAccount accountDebit, String eventId){
         WalletUser xUser = walletUserRepository.findByAccount(accountDebit);
         Long xUserId = xUser.getUserId();
         // get user charge by eventId and userID
         return userPricingRepository.findDetails(xUserId,eventId).orElse(null);
     }

     private BigDecimal getChargesAmount(UserPricing userPricingOptional, BigDecimal amount){
         BigDecimal percentage = null;

         if(userPricingOptional.getStatus().equals(ProductPriceStatus.GENERAL)){

             percentage = Util.computePercentage(amount, userPricingOptional.getGeneralAmount());
             // apply discount if applicable

         }else if (userPricingOptional.getStatus().equals(ProductPriceStatus.CUSTOM)){

             // apply discount if applicable
             percentage = Util.computePercentage(amount, userPricingOptional.getCustomAmount());

         }
//         if (percentage.compareTo(userPricingOptional.getCapPrice()) == 1){
//
//         }
         return percentage;

     }



     private BigDecimal getWhoToCharge(String eventId, String toAccountNumber, String fromAccountNumber, BigDecimal amount){
         BigDecimal tranAmCharges = BigDecimal.ZERO;
         WalletEventCharges charge = null;
         WalletAccount accountDebitTeller = null;
         Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId(eventId);
         if (eventInfo.isPresent()) {
             charge = eventInfo.get();
             WalletAccount eventAcct = walletAccountRepository.findByAccountNo(toAccountNumber);

             accountDebitTeller = walletAccountRepository
                     .findByUserPlaceholder(charge.getPlaceholder(), charge.getCrncyCode(), eventAcct.getSol_id()).orElse(null);

         }
         WalletAccount accountDebit; // = walletAccountRepository.findByAccountNo(debitAcctNo);
         WalletAccount accountCredit; // = walletAccountRepository.findByAccountNo(creditAcctNo);

         if(charge !=null){

             if (!charge.isChargeCustomer() && charge.isChargeWaya()) {
               //  accountDebit = accountDebitTeller;
               //  accountCredit = walletAccountRepository.findByAccountNo(toAccountNumber);
                 tranAmCharges = charge.getTranAmt();
                 log.info( "#################### CHARGING FROM ADMIN #################### " +  charge.getTranAmt());
             } else {
                 System.out.println( "#################### HERE WER AER CHARGING CUSTOMER #################### ");
                 System.out.println("debitAcctNo" + " Charge here " + charge.getTranAmt());
                 //accountCredit = accountDebitTeller;
                 log.info("accountDebitTeller :" + accountDebitTeller);
                 accountDebit = walletAccountRepository.findByAccountNo(fromAccountNumber);

                 System.out.println( "#################### ABOUT TO GET USER PRODUCT #################### " + eventId );
                 // get user charge by eventId and userID
                 UserPricing userPricingOptional = getUserProduct(accountDebit, eventId); // get user to
                 System.out.println( "#################### USER PRODUCT RESPONSE #################### " + eventId );

                 System.out.println( userPricingOptional +  " #################### ABOUT TO GET  PRODUCT RESPONSE #################### " + eventId );
                 tranAmCharges = getChargesAmount(userPricingOptional, amount);

                 System.out.println( "#################### RESPONSE FROM GET  PRODUCT RESPONSE #################### " + tranAmCharges );

             }
         }

         return tranAmCharges;
     }



     private Long transAccount(HttpServletRequest request, String fromAccountNumber, String toAccountNumber, BigDecimal amount, String transCategory,String tranCrncy, WalletTransStatus status){
         System.out.println( "##### HERER  transAccount " + request);
         System.out.println( "##### HERER  transAccount " + fromAccountNumber);
         System.out.println( "##### HERER  transAccount " + toAccountNumber);
         System.out.println( "##### HERER  transAccount " + tranCrncy);
         System.out.println( "##### HERER  transAccount " + status);

         Util util = new Util();
         String code = util.generateRandomNumber(9);
         try{
             //WAYABANKTRANS

             Optional<WalletAccount> accountDebitTeller = Optional.empty();
             Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId("WAYABANKTRANS");
             WalletAccount eventAcct = walletAccountRepository.findByAccountNo(toAccountNumber);
             if(eventInfo.isPresent()){
                 WalletEventCharges charge = eventInfo.get();
                 accountDebitTeller = walletAccountRepository
                         .findByUserPlaceholder(charge.getPlaceholder(), charge.getCrncyCode(), eventAcct.getSol_id());
             }

             if (!accountDebitTeller.isPresent()){
                 throw new CustomException("Error accountDebitTeller not present", HttpStatus.EXPECTATION_FAILED);
             }
             WalletAccount accountDebit = accountDebitTeller.get();


             TemporalToOfficialWalletDTO temp = new TemporalToOfficialWalletDTO();
             temp.setAmount(amount);
             temp.setCustomerAccountNumber(fromAccountNumber);
             temp.setOfficialAccountNumber(accountDebit.getAccountNo());
             temp.setPaymentReference(code);
             temp.setTranCrncy(tranCrncy);
             temp.setTranNarration("Transaction in transit");
             temp.setTransactionCategory(transCategory);

//             System.out.println( "##### before sending request TemporalWalletToOfficialWallet " + temp);
//             ResponseEntity<?> responseEntity = transAccountService.TemporalWalletToOfficialWallet(request, temp);
//             System.out.println( "##### after sending request TemporalWalletToOfficialWallet Response " + responseEntity);
//
//             ResponseHelper walletTransactions = (ResponseHelper) responseEntity.getBody();
//             System.out.println("responseEntity :: " + responseEntity.getBody());
//             System.out.println("walletTransactions :: " + Objects.requireNonNull(walletTransactions).getData());
//
//             Optional<List<WalletTransaction>> walletTransactions1 = (Optional<List<WalletTransaction>>) walletTransactions.getData();
//             System.out.println("walletTransactions1 :: " + walletTransactions1);
//
//             String tranI2 = "";
//             if (walletTransactions1.isPresent()){
//                 List<WalletTransaction> transactionList = walletTransactions1.get();
//                 for (WalletTransaction data: transactionList){
//                     tranI2 = data.getTranId();
//                 }
//             }

             System.out.println("here is the new ID  :: " + code);

             WalletTransAccount walletTransAccount = new WalletTransAccount();
             walletTransAccount.setCreatedAt(LocalDateTime.now());
             walletTransAccount.setDebitAccountNumber(fromAccountNumber);
             walletTransAccount.setCreditAccountNumber(toAccountNumber);
             walletTransAccount.setTranAmount(amount);
             walletTransAccount.setTranId(code);
             walletTransAccount.setTransactionType(transCategory);
             walletTransAccount.setTranCrncy(tranCrncy);
             walletTransAccount.setStatus(status);
             return walletTransAccountRepository.save(walletTransAccount).getId();
         }catch (CustomException ex){
             System.out.println( " TGHis is the error " + ex.getMessage());
             throw new CustomException("error", HttpStatus.EXPECTATION_FAILED);
         }
     }

     private void updateTransAccount(Long tranId, WalletTransStatus status){
         try{
             Optional<WalletTransAccount> walletTransAccount = walletTransAccountRepository.findById(tranId);
             if (walletTransAccount.isPresent()){
                 WalletTransAccount walletTransAccount1 = walletTransAccount.get();
                 walletTransAccount1.setStatus(status);
                 walletTransAccountRepository.save(walletTransAccount1);
             }

             /// move money from the trans account and be ready to commit transaction
         }catch (CustomException ex){
             throw new CustomException("error", HttpStatus.EXPECTATION_FAILED);
         }
     }

     @Override
     public boolean processPayment(HttpServletRequest request, Map<String, Object> map) {

         // put the requested amount into a tranAccount

         log.info(" ########  REQUEST ########  "+ map);

         // Token Fetch
         MyData tokenData = tokenService.getUserInformation();
         log.info(" ############### TokenData :: ###############" + tokenData);
         String email = tokenData != null ? tokenData.getEmail() : "";
         String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";


         String fromAccountNumber = (String) map.get("debitAccountNumber");
         String toAccountNumber = (String) map.get("benefAccountNumber");
         String eventId = (String) map.get("eventId");
         String transType = map.get("transType") == null ? "LOCAL" : (String) map.get("transType");
         String transCategory = map.get("transCategory") == null ? "TRANSFER" :(String) map.get("transCategory");
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

         System.out.println("#### #####    #### ##### " + fromAccountNumber );
         System.out.println("#### #####    #### ##### " + toAccountNumber );
         System.out.println("#### #####    #### ##### " + eventId );
         System.out.println("#### #####    #### ##### " + transType );
         System.out.println("#### #####    #### ##### " + transCategory );
         System.out.println("#### #####    #### ##### " + tranCrncy );
         System.out.println("#### #####    #### ##### " + amount );


         Long tranId = transAccount(request,fromAccountNumber, toAccountNumber, amount, transCategory, tranCrncy, WalletTransStatus.PENDING);
         // 1. check for account balance ||| request amount + transaction Charges
         // 2. check the lien amount on the users account  actualBalance  accountBalance - (requestAmount + lienAmount)
         // 2. init lien with request amount + transaction charges
         log.info(" ########  transAccount ID ########  "+ tranId);
         BigDecimal transCharge = getWhoToCharge(eventId, toAccountNumber, fromAccountNumber,amount);

         log.info(" ########  getWhoToCharge ########  "+ transCharge);

         BigDecimal actualAmount = BigDecimal.valueOf(transCharge.doubleValue() + amount.doubleValue());

         log.info(" ########  actualAmount ########  "+ actualAmount);


         // To fetch BankAcccount and Does it exist
         WalletAccount accountDebit = walletAccountRepository.findByAccountNo(fromAccountNumber);
         WalletAccount accountCredit = walletAccountRepository.findByAccountNo(toAccountNumber);
         if (accountDebit == null || accountCredit == null) {
             throw new CustomException("DJGO|DEBIT ACCOUNT OR BENEFICIARY ACCOUNT DOES NOT EXIST",HttpStatus.EXPECTATION_FAILED);
         }


         initLien(fromAccountNumber, actualAmount);   /// init Lien on customer debit account

         log.info(" ########  LIEN INIT ########  "+ actualAmount);

         // Check for account security
         log.info(accountDebit.getHashed_no());

         try{
             String secureDebit = reqIPUtils.WayaDecrypt(accountDebit.getHashed_no());
             log.info(secureDebit);
             String[] keyDebit = secureDebit.split(Pattern.quote("|"));
             if ((!keyDebit[1].equals(accountDebit.getAccountNo()))
                     || (!keyDebit[2].equals(accountDebit.getProduct_code()))
                     || (!keyDebit[3].equals(accountDebit.getAcct_crncy_code()))) {
                 throw new CustomException("DJGO|DEBIT ACCOUNT DATA INTEGRITY ISSUE",HttpStatus.EXPECTATION_FAILED);
             }

             // Check for Amount Limit
             if (!accountDebit.getAcct_ownership().equals("O")) {

                 Long userID = Long.parseLong(keyDebit[0]);
                 WalletUser user = walletUserRepository.findByUserId(userID);
                 BigDecimal AmtVal = BigDecimal.valueOf(user.getCust_debit_limit());
                 if (AmtVal.compareTo(amount) == -1) {
                     throw new CustomException("DJGO|DEBIT ACCOUNT TRANSACTION AMOUNT LIMIT EXCEEDED",HttpStatus.EXPECTATION_FAILED);
                 }

                 if (BigDecimal.valueOf(accountDebit.getClr_bal_amt()).compareTo(amount) == -1) {
                     throw new CustomException("DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE",HttpStatus.EXPECTATION_FAILED);
                 }

                 if (BigDecimal.valueOf(accountDebit.getClr_bal_amt()).compareTo(BigDecimal.ONE) != 1) {
                     throw new CustomException("DJGO|DEBIT ACCOUNT INSUFFICIENT BALANCE",HttpStatus.EXPECTATION_FAILED);
                 }
             }

         }catch (Exception ex){
             log.error(ex.getMessage());
         }

         log.info(accountCredit.getHashed_no());

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
             BigDecimal userLim = new BigDecimal(Objects.requireNonNull(tokenData).getTransactionLimit());
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



         updateTransAccount(tranId,WalletTransStatus.SUCCESSFUL);
         System.out.println(" ############### ACCOUNT VALIDATION DON SUCCESSFULLY :: ###############");

         // save transaction
         // check KYC
         // check
         // check fraud rules
         return true;
     }
 }
