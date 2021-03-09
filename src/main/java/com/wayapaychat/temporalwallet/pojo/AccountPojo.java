package com.wayapaychat.temporalwallet.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountPojo {

    private long id;
    private String accountNo;
    private String accountName;
    private long userId;

}
