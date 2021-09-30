package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class ExcelTransactionCreationDTO {
	
	@NotNull(message = "Email Cannot be Null")
	@Pattern(regexp = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\." + "[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
			+ "(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message = "Invalid Email")
	private String email;

	@Size(min = 10, message = "Phone Number must be at least 10 characters")
	@NotBlank(message = "phoneNumber Cannot be blank")
	private String phoneNumber;
	
	@Size(min = 10, max = 10, message = "Customer Account Number must be at least 10 characters")
	@NotBlank(message = "Customer Account Number Cannot be blank")
	private String customerAccountNo;
	
	@NotNull
	private BigDecimal amount;
	
	@NotBlank(message = "tranType must not Null or Blank")
	private String tranType;

	@NotBlank(message = "tranCrncy must not Null or Blank")
	@Size(min = 3, max = 5, message = "tranCrncy must be 3 alphanumeric (NGN)")
	private String tranCrncy;

	@NotBlank(message = "tranNarration must not Null or Blank")
	@Size(min = 5, max = 50, message = "tranNarration must be aleast 5 alphanumeric")
	private String tranNarration;

	@NotBlank(message = "tranNarration must not Null or Blank")
	@Size(min = 3, max = 50, message = "paymentReference must be aleast 3 alphanumeric")
	private String paymentReference;
	
	@Size(min = 13, max = 16, message = "Office Account Number must be at least 15 characters")
	@NotBlank(message = "Office Account Number Cannot be blank")
	private String officeAccountNo;

}
