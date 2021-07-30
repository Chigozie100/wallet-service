package com.wayapaychat.temporalwallet.dto;

import lombok.Data;

@Data
public class ToggleSwitchDTO {
	
	private boolean offPrevSwitch;
	
	private boolean onNewSwitch;
	
	private String prevSwitchCode;
	
	private String newSwitchCode;

}
