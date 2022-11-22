package com.wayapaychat.temporalwallet.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class AutoCreateAccount {

    @NotNull
    @Size(min=5, max=50)
    private String codeName;

    @NotNull
    @Size(min=5, max=50)
    private String codeDesc;

    @NotNull
    @Size(min=3, max=10)
    private String codeValue;

    private String codeSymbol;


    @NotNull
    private boolean chargeCustomer;

    @NotNull
    private boolean chargeWaya;


    @NotNull
    @Size(min=3, max=5)
    private String crncyCode;

    @NotNull
    @Size(min=6, max=50)
    private String eventId;


    @NotNull
    @Size(min=10, max=50)
    private String tranNarration;
}
