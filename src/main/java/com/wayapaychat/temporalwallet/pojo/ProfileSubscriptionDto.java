package com.wayapaychat.temporalwallet.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProfileSubscriptionDto {
    private String profileId;
    private String profileUserId;
    private boolean defaultAccount;
    private String permissionId;
    private String accessType;
    private boolean corporate;
    private List<String> profilePermissions;
    private BigDecimal transactionLimit = BigDecimal.ZERO;
}
