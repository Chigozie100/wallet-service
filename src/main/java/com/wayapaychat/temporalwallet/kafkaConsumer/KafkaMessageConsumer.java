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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j @RequiredArgsConstructor
public class KafkaMessageConsumer {

    public static final String WAYA_GROUP = "wayarepository";
    public static final String PROCESS_OTHER_ACCOUNT = "process-registration";
    public static final String KYC_LIMIT_SETUP = "kyc-limit-setup";
    private final WalletUserRepository walletUserRepository;

    @Autowired
    private ObjectMapper objectMapper;
    private final UserAccountService userAccountService;

    @KafkaListener(topics = PROCESS_OTHER_ACCOUNT, groupId = WAYA_GROUP)
    public void processUserAccountRegistration(String message){
        log.debug("::::::ABOUT TO PROCESS USER ACCT REG:::: {}",message);
        processAccountRegMessage(message);
        log.debug("::::::FINISH PROCESSING USER ACCT REG:::: {}",message);
    }

    @KafkaListener(topics = KYC_LIMIT_SETUP, groupId = WAYA_GROUP)
    public void processKycMessage(String message){
        log.debug("::::ABOUT TO PROCESS KYC DEBIT LIMIT ON USER ACCOUNT::: {}",message);
        processKycData(message);
        log.debug("::::FINISH PROCESSING KYC DEBIT LIMIT ON USER ACCOUNT::: {}",message);
    }


    private void processKycData(String message)  {
        try {
            KycTierDataDto kycTierDataDto = objectMapper.readValue(message,KycTierDataDto.class);
            log.debug("::::CONVERT MESSAGE TO KYC DTO CLASS::: {}",kycTierDataDto);
//            kycTierDataDto.getTierName().equalsIgnoreCase("TIER_2")
            if(kycTierDataDto.getOrderLevel() > 0 && kycTierDataDto.getProfileId() == null){

                List<WalletUser> walletUserList = walletUserRepository.findAllWalletByUserId(kycTierDataDto.getUserId());
                if(walletUserList.size() > 0 && walletUserList.size() == 1){
                    List<WalletUser> walletAccts = new ArrayList<>();
                    for (WalletUser walletUser: walletUserList){
                        walletUser.setCust_debit_limit(kycTierDataDto.getDailyTransactionLimit().doubleValue());
                        walletUser.setOneTimeTransactionLimit(kycTierDataDto.getOneTimeTransactionLimit().doubleValue());
                        walletAccts.add(walletUser);
                    }
                    walletUserRepository.saveAllAndFlush(walletAccts);
                    log.info("::::SUCCESSFUL UPDATED CUSTOMER DEBIT LIMIT::::LIMIT {}, USERID {}",kycTierDataDto.getDailyTransactionLimit().doubleValue(),kycTierDataDto.getUserId());
                }

            } else {

                Optional<WalletUser> walletUser = walletUserRepository.findUserIdAndProfileId(kycTierDataDto.getUserId(), kycTierDataDto.getProfileId());
                if(walletUser.isPresent()){
                    walletUser.get().setCust_debit_limit(kycTierDataDto.getDailyTransactionLimit().doubleValue());
                    walletUser.get().setOneTimeTransactionLimit(kycTierDataDto.getOneTimeTransactionLimit().doubleValue());
                    walletUserRepository.save(walletUser.get());
                    log.info("::::SUCCESSFUL UPDATED CUSTOMER DEBIT LIMIT::::LIMIT {}, USERID {}",kycTierDataDto.getDailyTransactionLimit().doubleValue(),kycTierDataDto.getUserId());
                    log.info(":::FINISH PROCESSING CUST_DEBIT_LIMIT::::LIMIT {}, TIERNAME {}, USERID {}",
                            kycTierDataDto.getUserId(), kycTierDataDto.getTierName(),kycTierDataDto.getUserId());
                }else {
                    log.debug("::::USER WALLET NOT FOUND {}",kycTierDataDto);
                    log.info("::::UNABLE TO UPDATE CUST_DEBIT_LIMIT {}",kycTierDataDto);
                }
            }
        }catch (Exception ex){
            log.error(":::::ERR ProcessKycData {}",ex.getLocalizedMessage());
            log.error(":::::ERR-CAUSE ProcessKycData {}",ex.getCause());
        }
    }


    private void processAccountRegMessage(String message){
        try {
            RegistrationDataDto data = objectMapper.readValue(message,RegistrationDataDto.class);
            log.debug("::::DATA MAPPED TO OBJECT FOR PROCESSING:: {} ",data);
            if(data.isCorporate()){
                log.debug("::::ABOUT TO PROCESS CORPORATE ACCOUNT REGISTRATION:: {} ",data);
                WalletUserDTO walletUserDtoRequest = getWalletUserDto(data);
                log.debug("::::MAPPED DATA:: {} ",walletUserDtoRequest);
                CompletableFuture.runAsync(() -> {
                    ResponseEntity<?> corporateReq = userAccountService.createUserAccount(walletUserDtoRequest.getClientId(), walletUserDtoRequest.getClientType(), walletUserDtoRequest, data.getToken());
                    log.debug(":::::PROCESS KAFKA Corporate Acct Response {} ",corporateReq);
                });
               log.debug("::::FINISH PROCESSING CORPORATE ACCT REG::::: {}",walletUserDtoRequest);
            }else {
                log.debug("::::ABOUT TO PROCESS NORMAL ACCOUNT REGISTRATION:: {}",data);
                WalletUserDTO walletUserDtoRequest = getWalletUserDto(data);
                log.debug("::::MAPPED DATA {}",walletUserDtoRequest);
                CompletableFuture.runAsync(() -> {
                    ResponseEntity<?> corporateReq = userAccountService.createUserAccount(walletUserDtoRequest.getClientId(), walletUserDtoRequest.getClientType(),walletUserDtoRequest, data.getToken());
                    log.debug(":::::PROCESS KAFKA Nor Acct Resp {}",corporateReq);
                });
                log.debug("::::FINISH PROCESSING NORMAL ACCT REG::::: {}",walletUserDtoRequest);
            }
        }catch (Exception ex){
            log.error(":::AN ERROR PROCESSING ACCOUNT REGISTRATION: {}",ex.getLocalizedMessage());
            log.error(":::AN ERROR PROCESSING ACCOUNT REGISTRATION::CAUSE:: {}",ex.getCause());
        }
    }


    public WalletUserDTO getWalletUserDto(RegistrationDataDto signUpDto){
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
        walletUserDTO.setDescription("");
        walletUserDTO.setAccountType("");
        walletUserDTO.setProfileId(signUpDto.getProfileId());
        walletUserDTO.setProfileType(signUpDto.getProfileType());
        walletUserDTO.setClientId(signUpDto.getClientId());
        walletUserDTO.setClientType(signUpDto.getClientType());
        return walletUserDTO;
    }
}
