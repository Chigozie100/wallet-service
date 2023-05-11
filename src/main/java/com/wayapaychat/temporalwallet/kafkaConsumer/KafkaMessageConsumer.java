package com.wayapaychat.temporalwallet.kafkaConsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayapaychat.temporalwallet.dto.WalletUserDTO;
import com.wayapaychat.temporalwallet.dto.kyc.KycTierDataDto;
import com.wayapaychat.temporalwallet.entity.WalletUser;
import com.wayapaychat.temporalwallet.pojo.signupKafka.RegistrationDataDto;
import com.wayapaychat.temporalwallet.repository.WalletUserRepository;
import com.wayapaychat.temporalwallet.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j @RequiredArgsConstructor
public class KafkaMessageConsumer {

    public static final String WAYA_GROUP = "waya";
    public static final String PROCESS_OTHER_ACCOUNT = "process-registration";
    public static final String KYC_LIMIT_SETUP = "kyc-limit-setup";
    private final WalletUserRepository walletUserRepository;

    @Autowired
    private ObjectMapper objectMapper;
    private final UserAccountService userAccountService;

    @KafkaListener(topics = PROCESS_OTHER_ACCOUNT, groupId = WAYA_GROUP)
    public void processUserAccountRegistration(String message){
        log.info("::::::ABOUT TO PROCESS USER ACCT REG:::: {}",message);
        processAccountRegMessage(message);
        log.info("::::::FINISH PROCESSING USER ACCT REG:::: {}",message);
    }

    @KafkaListener(topics = KYC_LIMIT_SETUP, groupId = WAYA_GROUP)
    public void processKycMessage(String message){
        log.info("::::ABOUT TO PROCESS KYC DEBIT LIMIT ON USER ACCOUNT::: {}",message);
        processKycData(message);
        log.info("::::FINISH PROCESSING KYC DEBIT LIMIT ON USER ACCOUNT::: {}",message);
    }


    private void processKycData(String message)  {
        try {
            KycTierDataDto kycTierDataDto = objectMapper.readValue(message,KycTierDataDto.class);
            log.info("::::CONVERT MESSAGE TO KYC DTO CLASS::: {}",kycTierDataDto);
            Optional<WalletUser> walletUser = walletUserRepository.findUserId(kycTierDataDto.getUserId());
            if(walletUser.isPresent()){
                walletUser.get().setCust_debit_limit(kycTierDataDto.getDailyTransactionLimit().doubleValue());
                walletUserRepository.save(walletUser.get());
                log.info("::::SUCCESSFUL UPDATED CUSTOMER DEBIT LIMIT::::LIMIT {}, USERID {}",kycTierDataDto.getDailyTransactionLimit().doubleValue(),kycTierDataDto.getUserId());
                log.info(":::FINISH PROCESSING CUST_DEBIT_LIMIT::::LIMIT {}, TIERNAME {}, USERID {}",
                        kycTierDataDto.getUserId(), kycTierDataDto.getTierName(),kycTierDataDto.getUserId());
            }else {
                log.info("::::USER WALLET NOT FOUND {}",kycTierDataDto);
                log.info("::::UNABLE TO UPDATE CUST_DEBIT_LIMIT {}",kycTierDataDto);
            }
        }catch (Exception ex){
            log.error(":::::ERR ProcessKycData {}",ex.getLocalizedMessage());
            log.error(":::::ERR-CAUSE ProcessKycData {}",ex.getCause());
        }
    }

    private void processAccountRegMessage(String message){
        try {
            RegistrationDataDto data = objectMapper.readValue(message,RegistrationDataDto.class);
            log.info("::::DATA MAPPED TO OBJECT FOR PROCESSING:: {}",data);
            if(data.isCorporate()){
                log.info("::::ABOUT TO PROCESS CORPORATE ACCOUNT REGISTRATION:: {}",data);
                WalletUserDTO walletUserDtoRequest = getWalletUserDto(data);
                log.info("::::MAPPED DATA {}",walletUserDtoRequest);
                CompletableFuture.runAsync(() -> {
                    ResponseEntity<?> corporateReq = userAccountService.createUserAccount(walletUserDtoRequest, data.getToken());
                    log.info(":::::PROCESS KAFKA Corporate Acct Resp {}",corporateReq);
                });
               log.info("::::FINISH PROCESSING CORPORATE ACCT REG::::: {}",walletUserDtoRequest);
            }else {
                log.info("::::ABOUT TO PROCESS NORMAL ACCOUNT REGISTRATION:: {}",data);
                WalletUserDTO walletUserDtoRequest = getWalletUserDto(data);
                log.info("::::MAPPED DATA {}",walletUserDtoRequest);
                CompletableFuture.runAsync(() -> {
                    ResponseEntity<?> corporateReq = userAccountService.createUserAccount(walletUserDtoRequest, data.getToken());
                    log.info(":::::PROCESS KAFKA Nor Acct Resp {}",corporateReq);
                });
                log.info("::::FINISH PROCESSING NORMAL ACCT REG::::: {}",walletUserDtoRequest);
            }
        }catch (Exception ex){
            log.error(":::AN ERROR PROCESSING ACCOUNT REGISTRATION: {}",ex.getLocalizedMessage());
            log.error(":::AN ERROR PROCESSING ACCOUNT REGISTRATION::CAUSE:: {}",ex.getCause());
        }
    }


    private WalletUserDTO getWalletUserDto(RegistrationDataDto signUpDto){
        WalletUserDTO walletUserDTO = new WalletUserDTO();
        if(signUpDto.isCorporate()){
            walletUserDTO.setFirstName(signUpDto.getOrganisationName());
            walletUserDTO.setLastName("");
            walletUserDTO.setCorporate(true);
            walletUserDTO.setEmailId(signUpDto.getOrganisationEmail());
            walletUserDTO.setMobileNo(signUpDto.getOrganisationPhone());
        }else {
            walletUserDTO.setFirstName(signUpDto.getFirstName());
            walletUserDTO.setLastName(signUpDto.getSurname());
            walletUserDTO.setCorporate(false);
            walletUserDTO.setEmailId(signUpDto.getEmail());
            walletUserDTO.setMobileNo(signUpDto.getPhoneNumber());
        }
        walletUserDTO.setUserId(Long.valueOf(signUpDto.getUserId()));
        walletUserDTO.setSolId("0000");
        walletUserDTO.setCustDebitLimit(0.0);
        walletUserDTO.setDescription(null);
        walletUserDTO.setAccountType(null);
        return walletUserDTO;
    }
}
