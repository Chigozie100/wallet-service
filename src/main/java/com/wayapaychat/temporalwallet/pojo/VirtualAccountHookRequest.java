package com.wayapaychat.temporalwallet.pojo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class VirtualAccountHookRequest {

    @NotBlank(message = "callbackUrl cannot be null")
    private String callbackUrl;

    @NotBlank(message = "accountNo cannot be null")
    private String accountNo;

    @NotBlank(message = "email cannot be null")
    private String email;

    @NotBlank(message = "businessId cannot be null")
    private String businessId;
}
