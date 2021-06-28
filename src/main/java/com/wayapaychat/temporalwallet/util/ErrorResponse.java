package com.wayapaychat.temporalwallet.util;

import org.apache.logging.log4j.util.Strings;

import static com.wayapaychat.temporalwallet.util.Constant.ERROR_MESSAGE;

public class ErrorResponse extends  ResponseHelper {

    public ErrorResponse(String message){
        super(false, message, Strings.EMPTY);
    }

    public ErrorResponse(){
        super(false, ERROR_MESSAGE, Strings.EMPTY);
    }

}
