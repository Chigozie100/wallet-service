package com.wayapaychat.temporalwallet.util;

import com.wayapaychat.temporalwallet.dto.OfficeUserTransferDTO;
import com.wayapaychat.temporalwallet.entity.*;
import com.wayapaychat.temporalwallet.enumm.PaymentRequestStatus;
import com.wayapaychat.temporalwallet.enumm.PaymentStatus;
import com.wayapaychat.temporalwallet.enumm.TransactionTypeEnum;
import com.wayapaychat.temporalwallet.interceptor.TokenImpl;
import com.wayapaychat.temporalwallet.repository.*;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.service.TransAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@Configuration
@EnableScheduling
@Slf4j
public class ScheduleJob {

    private final WalletNonWayaPaymentRepository walletNonWayaPaymentRepo;
    private final WalletAccountRepository walletAccountRepository;
    private final RecurrentConfigRepository recurrentConfigRepository;
    private final TransAccountService transAccountService;
    private final ReversalSetupRepository reversalSetupRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final TokenImpl tokenImpl;
    private final WalletUserRepository walletUserRepository;
    private final WalletPaymentRequestRepository walletPaymentRequestRepository;


    @Autowired
    public ScheduleJob(WalletNonWayaPaymentRepository walletNonWayaPaymentRepo, WalletAccountRepository walletAccountRepository, RecurrentConfigRepository recurrentConfigRepository, TransAccountService transAccountService, ReversalSetupRepository reversalSetupRepository, WalletTransactionRepository walletTransactionRepository, TokenImpl tokenImpl, WalletUserRepository walletUserRepository, WalletPaymentRequestRepository walletPaymentRequestRepository) {
        this.walletNonWayaPaymentRepo = walletNonWayaPaymentRepo;
        this.walletAccountRepository = walletAccountRepository;
        this.recurrentConfigRepository = recurrentConfigRepository;
        this.transAccountService = transAccountService;
        this.reversalSetupRepository = reversalSetupRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.tokenImpl = tokenImpl;
        this.walletUserRepository = walletUserRepository;
        this.walletPaymentRequestRepository = walletPaymentRequestRepository;
    }


    private Integer getReversalDays(){
        Optional<ReversalSetup> reversalSetup = reversalSetupRepository.findByActive();
        if(!reversalSetup.isPresent()){
            return 0;
        }
        return reversalSetup.get().getDays();
    }


    @Scheduled(cron = "${job.cron.twelam}")
    public void checkForPending() throws ParseException {

        long checkDays = getReversalDays();
        log.info("-----####### START ###### -------");
        List<WalletNonWayaPayment> walletNonWayaPaymentList = walletNonWayaPaymentRepo.findByAllByStatus(PaymentStatus.PENDING);

        log.info("OUTPUT :: {} " + walletNonWayaPaymentList);
        if(!walletNonWayaPaymentList.isEmpty()){
            String token = tokenImpl.getToken();

        for (WalletNonWayaPayment data: walletNonWayaPaymentList){
            WalletNonWayaPayment payment = walletNonWayaPaymentRepo.findById(data.getId()).orElse(null);

            SimpleDateFormat myFormat = new SimpleDateFormat("MM/dd/yyyy");

            LocalDateTime localDateTime = payment.getCreatedAt();
            Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
            Date date2 = Date.from(instant);
            String dateString2 = myFormat.format(date2);


            Date createdDate = myFormat.parse(dateString2);

            String dateString = myFormat.format(new Date());
            Date today = myFormat.parse(dateString);

            long difference = today.getTime() - createdDate.getTime();
            long daysBetween = (difference / (1000*60*60*24));
            log.info(checkDays + " ####### check Transaction upto 30 days ###### " + daysBetween);


            if(daysBetween > checkDays || daysBetween == checkDays && payment.getStatus().equals(PaymentStatus.PENDING)){
                log.info("-----####### inside Transaction upto 30 days ###### -------");
                payment.setStatus(PaymentStatus.EXPIRED);
                walletNonWayaPaymentRepo.save(payment);
                log.info( "-----####### END: record Updated ###### -------" + payment.getTranId());
                CompletableFuture.runAsync(() -> reverseNoneWayaPayment(payment.getEmailOrPhone(),token,payment.getTranId()));
            }else{
                log.info("-----####### END: NOT FOUND ###### -------");
            }

        }
        }

    }



