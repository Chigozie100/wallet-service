package com.wayapaychat.temporalwallet.pojo;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LogMessage {

    private int httpStatus;
    private String httpMethod;
    private String path;
    private String clientIP;
    private String javaMethod;
    private String response;
    private long timeTakenMs;
    private String requestBody;
    private String requestParams;

}
