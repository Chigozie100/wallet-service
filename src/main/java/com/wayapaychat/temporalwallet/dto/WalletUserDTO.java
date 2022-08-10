package com.wayapaychat.temporalwallet.dto;

import java.util.Date;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class WalletUserDTO {
	
	@NotNull
	@Size(min=4, max=5)
	private String solId;
	
	@NotNull
	private Long userId;
    
	@NotNull
	@Size(min=3, max=50)
	private String firstName;
    
	@NotNull
	@Size(min=3, max=50)
	private String lastName;
	
	@NotNull
	@Size(min=5, max=50)
	@Email
	private String emailId;
	
	@NotNull
	@Size(min=5, max=20)
	private String mobileNo;
	
	@NotNull
	private Date dob;
	
	private double custDebitLimit;
	
	@NotNull
	@Size(min=2, max=5)
    private String custTitleCode;
	
	@NotNull
	@Size(min=1, max=1)
	private String custSex;
	
	@NotNull
	@Size(min=7, max=20)
	private String custIssueId;
	
	@NotNull
	private Date custExpIssueDate;

	private String accountType;

}
