package com.wayapaychat.temporalwallet.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Set;

@Data
public class NonWayaTransferExcelDTO {
    @NotEmpty(message= "List Should Not be Empty")
    private Set<@Valid NoneWayaPaymentRequest> transfer;

    public NonWayaTransferExcelDTO(
            @NotEmpty(message = "List Should Not be Empty") Set<@Valid NoneWayaPaymentRequest> transfer) {
        super();
        this.transfer = transfer;
    }


}
