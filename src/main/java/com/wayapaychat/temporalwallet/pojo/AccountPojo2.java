package com.wayapaychat.temporalwallet.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class AccountPojo2 {
    private Long userId;
    private boolean isSimulated;
    private String accountType;
    private String description;
    @NotBlank(message = "Profile Id can not be Blank or Null")
    private String profileId;


}
