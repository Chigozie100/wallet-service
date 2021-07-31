package com.wayapaychat.temporalwallet.dto;

import lombok.Data;

@Data
public class ToggleSwitchDTO {
	
	private boolean onSwitch;
	
	private String prevSwitchCode;
	
	private String newSwitchCode;

}
