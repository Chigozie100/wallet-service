package com.wayapaychat.temporalwallet.dto;

import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class BulkTransactionExcelDTO {
	
	@NotEmpty(message= "List Should Not be Empty")
	private Set<@Valid ExcelTransactionCreationDTO> usersList;

	public BulkTransactionExcelDTO(
			@NotEmpty(message = "List Should Not be Empty") Set<@Valid ExcelTransactionCreationDTO> usersList) {
		super();
		this.usersList = usersList;
	}
	
	

}
