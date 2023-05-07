package com.wayapaychat.temporalwallet.pojo.signupKafka;

import lombok.*;

@Data @Builder @AllArgsConstructor @NoArgsConstructor @ToString
public class SignUpDto {
    private UserDataDto user;
    private ProfileDataDto profile;
    private String token;
}
