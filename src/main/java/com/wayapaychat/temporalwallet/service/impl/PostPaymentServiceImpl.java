package com.wayapaychat.temporalwallet.service.impl;

import com.wayapaychat.temporalwallet.config.SecurityConstants;
import com.wayapaychat.temporalwallet.dao.AuthUserServiceDAO;
import com.wayapaychat.temporalwallet.dao.TemporalWalletDAO;
import com.wayapaychat.temporalwallet.dto.AccountLienDTO;
import com.wayapaychat.temporalwallet.dto.AdminLocalTransferDTO;
import com.wayapaychat.temporalwallet.dto.ExternalCBAResponse;
import com.wayapaychat.temporalwallet.dto.MifosTransfer;
import com.wayapaychat.temporalwallet.entity.*;
import com.wayapaychat.temporalwallet.enumm.CategoryType;
import com.wayapaychat.temporalwallet.enumm.ProductPriceStatus;
import com.wayapaychat.temporalwallet.enumm.ProviderType;
import com.wayapaychat.temporalwallet.enumm.TransactionTypeEnum;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.interceptor.TokenImpl;
import com.wayapaychat.temporalwallet.notification.CustomNotification;
import com.wayapaychat.temporalwallet.pojo.MyData;
import com.wayapaychat.temporalwallet.pojo.UserDetailPojo;
import com.wayapaychat.temporalwallet.proxy.MifosWalletProxy;
import com.wayapaychat.temporalwallet.repository.*;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.service.IPostPaymentService;
import com.wayapaychat.temporalwallet.service.SwitchWalletService;
import com.wayapaychat.temporalwallet.service.TransactionCountService;
import com.wayapaychat.temporalwallet.service.UserAccountService;
import com.wayapaychat.temporalwallet.util.MessageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static com.wayapaychat.temporalwallet.util.Constant.ADMIN_TRANSACTION;

@Slf4j
@Service
public class PostPaymentServiceImpl implements IPostPaymentService {
    private final SwitchWalletService switchWalletService;
    private final TokenImpl tokenService;
    private final AuthUserServiceDAO authService;
    private final CustomNotification customNotification;
    private final TemporalWalletDAO tempwallet;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletAccountRepository walletAccountRepository;
    private final WalletUserRepository walletUserRepository;
    private final WalletEventRepository walletEventRepository;
    private final MifosWalletProxy mifosWalletProxy;
    private final UserPricingRepository userPricingRepository;
    private final TransactionCountService transactionCountService;
    private final UserAccountService userAccountService;
    private final ExternalServiceProxyImpl externalServiceProxy;

    @Autowired
    public PostPaymentServiceImpl(SwitchWalletService switchWalletService, TokenImpl tokenService, AuthUserServiceDAO authService, CustomNotification customNotification, TemporalWalletDAO tempwallet, WalletTransactionRepository walletTransactionRepository, WalletAccountRepository walletAccountRepository, WalletUserRepository walletUserRepository, WalletEventRepository walletEventRepository, MifosWalletProxy mifosWalletProxy, UserPricingRepository userPricingRepository, TransactionCountService transactionCountService, UserAccountService userAccountService, ExternalServiceProxyImpl externalServiceProxy) {
        this.switchWalletService = switchWalletService;
        this.tokenService = tokenService;
        this.authService = authService;
        this.customNotification = customNotification;
        this.tempwallet = tempwallet;
        this.walletTransactionRepository = walletTransactionRepository;
        this.walletAccountRepository = walletAccountRepository;
        this.walletUserRepository = walletUserRepository;
        this.walletEventRepository = walletEventRepository;
        this.mifosWalletProxy = mifosWalletProxy;
        this.userPricingRepository = userPricingRepository;
        this.transactionCountService = transactionCountService;
        this.userAccountService = userAccountService;
        this.externalServiceProxy = externalServiceProxy;
    }

