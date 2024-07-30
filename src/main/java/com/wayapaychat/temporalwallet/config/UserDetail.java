package com.wayapaychat.temporalwallet.config;


import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
public class UserDetail {

    private Long id;
    private String email;
    private String phoneNumber;
    private String referenceCode;
    private String firstName;
    private String surname;
    private String password;
    private String phoneVerified;
    private String emailVerified;
    private String pinCreated;
    private List<String> roles;
    private boolean corporate;

}