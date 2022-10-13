package com.wayapaychat.temporalwallet.notification;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class ResponseObj<T> {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+1")
    private Date timeStamp = new Date();
    public boolean status;
    public String message;
    public T data;


    public ResponseObj(boolean status, String message, T data) {
        super();
        this.status = status;
        this.message = message;
        this.data = data;
    }

}
