package com.wayapaychat.temporalwallet.util;

import com.wayapaychat.temporalwallet.entity.WalletNonWayaPayment;
import com.wayapaychat.temporalwallet.enumm.PaymentStatus;
import com.wayapaychat.temporalwallet.repository.WalletNonWayaPaymentRepository;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;


@Configuration
@EnableScheduling
@Slf4j
public class ScheduleJob {

    @Autowired
    WalletNonWayaPaymentRepository walletNonWayaPaymentRepo;


    @Scheduled(cron = "${job.cron.nonewaya}")
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
            if(checkDays == daysBetween && payment.getStatus().equals(PaymentStatus.PENDING)){
                log.info("-----####### Transaction upto 30 days ###### -------");
                payment.setStatus(PaymentStatus.EXPIRED);
                walletNonWayaPaymentRepo.save(payment);
                log.info("-----####### END: record Updated ###### -------" + payment);
            }else{
                log.info("-----####### END: NOT FOUND ###### -------");
            }

        }

    }

}
