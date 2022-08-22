package com.wayapaychat.temporalwallet.dto;

import com.wayapaychat.temporalwallet.entity.WalletUser;
import lombok.Data;

@Data
public class TransactionCountDto {
    private String userId;
    private Long totalCount;
    private WalletUser user;

    public TransactionCountDto(String userId, Long totalCount) {
        this.userId = userId;
        this.totalCount = totalCount;
    }

    public TransactionCountDto(String userId, Long totalCount, WalletUser user) {
        this.userId = userId;
        this.totalCount = totalCount;
        this.user = user;
    }
}
