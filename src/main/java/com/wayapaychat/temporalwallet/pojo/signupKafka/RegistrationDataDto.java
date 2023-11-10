package com.wayapaychat.temporalwallet.pojo.signupKafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data @AllArgsConstructor
public class RegistrationDataDto {
    private String userId;
    private String firstName;
    private String surname;
    private String middleName;
    private String name;
    private String email;
    private String phoneNumber;
    private String referenceCode;
    private boolean corporate;
    private String regDeviceType;
    private String regDeviceIP;
    private String merchantId;
    private String dateOfBirth;
    private String referral;
    private String gender;
    private String address;
    private String city;
    private String state;

    private String organisationName;
    private String organisationEmail;
    private String organisationPhone;
    private String organizationCity;
    private String organizationAddress;
    private String organizationState;
    private String organisationType;
    private String businessType;

    private String token;

    private String profileId;
    private String profileType;
    private String clientId;
    private String clientType;
    public RegistrationDataDto() {
        super();
    }

}
