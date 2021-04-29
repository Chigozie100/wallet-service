package com.wayapaychat.temporalwallet.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletStatus {

	private Long id;
    private String code;
    private String value;
    private boolean submittedAndPendingApproval;
    private boolean approved;
    private boolean rejected;
    private boolean withdrawnByApplicant;
    private boolean active;
    private boolean closed;
}
