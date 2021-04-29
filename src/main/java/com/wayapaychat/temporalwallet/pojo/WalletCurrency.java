package com.wayapaychat.temporalwallet.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WalletCurrency {

	private String code;
    private String name;
    private Integer decimalPlaces;
    private String displaySymbol;
    private String nameCode;
    private String displayLabel;
}
