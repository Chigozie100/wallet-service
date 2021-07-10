package com.wayapaychat.temporalwallet.dto;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDTO {
	
	@NotNull
    private long userId;
	
	@NotNull
    private boolean isCorporate;

}
