package com.wayapaychat.temporalwallet.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class UpdateEventChargeDTO {

    private BigDecimal taxAmt;

    @NotNull
    private BigDecimal tranAmt;

    @NotNull
    @Size(min=3, max=5)
    private String crncyCode;

    @NotNull
    private boolean taxable;

    @NotNull
    private boolean chargeCustomer;

    @NotNull
    private boolean chargeWaya;
}
