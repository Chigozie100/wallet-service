package com.wayapaychat.temporalwallet.dto;

import lombok.Data;

@Data
public class BillerManagementResponse {
    private Long id;
    private String name;
    private String billerAggregatorCode;
    private String billerWayaPayCode;
    private boolean active;
    private Long categoryId;
    private String categoryName;
    private String aggregatorName;
    private String categoryCode;

}
