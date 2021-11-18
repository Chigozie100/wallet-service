package com.wayapaychat.temporalwallet.exception;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@RestController
@Slf4j
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Object> handleAllExceptions(Exception ex) {
        String message = ex.getLocalizedMessage();
        log.error(ex.getMessage());
        return buildResponseEntity(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(MissingHeaderInfoException.class)
    public final ResponseEntity<Object> handleInvalidTraceIdException (MissingHeaderInfoException ex, WebRequest request) {
        String message = ex.getLocalizedMessage();
        log.error(ex.getMessage());
        return buildResponseEntity(message, HttpStatus.EXPECTATION_FAILED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public final ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex) {
        String message = ex.getLocalizedMessage();
        log.error(ex.getMessage());
        return buildResponseEntity(message, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        String message = "File too Large for Upload. Maximum file Size: " + exc.getMaxUploadSize();
        return buildResponseEntity(message, HttpStatus.EXPECTATION_FAILED);
    }
    
    private ResponseEntity<Object> buildResponseEntity(String apiResponse, HttpStatus status) {
        return new ResponseEntity<>(getError(apiResponse), status);
    }
    
    private Map<String, Object> getError(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("status", false);
        response.put("timestamp", new Date());
        response.put("data", null);
        response.put("timeStamp", ZonedDateTime.now());
        response.put("data", null);

        return response;
    }

}
