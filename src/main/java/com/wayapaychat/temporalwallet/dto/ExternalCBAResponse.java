
package com.wayapaychat.temporalwallet.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.wayapaychat.temporalwallet.enumm.ExternalCBAResponseCodes;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *
 * @author dynamo
 */
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data 
public class ExternalCBAResponse implements Serializable {
 
    private String transactionRef;
 
    private String resourceId;
 
    private String responseCode;
 
    private String responseDescription;

    @Temporal(TemporalType.TIMESTAMP) 
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private Date responseTime;

    public ExternalCBAResponse(ExternalCBAResponseCodes responseCodeEnum) {
        this.responseCode = responseCodeEnum.getRespCode();
        this.responseDescription = responseCodeEnum.getRespDescription();
    }

}
