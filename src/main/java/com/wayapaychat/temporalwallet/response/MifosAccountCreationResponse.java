package com.wayapaychat.temporalwallet.response;

import lombok.Data;

@Data
public class MifosAccountCreationResponse {
    private String resourceId;
    private String responseCode;
    private String responseDescription;
    private String clientId;
    private String accountNo;

}
