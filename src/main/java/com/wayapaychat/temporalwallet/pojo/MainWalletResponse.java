package com.wayapaychat.temporalwallet.pojo;

import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MainWalletResponse {

	private Long id;
    private String accountNo;
    private Integer clientId;
    private String clientName;
    private Long savingsProductId;
    private String savingsProductName;
    private Long fieldOfficerId;
    private Double nominalAnnualInterestRate;
    private boolean defaultWallet;
    private WalletStatus status;
    private WalletTimeLine timeline;
    private WalletCurrency currency;
    private WalletSummary summary;
}
