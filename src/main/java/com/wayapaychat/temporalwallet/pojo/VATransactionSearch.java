package com.wayapaychat.temporalwallet.pojo;

import lombok.Data;

@Data
public class VATransactionSearch {
    private String accountNo;
    private String startDate;
    private String endDate;
}
