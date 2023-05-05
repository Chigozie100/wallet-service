package com.wayapaychat.temporalwallet.pojo.signupKafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class UserDataDto {
    private long id;
    private String firstName;
    private String surname;
    private String name;
    private String email;
    private String phoneNumber;
    private String referenceCode;
    private boolean isCorporate;
    private boolean isAdmin;
    private String regDeviceType;
    private String regDeviceIP;
    private String merchantId;
}
