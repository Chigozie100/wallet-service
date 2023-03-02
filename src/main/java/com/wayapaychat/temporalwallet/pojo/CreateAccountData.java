package com.wayapaychat.temporalwallet.pojo;

import lombok.Data;

@Data
public class CreateAccountData {
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String email;
    private String accountNumber;
    private String accountName;
    private String product;
}
