package com.wayapaychat.temporalwallet.dto;

import lombok.Data;

@Data
public class UserProfileResponse {
    private String timestamp;
    private String message;
    private boolean status;
    private ProfileDetails data;
}