    @Override
    public ApiResponse<?> AdminsendMoney(HttpServletRequest request, AdminLocalTransferDTO transfer) {
        Provider provider = switchWalletService.getActiveProvider();
        System.out.println("provider :: {} " + provider);
        if (provider == null) {
            return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "NO PROVIDER SWITCHE", null);
        }
        log.info("WALLET PROVIDER: " + provider.getName());
        switch (provider.getName()) {
            case ProviderType.TEMPORAL:
                return processAdminsendMoney(request, transfer, false);
            case ProviderType.MIFOS:
                return processAdminsendMoney(request, transfer, true);
            default:
                return null;
        }
    }

    ApiResponse<?> processAdminsendMoney(HttpServletRequest request, AdminLocalTransferDTO transfer, boolean isMifos) {
        String token = request.getHeader(SecurityConstants.HEADER_STRING);

        MyData userToken = isTokenValid(token);  // check for valid token

        UserDetailPojo user = authService.AuthUser(transfer.getUserId().intValue());

        isValidUser(user);  // check for valid user

        isAdminUser(user);  // check if user is Admin

        isFromAccountNumberEqualToAccountNumber(transfer.getDebitAccountNumber(), transfer.getBenefAccountNumber());  // check that DebitAccountNumber is different from BenefAccountNumber

        TransactionTypeEnum tranType = TransactionTypeEnum.valueOf(transfer.getTranType());
        CategoryType tranCategory = CategoryType.valueOf("TRANSFER");

        ApiResponse<?> resp = new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "INVAILED ACCOUNT NO", null);
        try {
            String fromAccountNumber = transfer.getDebitAccountNumber();
            String toAccountNumber = transfer.getBenefAccountNumber();


            int intRec = tempwallet.PaymenttranInsert("WAYAADMTOCUS", fromAccountNumber, toAccountNumber, transfer.getAmount(),
                    transfer.getPaymentReference());
            if (intRec == 1) {
                String tranId = createTransaction(token, "WAYAADMTOCUS", fromAccountNumber, toAccountNumber, transfer.getTranCrncy(),
                        transfer.getAmount(), tranType, transfer.getTranNarration(), transfer.getPaymentReference(),
                        request, tranCategory, isMifos);
                String[] tranKey = tranId.split(Pattern.quote("|"));
                if (tranKey[0].equals("DJGO")) {
                    return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, tranKey[1], null);
                }
                List<WalletTransaction> transaction = walletTransactionRepository
                        .findByTranIdIgnoreCase(tranId).orElse(null);
                if (transaction == null) {
                    return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "TRANSACTION FAILED TO CREATE", null);
                }
                resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "TRANSACTION SUCCESSFUL", transaction);

                Date tDate = new Date();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String tranDate = dateFormat.format(tDate);

                WalletAccount xAccount = walletAccountRepository.findByAccountNo(fromAccountNumber);
                WalletUser xUser = walletUserRepository.findByAccount(xAccount);
                String xfullName = xUser.getFirstName() + " " + xUser.getLastName();

                String message1 = MessageHelper.formatDebitMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
                        transfer.getTranNarration());
                CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, xfullName,
                        xUser.getEmailAddress(), message1, userToken.getId(), transfer.getAmount().toString(), tranId,
                        tranDate, transfer.getTranNarration()));
                CompletableFuture.runAsync(() -> customNotification.pushSMS(token, xfullName, xUser.getMobileNo(),
                        message1, userToken.getId()));
                CompletableFuture.runAsync(() -> customNotification.pushInApp(token, xfullName, "0",
                        message1, xUser.getUserId(),ADMIN_TRANSACTION));

                WalletAccount yAccount = walletAccountRepository.findByAccountNo(toAccountNumber);
                WalletUser yUser = walletUserRepository.findByAccount(yAccount);
                String yfullName = yUser.getFirstName() + " " + yUser.getLastName();

                String message2 = MessageHelper.formatNewMessage(transfer.getAmount(), tranId, tranDate, transfer.getTranCrncy(),
                        transfer.getTranNarration());
                CompletableFuture.runAsync(() -> customNotification.pushTranEMAIL(token, yfullName,
                        yUser.getEmailAddress(), message2, userToken.getId(), transfer.getAmount().toString(), tranId,
                        tranDate, transfer.getTranNarration()));
                CompletableFuture.runAsync(() -> customNotification.pushSMS(token, yfullName, yUser.getMobileNo(),
                        message2, userToken.getId()));
                CompletableFuture.runAsync(() -> customNotification.pushInApp(token, yfullName, yUser.getUserId().toString(),
                        message2, 0L,ADMIN_TRANSACTION));

            } else {
                if (intRec == 2) {
                    return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND,
                            "Unable to process duplicate transaction", null);
                } else {
                    return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unknown Database Error", null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resp;
    }




    private void isFromAccountNumberEqualToAccountNumber(String fromAccountNumber, String toAccountNumber){

        if(fromAccountNumber.equals(toAccountNumber)) {
            throw new CustomException("DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT", HttpStatus.EXPECTATION_FAILED);
            // return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "DEBIT ACCOUNT CAN'T BE THE SAME WITH CREDIT ACCOUNT", null);
        }
    }

    private void isValidUser(UserDetailPojo user){
        log.info("UserDetailPojo: user {} ", user);
        if (user == null) {
            throw new CustomException("INVALID USER ID", HttpStatus.NOT_FOUND);
        }
    }

    private void isAdminUser(UserDetailPojo user){
        if (!user.is_admin()) {
            throw new CustomException("USER ID PERFORMING OPERATION IS NOT AN ADMIN", HttpStatus.NOT_FOUND);
        }
    }

    private MyData isTokenValid(String token){
        MyData userToken = tokenService.getTokenUser(token);
        if (userToken == null) {
            throw new CustomException("INVALID TOKEN", HttpStatus.NOT_FOUND);
        }
        return userToken;
    }



    private String checkCount(String paymentRef, String creditAcctNo){
        String tranCount = tempwallet.transactionCount(paymentRef, creditAcctNo);
        if (!tranCount.isBlank()) {
            return "tranCount";
        }
        return "";
    }

    private String validateEvent(Optional<WalletEventCharges> eventInfo){

        if (eventInfo.isEmpty()) {
            return "DJGO|Event ID Validation Failed";
        }
        return "";
    }

    private String checkTranType(TransactionTypeEnum tranType){
        String tranId;
        if (tranType.getValue().equalsIgnoreCase("CARD") || tranType.getValue().equalsIgnoreCase("LOCAL")) {
            tranId = tempwallet.SystemGenerateTranId();
        } else {
            tranId = tempwallet.GenerateTranId();
        }
        return tranId;
    }



    public String createTransaction(String token, String eventId, String debitAcctNo, String creditAcctNo, String tranCrncy, BigDecimal amount,
                                    TransactionTypeEnum tranType, String tranNarration, String paymentRef, HttpServletRequest request,
                                    CategoryType tranCategory, boolean mifos) {
        log.info("request : " + request);
        BigDecimal tranAmCharges;
        Optional<WalletEventCharges> eventInfo = walletEventRepository.findByEventId(eventId);
        try {

            System.out.println("############# BEFORE mifos " + mifos);
            // call Mifos
            if(mifos){
                WalletAccount accountDebit = walletAccountRepository.findByAccountNo(debitAcctNo);
                WalletAccount accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
                System.out.println("############# BEFORE mifos inside 1");

                String finalTranId = tempwallet.GenerateTranId();
                System.out.println("############# BEFORE mifos inside 2");
                ApiResponse<?> response = postToMifos(token, accountCredit, accountDebit, amount, tranNarration, finalTranId,  tranType);

                System.out.println("############# AFTER mifos inside 2" + response);
                if(!response.getStatus()){
                    throw new CustomException("Error in posting to MIFOS", HttpStatus.EXPECTATION_FAILED);
                }
            }

            int n = 1;
            log.info("START TRANSACTION");
            String checkCount = checkCount(paymentRef, creditAcctNo);
            log.info(checkCount);

            String validateEvent = validateEvent(eventInfo);
            log.info(validateEvent);

            if (eventInfo.isEmpty())
                throw new CustomException("Event is null", HttpStatus.EXPECTATION_FAILED);

            WalletEventCharges charge = eventInfo.get();
            WalletAccount eventAcct = walletAccountRepository.findByAccountNo(creditAcctNo);

            WalletAccount accountDebitTeller = walletAccountRepository
                    .findByUserPlaceholder(charge.getPlaceholder(), charge.getCrncyCode(), eventAcct.getSol_id()).orElse(null);


            // ########################### REMOVE THIS CODE ########################


            // To fetch BankAcccount and Does it exist
            WalletAccount accountDebit = walletAccountRepository.findByAccountNo(debitAcctNo);
            WalletAccount accountCredit; // = walletAccountRepository.findByAccountNo(creditAcctNo);

            System.out.println(" debitAcctNo ::: " + accountDebit);
            System.out.println(" accountCredit ::: " + creditAcctNo);

            if (!charge.isChargeCustomer() && charge.isChargeWaya()) {
                if(!StringUtils.isNumeric(debitAcctNo)){
                    accountDebit = accountDebitTeller;
                }

                accountCredit = walletAccountRepository.findByAccountNo(creditAcctNo);
                tranAmCharges = charge.getTranAmt();
            } else {
                System.out.println( "#################### HERE WER AER CHARGIN CUSTOMER #################### ");
                System.out.println(debitAcctNo + " Charge here " + charge.getTranAmt());
                accountCredit = accountDebitTeller;
                accountDebit = walletAccountRepository.findByAccountNo(debitAcctNo);
                //tranAmCharges = charge.getTranAmt();

                System.out.println( "#################### ABOUT TO GET USER PRODUCT #################### " + eventId );
                // get user charge by eventId and userID
                UserPricing userPricingOptional = getUserProduct(accountDebit, eventId); // get user to
                System.out.println( "#################### USER PRODUCT RESPONSE #################### " + eventId );

                System.out.println( "#################### ABOUT TO GET  PRODUCT RESPONSE #################### " + eventId );
                tranAmCharges = getChargesAmount(userPricingOptional, amount);

            }

            // place a lien amount

            // ###############################################
            MyData tokenData = tokenService.getUserInformation();
            String email = tokenData != null ? tokenData.getEmail() : "";
            String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";
            // ########################### REMOVE THIS CODE UP ########################


            // **********************************************
            String tranId = checkTranType(tranType);  // check for Transaction Type

            if (tranId.equals("")) {
                return "DJGO|TRANSACTION ID GENERATION FAILED: PLS CONTACT ADMIN";
            }

            String senderName="";
            String receiverName="";
            if(accountDebit !=null && accountCredit !=null){
                removeLien(accountDebit,amount);  // Remove Lien
                senderName = accountDebit.getAcct_name();
                receiverName = accountCredit.getAcct_name();
                String tranNarrate = "WALLET-" + tranNarration;
                // Do debit and credit
                doDebitAndCredit(tranId, accountDebit, accountCredit, amount, tranType, tranCrncy, tranCategory, paymentRef, senderName, receiverName, tranNarrate, n, userId, email);
            }

            // To send sms and email notification
            if(!eventId.equals("WAYAOFFTOCUS")){
                WalletAccount finalAccountDebit = accountDebit;
                String finalSenderName = senderName;
                String finalReceiverName = receiverName;
                CompletableFuture.runAsync(() -> doDeductTransactionCharges(tokenData, finalSenderName, finalReceiverName, paymentRef, tranCrncy, tranCategory, tranAmCharges, finalAccountDebit, accountCredit));
            }

            // credit merchant wallet
            log.info("END TRANSACTION");
            // HttpServletRequest request


            String receiverAcct = Objects.requireNonNull(accountCredit).getAccountNo();

            if(StringUtils.isNumeric(Objects.requireNonNull(accountDebit).getAccountNo())){
                WalletUser xUser = walletUserRepository.findByAccount(accountDebit);
                Long xUserId = xUser.getUserId();

                if(xUserId !=null){
                    CompletableFuture.runAsync(() -> transactionCountService.makeCount(String.valueOf(xUserId), paymentRef));
                }
                String finalSenderName1 = senderName;
                String finalReceiverName1 = receiverName;
                CompletableFuture.runAsync(() -> externalServiceProxy.printReceipt(amount, receiverAcct, paymentRef,
                        new Date(), tranType.getValue(), xUser.getUserId().toString(), finalReceiverName1, tranCategory.getValue(), token, finalSenderName1));
            }


            // Remove Lien
            removeLien(accountDebit,amount);
            System.out.println("removeLien the second time");

            return tranId;
        } catch (Exception e) {
            e.printStackTrace();
            return ("DJGO|" + e.getMessage());
        }

    }


    private void doDebitAndCredit(String tranId, WalletAccount accountDebit, WalletAccount accountCredit, BigDecimal amount, TransactionTypeEnum tranType, String tranCrncy, CategoryType tranCategory, String paymentRef, String senderName, String receiverName, String tranNarrate, int n, String userId, String email){

        WalletTransaction tranDebit = new WalletTransaction(tranId, accountDebit.getAccountNo(), amount, tranType,
                tranNarrate, LocalDate.now(), tranCrncy, "D", accountDebit.getGl_code(), paymentRef, userId, email,
                n, tranCategory, senderName, receiverName);

        n = n + 1;

        WalletTransaction tranCredit = new WalletTransaction(tranId, accountCredit.getAccountNo(), amount, tranType,
                tranNarrate, LocalDate.now(), tranCrncy, "C", accountCredit.getGl_code(), paymentRef, userId, email,
                n, tranCategory, senderName, receiverName);
        walletTransactionRepository.saveAndFlush(tranDebit);
        walletTransactionRepository.saveAndFlush(tranCredit);
        tempwallet.updateTransaction(paymentRef, amount, tranId);

        double clrbalAmtDr = accountDebit.getClr_bal_amt() - amount.doubleValue();
        double cumbalDrAmtDr = accountDebit.getCum_dr_amt() + amount.doubleValue();
        accountDebit.setLast_tran_id_dr(tranId);
        accountDebit.setClr_bal_amt(clrbalAmtDr);
        accountDebit.setCum_dr_amt(cumbalDrAmtDr);
        accountDebit.setLast_tran_date(LocalDate.now());
        walletAccountRepository.saveAndFlush(accountDebit);

        double clrbalAmtCr = accountCredit.getClr_bal_amt() + amount.doubleValue();
        double cumbalCrAmtCr = accountCredit.getCum_cr_amt() + amount.doubleValue();
        accountCredit.setLast_tran_id_cr(tranId);
        accountCredit.setClr_bal_amt(clrbalAmtCr);
        accountCredit.setCum_cr_amt(cumbalCrAmtCr);
        accountCredit.setLast_tran_date(LocalDate.now());
        walletAccountRepository.saveAndFlush(accountCredit);
    }

    private void removeLien(WalletAccount accountDebit, BigDecimal amount){

        System.out.println("############### AccountDebit ::: #################3  " + accountDebit);
        // get user current Lien
        AccountLienDTO accountLienDTO = new AccountLienDTO();
        accountLienDTO.setCustomerAccountNo(accountDebit.getAccountNo());
        accountLienDTO.setLien(false);
        accountLienDTO.setLienReason("no longer needed");
        //double lienAmount = accountDebit.getLien_amt() - amount.doubleValue();

        accountLienDTO.setLienAmount(amount);

        ResponseEntity<?> responseEntity;
        try{
            responseEntity = userAccountService.AccountAccessLien(accountLienDTO);
        }catch (CustomException ex){
            throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }

        System.out.println("############### RESPONSE FROM REMOVING LIEN  :: ###############"  + responseEntity);

    }


    private ApiResponse<?> postToMifos(String token,WalletAccount accountCredit, WalletAccount accountDebit, BigDecimal amount, String tranNarration, String tranId,
                                       TransactionTypeEnum tranType){
        System.out.println("############# BEFORE accountCredit " + accountCredit);
        System.out.println("############# accountDebit" + accountDebit);
        System.out.println("############# amount" + amount);
        System.out.println("############# tranNarration" + tranNarration);
        System.out.println("############# tranNarration" + tranId);
        System.out.println("############# tranNarration" + tranType);


        MifosTransfer mifosTransfer = new MifosTransfer();
        mifosTransfer.setAmount(amount);
        mifosTransfer.setDestinationAccountNumber(accountCredit.getNubanAccountNo() !=null ? accountCredit.getNubanAccountNo(): accountCredit.getAccountNo());

        mifosTransfer.setDestinationAccountType(accountCredit.getAccountType() !=null ? accountCredit.getAccountType() : "SAVINGS");
        mifosTransfer.setDestinationCurrency(accountCredit.getAcct_crncy_code());
        mifosTransfer.setNarration(tranNarration);
        mifosTransfer.setRequestId(tranId+"345493");
        mifosTransfer.setSourceAccountNumber(accountDebit.getNubanAccountNo() !=null ? accountDebit.getNubanAccountNo() : accountDebit.getAccountNo());

        mifosTransfer.setSourceAccountType("SAVINGS");
        mifosTransfer.setSourceCurrency(accountDebit.getAcct_crncy_code());
        mifosTransfer.setTransactionType(TransactionTypeEnum.TRANSFER.getValue());
        ExternalCBAResponse response;
        System.out.println(" here" + mifosTransfer);
        try{
            log.info("## token  ####### :: " + token);
            log.info("## BEFOR MIFOS REQUEST ####### :: " + mifosTransfer);
            response = mifosWalletProxy.transferMoney(mifosTransfer);
            log.info("### RESPONSE FROM MIFOS MifosWalletProxy  ###### :: " + response);
        }catch(CustomException ex){
            System.out.println("ERROR posting to MIFOS :::: " + ex.getMessage());
            throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }

        return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "SUCCESS", response.getResponseCode());
    }

    private UserPricing getUserProduct(WalletAccount accountDebit, String eventId){
        WalletUser xUser = walletUserRepository.findByAccount(accountDebit);
        Long xUserId = xUser.getUserId();
        // get user charge by eventId and userID
        return userPricingRepository.findDetails(xUserId,eventId).orElse(null);
    }

    private BigDecimal computePercentage(BigDecimal amount, BigDecimal percentageValue){
        BigDecimal per = BigDecimal.valueOf(percentageValue.doubleValue() / 100);
        return BigDecimal.valueOf(per.doubleValue() * amount.doubleValue());
    }

    private BigDecimal getChargesAmount(UserPricing userPricingOptional, BigDecimal amount){
        BigDecimal percentage = null;

        if(userPricingOptional.getStatus().equals(ProductPriceStatus.GENERAL)){

            percentage = computePercentage(amount, userPricingOptional.getGeneralAmount());
            // apply discount if applicable

        }else if (userPricingOptional.getStatus().equals(ProductPriceStatus.CUSTOM)){

            // apply discount if applicable
            percentage = computePercentage(amount, userPricingOptional.getCustomAmount());

        }

//        if (percentage.compareTo(userPricingOptional.getCapPrice()) == 1){
//
//        }
        return percentage;

    }

    private  void doDeductTransactionCharges(MyData tokenData, String senderName, String receiverName, String paymentRef, String tranCrncy, CategoryType tranCategory, BigDecimal chargesAmount, WalletAccount accountDebit, WalletAccount accountCredit){
        try{
            int n = 1;
            // Token Fetch

            String email = tokenData != null ? tokenData.getEmail() : "";
            String userId = tokenData != null ? String.valueOf(tokenData.getId()) : "";

            String tranId;
            if (TransactionTypeEnum.CHARGES.getValue().equalsIgnoreCase("CARD") || TransactionTypeEnum.CHARGES.getValue().equalsIgnoreCase("LOCAL")) {
                tranId = tempwallet.SystemGenerateTranId();
            } else {
                tranId = tempwallet.GenerateTranId();
            }
            if (tranId.equals("")) {
                throw new CustomException("DJGO|TRANSACTION ID GENERATION FAILED: PLS CONTACT ADMIN", HttpStatus.EXPECTATION_FAILED);
            }


            String tranNarrate = "WALLET-" + "transaction charges";
            doDebitAndCredit(tranId, accountDebit, accountCredit, chargesAmount, TransactionTypeEnum.CHARGES, tranCrncy, tranCategory, paymentRef, senderName, receiverName, tranNarrate, n, userId, email);

        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("DJGO|" + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }

    }




}
