package com.wayapaychat.temporalwallet.pojo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class AppendToVirtualAccount {
    @NotEmpty(message = "accountNumber cannot be null")
    private String accountNumber;
    @NotEmpty(message = "accountNumber cannot be null")
    private String appendName;
}
