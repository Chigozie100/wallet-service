package com.wayapaychat.temporalwallet.pojo.signupKafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ProfileDataDto {
    private String firstName;
    private String surname;
    private String middleName;
    private String phoneNumber;
    private String email;
    private String referral;
    private String gender;
    private String address;
    private String city;
    private String state;
    private boolean corporate;
    private BusinessDataDto otherDetails;
}
