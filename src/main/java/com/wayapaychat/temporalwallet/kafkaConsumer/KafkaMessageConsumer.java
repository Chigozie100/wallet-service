package com.wayapaychat.temporalwallet.kafkaConsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayapaychat.temporalwallet.dto.WalletUserDTO;
import com.wayapaychat.temporalwallet.pojo.signupKafka.SignUpDto;
import com.wayapaychat.temporalwallet.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j @RequiredArgsConstructor
public class KafkaMessageConsumer {

    private static final String WAYA_GROUP = "waya";
    public static final String PROCESS_OTHER_ACCOUNT = "process-registration";

    @Autowired
    private ObjectMapper objectMapper;
    private final UserAccountService userAccountService;

    @KafkaListener(topics = PROCESS_OTHER_ACCOUNT, groupId = WAYA_GROUP)
    public void processUserAccountRegistration(String message){
        log.info("::::::ABOUT TO PROCESS USER ACCT REG:::: {}",message);

    }

    private void processMessage(String message){
        try {
            SignUpDto data = objectMapper.convertValue(message,SignUpDto.class);
            log.info("::::DATA MAPPED TO OBJECT FOR PROCESSING:: {}",data);
            if(data.getUser().isCorporate()){
                log.info("::::ABOUT TO PROCESS CORPORATE ACCOUNT REGISTRATION:: {}",data);
                WalletUserDTO request = new WalletUserDTO();

                ResponseEntity<?> corporateReq = userAccountService.createUserAccount(request);
            }else {
                log.info("::::ABOUT TO PROCESS NORMAL ACCOUNT REGISTRATION:: {}",data);
            }
        }catch (Exception ex){
            log.error(":::AN ERROR PROCESSING ACCOUNT REGISTRATION: {}",ex.getLocalizedMessage());
            log.error(":::AN ERROR PROCESSING ACCOUNT REGISTRATION::CAUSE:: {}",ex.getCause());
        }
    }
}