    private Map<String, Object> noneWayaPaymentReversal(String tranId){
        String debitAccount = "";
        String creditAccount = "";
        String email = "";
        BigDecimal tranAmount = BigDecimal.ZERO;
        List<WalletTransaction> transactionList = walletTransactionRepository.findByTransaction(tranId);

        for (int i = 0; i < transactionList.size(); i++) {
            if (transactionList.get(i).getPartTranType().equals("C")){
                creditAccount = transactionList.get(i).getAcctNum();
                tranAmount = transactionList.get(i).getTranAmount();
            }
            if (transactionList.get(i).getPartTranType().equals("D")){
                debitAccount = transactionList.get(i).getAcctNum();
                email = transactionList.get(i).getCreatedBy();
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("creditAccount", creditAccount);
        map.put("debitAccount", debitAccount);
        map.put("tranAmount", tranAmount);
        map.put("email", email);
        return map;
    }

    private void reverseNoneWayaPayment(String receiverEmailOrPhone, String token,String tranId){

        Map<String, Object> map = noneWayaPaymentReversal(tranId);
        String creditAccount = (String) map.get("creditAccount");
        String debitAccount = (String) map.get("debitAccount");
        BigDecimal tranAmount = (BigDecimal) map.get("tranAmount");

        String email = (String) map.get("email");

        OfficeUserTransferDTO officeUserTransferDTO = new OfficeUserTransferDTO();
        officeUserTransferDTO.setAmount(tranAmount);
        officeUserTransferDTO.setCustomerCreditAccount(debitAccount);
        officeUserTransferDTO.setOfficeDebitAccount(creditAccount);
        officeUserTransferDTO.setPaymentReference(generatePaymentTransactionId());
        officeUserTransferDTO.setTranCrncy("NGN");
        officeUserTransferDTO.setTranNarration(tranId+ " REVERSAL TRANSACTION");
        officeUserTransferDTO.setTranType(TransactionTypeEnum.REVERSAL.name());

        Map<String, String > mapp = new HashMap<>();
        mapp.put("receiverEmail", email);


        ApiResponse<?> response = transAccountService.OfficialUserTransferSystem(mapp, token,null, officeUserTransferDTO);
        log.info(String.valueOf(response.getData()));
    }

    @Scheduled(cron = "${job.cron.twelam}")
    public void processPendingPaymentRequest() throws ParseException {

        long checkDays = getReversalDays();
        log.info("-----####### START ###### -------");
        List<WalletPaymentRequest> walletPaymentRequestList = walletPaymentRequestRepository.findByAllByStatus(PaymentRequestStatus.PENDING);

        log.info("OUTPUT :: {} " + walletPaymentRequestList);
        if(!walletPaymentRequestList.isEmpty()){
            String token = tokenImpl.getToken();

            for (WalletPaymentRequest data: walletPaymentRequestList){
                // WalletNonWayaPayment payment = walletNonWayaPaymentRepo.findById(data.getId()).orElse(null);

                SimpleDateFormat myFormat = new SimpleDateFormat("MM/dd/yyyy");

                LocalDateTime localDateTime = data.getCreatedAt();
                Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
                Date date2 = Date.from(instant);
                String dateString2 = myFormat.format(date2);

                Date createdDate = myFormat.parse(dateString2);

                String dateString = myFormat.format(new Date());
                Date today = myFormat.parse(dateString);

                long difference = today.getTime() - createdDate.getTime();
                long daysBetween = (difference / (1000*60*60*24));
                log.info(checkDays + " ####### check Transaction upto 30 days ###### " + daysBetween);

                if(daysBetween > checkDays || daysBetween == checkDays && data.getStatus().equals(PaymentRequestStatus.PENDING)){
                    log.info("-----####### inside Transaction upto 30 days ###### -------");

                    updatePaymentRequestStatus(data.getReference());
                    log.info( "-----####### END: record Updated ###### -------" );

                }else{
                    log.info("-----####### END: NOT FOUND ###### -------");
                }

            }
        }
    }


    private void updatePaymentRequestStatus(String reference){

        Optional<WalletPaymentRequest> walletPaymentRequest = walletPaymentRequestRepository.findByReferenceAndStatus(reference, PaymentRequestStatus.PENDING);
        if (walletPaymentRequest.isPresent()) {
            WalletPaymentRequest walletPaymentRequest1 = walletPaymentRequest.get();
            walletPaymentRequest1.setStatus(PaymentRequestStatus.EXPIRED);
            walletPaymentRequestRepository.save(walletPaymentRequest1);
        }
    }

    @Scheduled(cron = "${job.cron.twelam}")
    public void massDebitAndCredit() throws ParseException {
        ApiResponse<?> response = null;
        ArrayList<Object> objectArrayList = new ArrayList<>();
        RecurrentConfig recurrentConfig = recurrentConfigRepository.findByActive().orElse(null);

        if (recurrentConfig !=null){
            // check type of recurrent
            if(recurrentConfig.isRecurring()){
                if(recurrentConfig.getPayDate().compareTo(new Date()) == 0 && recurrentConfig.getDuration().equals(RecurrentConfig.Duration.MONTH)){
                    /// run and update the next date
                    processPayment(response, objectArrayList,recurrentConfig);

                    recurrentConfig.setPayDate(getNextMonth(recurrentConfig.getPayDate()));
                }else if (recurrentConfig.getPayDate().compareTo(new Date()) == 0 && recurrentConfig.getDuration().equals(RecurrentConfig.Duration.YEAR)){
                    processPayment(response, objectArrayList,recurrentConfig);
                    recurrentConfig.setPayDate(getNextYear(recurrentConfig.getPayDate()));
                }
            }
            log.info(String.valueOf(objectArrayList));
            recurrentConfigRepository.save(recurrentConfig);

        }


        /**
         *get the date
         * check the getDuration
         * update new date based on duration
         */
        /*
        1. Get all simulated users
        2. Get Offical Account
        3. Build the request object
        4. Perform Credit of Debit
         */

    }


    private void processPayment(ApiResponse<?> response, ArrayList<Object> objectArrayList, RecurrentConfig recurrentConfig){
        List<WalletAccount> userAccount = walletAccountRepository.findBySimulatedAccount();
        String token = tokenImpl.getToken();
        for(WalletAccount data: userAccount){
            OfficeUserTransferDTO transfer = getOfficeUserTransferDTO(data, recurrentConfig, "");
            response = transAccountService.OfficialUserTransferSystem(null,token,null, transfer);
            objectArrayList.add(response);
        }

    }

    public static Date getNextMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        if (calendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
        } else {
            calendar.roll(Calendar.MONTH, true);
        }
        return calendar.getTime();
    }

    public static Date getNextYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        if (calendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
        } else {

            calendar.roll(Calendar.YEAR, true);
        }
        return calendar.getTime();
    }



    private OfficeUserTransferDTO getOfficeUserTransferDTO(WalletAccount userAccount, RecurrentConfig recurrentConfig, String transCat){

        OfficeUserTransferDTO officeUserTransferDTO = new OfficeUserTransferDTO();
        officeUserTransferDTO.setAmount(recurrentConfig.getAmount());
        officeUserTransferDTO.setCustomerCreditAccount(userAccount.getAccountNo());
        officeUserTransferDTO.setOfficeDebitAccount(recurrentConfig.getOfficialAccountNumber());
        officeUserTransferDTO.setPaymentReference(generatePaymentTransactionId());
        officeUserTransferDTO.setTranCrncy("NGN");
        officeUserTransferDTO.setTranNarration(transCat+ " SIMULATED TRANSACTION");
        officeUserTransferDTO.setTranType("LOCAL");

        return officeUserTransferDTO;
    }

    public static String generatePaymentTransactionId() {
        return new SimpleDateFormat("yyyyMMddHHmmssSS").format(new Date());
    }

}
