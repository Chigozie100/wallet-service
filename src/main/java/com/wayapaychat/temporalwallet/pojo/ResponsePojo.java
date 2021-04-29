package com.wayapaychat.temporalwallet.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponsePojo {
    private boolean status;
    private String message;
    private int code;

    

    public static ResponsePojo response(boolean error, String message, int code) {
        return new ResponsePojo(error, message,code);
    }
}
