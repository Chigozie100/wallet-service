package com.wayapaychat.temporalwallet.dto;

import java.util.Date;

import org.springframework.http.HttpStatus;

import com.wayapaychat.temporalwallet.pojo.MyData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReceiptJson {
	
	private  String message;
	
    private  boolean status;
    
    private HttpStatus httpStatus;
    
    private ReceiptResponse data;

}
