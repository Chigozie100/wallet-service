package com.wayapaychat.temporalwallet.pojo.signupKafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data @AllArgsConstructor @NoArgsConstructor
public class InWardDataDto {
    private String accountNumber;
    private String nubanAccountNumber;
    private String accountName;
    private String accountType;
    private String emailAddress;
    private String mobileNo;
    private Long walletUserId;
    private Long walletAccountId;
    private Long userId;
    private boolean deleteFlag;
    private boolean closeFlag;
    private boolean walletDefault;

}
