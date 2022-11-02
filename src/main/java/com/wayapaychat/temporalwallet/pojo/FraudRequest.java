package com.wayapaychat.temporalwallet.pojo;

import lombok.Data;

@Data
public class FraudRequest {

    private String name;
    private String description;
    private String accountType;
}
