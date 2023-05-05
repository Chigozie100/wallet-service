package com.wayapaychat.temporalwallet.pojo.signupKafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessDataDto {
    private String organisationName;
    private String organisationEmail;
    private String organisationPhone;
    private String organizationCity;
    private String organizationAddress;
    private String organizationState;
    private String organisationType;
    private String businessType;
}
