package com.wayapaychat.temporalwallet.pojo.signupKafka;

import lombok.*;

@Data
public class SignUpDto {
    private UserDataDto user;
    private ProfileDataDto profile;
    private String token;
}
