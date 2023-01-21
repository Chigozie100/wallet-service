
package com.wayapaychat.temporalwallet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wayapaychat.temporalwallet.enumm.ExternalCBAResponseCodes;

import java.io.Serializable;
/**
 *
 * @author dynamo
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@lombok.Data
public class ExternalCBAAccountDetailsResponse extends ExternalCBAResponse implements Serializable{

    private String accountNumber;

    private String productName;

    private String ledgerBalance;
    
    private String availableBalance;

    private String accountName;

    private String currency;
    
    public ExternalCBAAccountDetailsResponse(ExternalCBAResponseCodes responseCodeEnum) {
       super(responseCodeEnum);
    }
}
