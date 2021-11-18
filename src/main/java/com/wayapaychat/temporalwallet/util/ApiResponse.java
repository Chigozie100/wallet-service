package com.wayapaychat.temporalwallet.util;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

import java.io.Serializable;

@SuppressWarnings("serial")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ApiResponse<E> implements Serializable {

    private Boolean status;

    private Integer code;

    private String message;
    private E data;
    


    private ApiResponse(Boolean status, Integer code, String message, E data) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = data;
    }
    

    private ApiResponse(Boolean status, Integer code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = data;
    }

//    public ApiResponse(Boolean status, Integer code, String message, E data, String token) {
//        this.status = status;
//        this.code = code;
//        this.message = message;
//        this.data = data;
//        this.token = token;
//    }
    
   

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public E getData() {
        return data;
    }


    public void setData(E data) {
        this.data = data;
    }

//    public String getToken() {
//        return token;
//    }
//
//    public void setToken(String token) {
//        this.token = token;
//    }


    public static class Builder<E>{


        private Boolean status  = false;

        private Integer code = Code.UNKNOWN_ERROR;

        private String message = "Unknow Error";

//        private String token = null;

        private E data = null;



        public Builder(){
        }

        public Builder setStatus(boolean status){
            this.status = status;
            return this;
        }

        public Builder setCode(Integer code){
            this.code = code;
            return this;
        }

        public Builder setMessage(String message){
            this.message = message;
            return this;
        }

        public Builder setData(E e){
            this.data = e;
            return this;
        }

//        public Builder setToken(String e){
//            this.token = e;
//            return this;
//        }

        public ApiResponse build(){
            return new ApiResponse(this.status, this.code, this.message, this.data);
        }

        public ApiResponse buildSuccess(String message, E e){
            return new ApiResponse(true, Code.SUCCESS, message, e);
        }

        public ApiResponse buildSuccess(String message){
            return new ApiResponse(true, Code.SUCCESS, message);
        }

//        public ApiResponse buildSuccessWithToken(String message, E data, String token) {
//            return new ApiResponse(true, Code.SUCCESS, message, data,token);
//        }
        
        
    }

    public static class Code{

        //General Error 100 - 110
        public static final Integer SUCCESS = 200;
        public static final Integer NOT_FOUND = 404;
        public static final Integer UNKNOWN_ERROR = 500;
        public static final Integer EMPTY_REQUEST = 204;

        //Authentication Error
        public static final Integer UNAUTHORIZED_USER = 101;
        public static final Integer DECLINE = 102;
        public static final Integer BAD_CREDENTIALS = 103;
        public static final Integer INCORRECT_PIN = 104;


        public static final Integer NOT_ALLOWED = 100;
        public static final Integer VALIDATION_ERRORS = 101;
        public static final Integer DUPLICATE_RECORD = 112;



        //Wallet Errors
        public static final Integer  INSUFFICIENT_BALANCE = 300;
        public static final Integer  TRANSACTION_NOT_FOUND = 301;
        public static final Integer  INSUFFICIENT_WAYA_BALANCE = 303;

        //Users Error
        public static final Integer  USER_EMAIL_EXISTS = 420;
        public static final Integer  USER_PHONE_NUMBER_EXISTS = 421;
        public static final Integer  USER_PASSWORD_NOT_MATCH = 422;
        public static final Integer  USER_USERNAME_EXISTS = 423;
        public static final Integer  OLD_PASSWORD_IS_NOT_CORRECT = 424;
        public static final Integer  USER_USERNAME_NOT_FOUND = 425;



        //Token
        public static final Integer  TOKEN_REFRESH_FAILED  = 520;
        public static final Integer  VERIFICATION_OTP_EXPIRED  = 521;
        public static final Integer  RESET_PASSWORD_OTP_INVALID  = 522;
        public static final Integer  RESET_PASSWORD_OTP_EXPIRED  = 523;

        //EMAIL
        public static final Integer  EMAIL_FAILED  = 620;

        //PASSWORD
        public static final Integer  PASSWORD_FAILED  = 720;

        //OTP
        public static final Integer  OTP_EXPIRED  = 820;
        public static final Integer  OTP_ERROR  = 821;

        //WaitList
        public static final Integer DUPLICATE_CONTACT = 920;

        //REFERRAL
        public static final Integer REFERRER_ALREADY_EXISTS = 720;
        public static final Integer USER_NOT_REFERRER = 721;

        // Access Control

        public static final Integer UNAUTHORIZED_ADMIN_ACCESS = 820;
        public static final Integer USER_NOT_ADMIN = 822;
        public static final Integer INVALID_PERMISSION_NAME = 823;

        public static final Integer USER_REQUEST_ALREADY_EXIST = 824;

        public static final Integer ILLEGAL_PERMISSION_REQUEST = 825;

        public static final Integer PERMISSION_ALREADY_GRANTED_USER = 826;

        public static final Integer INVALID_PERMISSION_REQUEST_STATUS = 827;
        
        public static final Integer USER_NOT_COOPERATE = 828;
        
        public static final Integer INVALID_ACCOUNT = 830;
    }
}
