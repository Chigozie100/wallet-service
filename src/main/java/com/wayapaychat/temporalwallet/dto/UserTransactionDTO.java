package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTransactionDTO {
	
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
	@Min(value = 1, message = "Amount must be greater than zero")
	private BigDecimal amount;


}
