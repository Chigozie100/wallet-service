package com.wayapaychat.temporalwallet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data @AllArgsConstructor @NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountUserDetail {
    private String accountNo;
    private String accountName;
    private String currencyCode;
    private boolean accountDefault;
    private String nubanAccountNo;
    private String email;
    private String phoneNumber;
    private Long userId;
    private String profileId;
}
