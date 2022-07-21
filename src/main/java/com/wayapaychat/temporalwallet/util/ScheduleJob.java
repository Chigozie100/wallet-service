package com.wayapaychat.temporalwallet.util;

import com.wayapaychat.temporalwallet.dto.OfficeUserTransferDTO;
import com.wayapaychat.temporalwallet.entity.RecurrentConfig;
import com.wayapaychat.temporalwallet.entity.WalletAccount;
import com.wayapaychat.temporalwallet.entity.WalletNonWayaPayment;
import com.wayapaychat.temporalwallet.enumm.PaymentStatus;
import com.wayapaychat.temporalwallet.repository.RecurrentConfigRepository;
import com.wayapaychat.temporalwallet.repository.WalletAccountRepository;
import com.wayapaychat.temporalwallet.repository.WalletNonWayaPaymentRepository;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.service.ConfigService;
import com.wayapaychat.temporalwallet.service.TransAccountService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


@Configuration
@EnableScheduling
@Slf4j
public class ScheduleJob {

    @Autowired
    WalletNonWayaPaymentRepository walletNonWayaPaymentRepo;

    @Autowired
    WalletAccountRepository walletAccountRepository;

    @Autowired
    RecurrentConfigRepository recurrentConfigRepository;

    @Autowired
    TransAccountService transAccountService;


    @Scheduled(cron = "${job.cron.twelam}")
    public void checkForPending() throws ParseException {
        int checkDays = 30;
        log.info("-----####### START ###### -------");
        List<WalletNonWayaPayment> walletNonWayaPaymentList = walletNonWayaPaymentRepo.findByAllByStatus(PaymentStatus.PENDING);

        log.info("OUTPUT :: {} " + walletNonWayaPaymentList);
        for (WalletNonWayaPayment data: walletNonWayaPaymentList){
            WalletNonWayaPayment payment = walletNonWayaPaymentRepo.findById(data.getId()).orElse(null);

            SimpleDateFormat myFormat = new SimpleDateFormat("MM/dd/yyyy");

            LocalDateTime localDateTime = payment.getCreatedAt();
            Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
            Date date2 = Date.from(instant);
            String dateString2 = myFormat.format(date2);

            long daysBetween = 0;
            Date createdDate = myFormat.parse(dateString2);

            String dateString = myFormat.format(new Date());
            Date today = myFormat.parse(dateString);

            long difference = today.getTime() - createdDate.getTime();
            daysBetween = (difference / (1000*60*60*24));
            log.info(checkDays + "-----####### Transaction upto 30 days ###### -------" + daysBetween);
            if(checkDays == daysBetween  || daysBetween > checkDays && payment.getStatus().equals(PaymentStatus.PENDING)){
                log.info("-----####### Transaction upto 30 days ###### -------");
                payment.setStatus(PaymentStatus.EXPIRED);
                walletNonWayaPaymentRepo.save(payment);
                log.info("-----####### END: record Updated ###### -------" + payment);
            }else{
                log.info("-----####### END: NOT FOUND ###### -------");
            }

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
//                    List<WalletAccount> userAccount = walletAccountRepository.findBySimulatedAccount();
//
//                    for(WalletAccount data: userAccount){
//                        OfficeUserTransferDTO transfer = getOfficeUserTransferDTO(data, recurrentConfig, "");
//                        response =  transAccountService.OfficialUserTransfer(null, transfer);
//                        objectArrayList.add(response);
//                    }

                    recurrentConfig.setPayDate(getNextMonth(recurrentConfig.getPayDate()));
                }else if (recurrentConfig.getPayDate().compareTo(new Date()) == 0 && recurrentConfig.getDuration().equals(RecurrentConfig.Duration.YEAR)){
                    processPayment(response, objectArrayList,recurrentConfig);
                    recurrentConfig.setPayDate(getNextYear(recurrentConfig.getPayDate()));
                }
            }
            log.info(String.valueOf(objectArrayList));
            recurrentConfigRepository.save(recurrentConfig);

//            if (recurrentConfig.getDuration().equals(RecurrentConfig.Duration.MONTH)){
//                recurrentConfig.setPayDate(getNextMonth(recurrentConfig.getPayDate()));
//            }
//            if (recurrentConfig.getDuration().equals(RecurrentConfig.Duration.YEAR)){
//                recurrentConfig.setPayDate(getNextYear(recurrentConfig.getPayDate()));
//            }
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

        for(WalletAccount data: userAccount){
            OfficeUserTransferDTO transfer = getOfficeUserTransferDTO(data, recurrentConfig, "");
            response =  transAccountService.OfficialUserTransfer(null, transfer);
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


    public void syncSimulatedUsers(){

    }

}
