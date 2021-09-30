package com.wayapaychat.temporalwallet.dto;

import java.util.Date;

//import javax.validation.constraints.Email;
//import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class UserAccountDTO {
	
	@NotNull
	private Long userId;
	
	//@NotNull
	//@NotBlank(message = "Email must not be Null or Blank")
	//@Size(min=5, max=50)
	//@Email
	private String newEmailId;
	
	//@NotNull
	//@Size(min=13, max=20)
	private String newMobileNo;
	
	//@NotNull
	//@Size(min=7, max=20)
	private String newCustIssueId;
	
	//@NotNull
	private Date newCustExpIssueDate;	
	
	//@NotNull
	//@Size(min=10, max=10)
	private String oldDefaultAcctNo;
	
	//@NotNull
	//@Size(min=10, max=10)
	private String newDefaultAcctNo;
	
	//@NotNull
	private double newCustDebitLimit;
	

}
