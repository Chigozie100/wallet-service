package com.wayapaychat.temporalwallet.dto;

import lombok.Data;

@Data
public class ProfileDetails {
    private String id;
    private String email;
    private String firstName;
    private String surname;
    private String dateOfBirth;
    private String gender;
    private String phoneNumber;
    private String userId;
    private String referral;
    private String referenceCode;
    private boolean smsAlertConfig;
    private boolean corporate;
    private OtherDetails otherDetails;
}
