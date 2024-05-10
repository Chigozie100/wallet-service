package com.wayapaychat.temporalwallet.pojo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


@Data
public class VirtualAccountRequest {
    @Size(min=1, max=50, message = "The account name '${validatedValue}' must be between {min} and {max} characters long")
    @NotBlank(message = "Account Name must not be null")
    private String accountName;

    private String userId;

    @NotBlank(message = "Account Name must not be null")
    private String phoneNumber;

    @NotBlank(message = "Account Name must not be null")
    private String email;

    private String bvn;
    private String nin;

}
