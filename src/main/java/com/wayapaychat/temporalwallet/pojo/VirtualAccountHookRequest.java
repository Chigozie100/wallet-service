package com.wayapaychat.temporalwallet.pojo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class VirtualAccountHookRequest {
    private String bank;
    private String bankCode;

    @NotBlank(message = "callbackUrl cannot be null")
    private String callbackUrl;

    @NotBlank(message = "accountNo cannot be null")
    private String accountNo;
    private String email;
    private String merchantId;
}
