package com.wayapaychat.temporalwallet.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Data
public class BulkNonWayaTransferExcelDTO {
    @NotEmpty(message= "List Should Not be Empty")
    private Set<@Valid NonWayaPaymentMultipleOfficialDTO> transfer;

    public BulkNonWayaTransferExcelDTO(
            @NotEmpty(message = "List Should Not be Empty") Set<@Valid NonWayaPaymentMultipleOfficialDTO> transfer) {
        super();
        this.transfer = transfer;
    }



}
