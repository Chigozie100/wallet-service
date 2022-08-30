package com.wayapaychat.temporalwallet.pojo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


@Data
public class VirtualAccountRequest {
    @Size(min=1, max=50, message = "The account name '${validatedValue}' must be between {min} and {max} characters long")
    @NotBlank(message = "Account Name must not be null")
    private String accountName;

    @NotBlank(message = "User Id must not be null")
    @Size(min=1, max=20, message = "The user id '${validatedValue}' must be between {min} and {max} characters long")
    private String userId;

    private String accountType;

}
