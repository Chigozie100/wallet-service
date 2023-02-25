package com.wayapaychat.temporalwallet.response;

import com.wayapaychat.temporalwallet.enumm.ExternalCBAResponseCodes;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExternalCBAAccountCreationResponse {
    private String resourceId;
    private String responseCode;
    private String responseDescription;
    private String clientId;
    private String accountNo;

    public ExternalCBAAccountCreationResponse(ExternalCBAResponseCodes responseCodeEnum) {
        this.responseCode = responseCodeEnum.getRespCode();
        this.responseDescription = responseCodeEnum.getRespDescription();
    }

}
