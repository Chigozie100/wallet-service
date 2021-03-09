package com.wayapaychat.temporalwallet.util;

import static com.wayapaychat.temporalwallet.util.Constant.SUCCESS_MESSAGE;

public class SuccessResponse extends ResponseHelper {

    public SuccessResponse(String message, Object data){
        super(true, message, data);
    }

    public SuccessResponse(Object data){
        super(true, SUCCESS_MESSAGE, data);
    }

}
