package com.wayapaychat.temporalwallet.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountResponse {

    private long id;
    private String accountNo;
    private String accountName;
    private long userId;

}
