package com.wayapaychat.temporalwallet.dto;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
public class ServiceResponse implements Serializable {
    
    private Date timeStamp = new Date();
    private boolean status;
    private String message;
    private Object data;
    private int dataSize;

    public ServiceResponse() {
    }

    public ServiceResponse(String message, Object data) {
        this.message = message;
        this.data = data;
    }

    public ServiceResponse(Boolean status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }


    public ServiceResponse(Boolean status, String message, Object data, int pageLength) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.dataSize = pageLength;
    }

}
