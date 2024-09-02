package com.wayapaychat.temporalwallet.service;

import com.wayapaychat.temporalwallet.config.DynamicUrlInterceptor;
import com.wayapaychat.temporalwallet.dto.TransferTransactionDTO;
import com.wayapaychat.temporalwallet.entity.Transactions;
import com.wayapaychat.temporalwallet.entity.VirtualAccountSettings;
import com.wayapaychat.temporalwallet.entity.WebhookLogs;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.pojo.WebhookPayload;
import com.wayapaychat.temporalwallet.proxy.WebhookClient;
import com.wayapaychat.temporalwallet.repository.VirtualAccountSettingRepository;
import com.wayapaychat.temporalwallet.repository.WebhookLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class WebhookService {

    private final VirtualAccountSettingRepository virtualAccountSettingRepository;
    private final WebhookLogRepository webhookLogRepository;
    private final DynamicUrlInterceptor dynamicUrlInterceptor;
    private final WebhookClient webhookClient;


    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 5000))
    public void sendWebhookNotification(WebhookPayload payload, TransferTransactionDTO transaction) {
        VirtualAccountSettings virtualAccountSettings = getWebhookDetails(transaction.getBenefAccountNumber());
        if(virtualAccountSettings == null || virtualAccountSettings.getCallbackUrl() ==null || virtualAccountSettings.getCallbackUrl().isEmpty()){
            throw new RuntimeException("No webhook URL found for merchant ID: " + payload.getMerchantId());
        }
        String webhookUrl = virtualAccountSettings.getCallbackUrl();
        try {

            dynamicUrlInterceptor.setDynamicUrl(webhookUrl);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.valueOf("application/json"));

            ResponseEntity<String> response = webhookClient.sendWebhookNotification(httpHeaders, payload);

            if (response.getStatusCode().is2xxSuccessful()) {
                // Log success
                logWebhookSuccess(payload, webhookUrl, response.getStatusCodeValue(), response.getBody());
            } else {
                // Log failure
                logWebhookFailure(payload, webhookUrl, response.getStatusCodeValue(), response.getBody());
                throw new RuntimeException("Failed to send webhook: Non-2xx response");
            }

        } catch (Exception e) {
            // Log failure and retry
            logWebhookFailure(payload, webhookUrl, HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
            throw e; // This will trigger the retry
        }
    }

    private void logWebhookSuccess(WebhookPayload transaction, String webhookUrl, int status, String responseBody) {
        WebhookLogs log = new WebhookLogs();
        log.setMerchantId(transaction.getMerchantId()); //transaction.getMerchantId()
        log.setTransactionId(Long.parseLong(transaction.getTransactionId()));
        log.setWebhookUrl(webhookUrl);
        log.setResponseStatus(status);
        log.setResponseBody(responseBody);
        log.setAttempts(1); // Update this in case of retries
        webhookLogRepository.save(log);
    }

    private void logWebhookFailure(WebhookPayload transaction, String webhookUrl, int status, String responseBody) {
        WebhookLogs log = new WebhookLogs();
        log.setMerchantId(transaction.getMerchantId()); //transaction.getMerchantId()
        log.setTransactionId(Long.parseLong(transaction.getTransactionId()));
        log.setWebhookUrl(webhookUrl);
        log.setResponseStatus(status);
        log.setResponseBody(responseBody);
        log.setAttempts(1); // Update this in case of retries
        webhookLogRepository.save(log);
    }

    private VirtualAccountSettings getWebhookDetails(String accountNo){
       Optional<VirtualAccountSettings> virtualAccountSettings =
               virtualAccountSettingRepository.findByAccountNo(accountNo);

       if(!virtualAccountSettings.isPresent())
           throw new CustomException("Error", HttpStatus.EXPECTATION_FAILED);
       return virtualAccountSettings.get();

    }


}
