package com.wayapaychat.temporalwallet.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wayapaychat.temporalwallet.entity.WalletTransaction;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TransactionsResponse{
	
	private Boolean status;

    private Integer code;

    private String message;

    private List<WalletTransaction> data;

	public TransactionsResponse(Boolean status, Integer code, String message, List<WalletTransaction> data) {
		this.status = status;
		this.code = code;
		this.message = message;
		this.data = data;
	}
	
	public static class Code{

        public static final Integer SUCCESS = 200;
        public static final Integer NOT_FOUND = 404;
        public static final Integer UNKNOWN_ERROR = 500;
        public static final Integer EMPTY_REQUEST = 204;
        public static final Integer CREATED = 201;
        public static final Integer BAD_REQUEST = 400;
        public static final Integer UNAUTHORIZED = 401;
        public static final Integer FORBIDDEN = 403;
	}

}
