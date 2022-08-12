package com.wayapaychat.temporalwallet.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class AccountBlockDTO {
    @NotBlank(message = "Account must not be Null or Blank")
    @Size(min=10, max=10, message = "Account must be 10 digit")
    private String customerAccountNo;

    private boolean isBlock = true;
}
