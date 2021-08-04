package com.wayapaychat.temporalwallet.dto;

import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BulkTransactionCreationDTO {
	
	@NotEmpty(message= "List Should Not be Empty")
	private Set<@Valid UserTransactionDTO> usersList;
	
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

	public BulkTransactionCreationDTO(
			@NotEmpty(message = "List Should Not be Empty") Set<@Valid UserTransactionDTO> usersList,
			@NotBlank(message = "tranType must not Null or Blank") String tranType,
			@NotBlank(message = "tranCrncy must not Null or Blank") @Size(min = 3, max = 5, message = "tranCrncy must be 3 alphanumeric (NGN)") String tranCrncy,
			@NotBlank(message = "tranNarration must not Null or Blank") @Size(min = 5, max = 50, message = "tranNarration must be aleast 5 alphanumeric") String tranNarration,
			@NotBlank(message = "tranNarration must not Null or Blank") @Size(min = 3, max = 50, message = "paymentReference must be aleast 3 alphanumeric") String paymentReference,
			@Size(min = 13, max = 16, message = "Office Account Number must be at least 15 characters") @NotBlank(message = "Office Account Number Cannot be blank") String officeAccountNo) {
		super();
		this.usersList = usersList;
		this.tranType = tranType;
		this.tranCrncy = tranCrncy;
		this.tranNarration = tranNarration;
		this.paymentReference = paymentReference;
		this.officeAccountNo = officeAccountNo;
	}
	
	

}
