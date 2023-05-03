package com.wayapaychat.temporalwallet.dto;

import lombok.Data;

@Data
public class OtherDetails {
    private String organisationName;
    private String organisationEmail;
    private String organisationPhone;
    private String organizationAddress;
    private String organisationType;
    private String businessType;
    private boolean phoneVerified;
    private boolean emailVerified;
}
