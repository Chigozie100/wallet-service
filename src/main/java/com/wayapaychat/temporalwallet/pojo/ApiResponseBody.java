package com.wayapaychat.temporalwallet.pojo;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.ToString;

import java.util.Date;

@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponseBody<T> {
    private String timestamp = new Date().toString();
    private String message;
    private boolean status;
    private T data;

    public ApiResponseBody() {
    }

    public ApiResponseBody(T data, String message, boolean status) {
        timestamp = new Date().toString();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public ApiResponseBody(String message, boolean status) {

        timestamp = new Date().toString();
        this.status = status;
        this.message = message;
        this.data = null;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

}

