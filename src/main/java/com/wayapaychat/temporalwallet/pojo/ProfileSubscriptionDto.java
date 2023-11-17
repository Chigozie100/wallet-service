package com.wayapaychat.temporalwallet.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Data @JsonIgnoreProperties(ignoreUnknown = true) @AllArgsConstructor @NoArgsConstructor @ToString
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
