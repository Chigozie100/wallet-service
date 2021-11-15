package com.wayapaychat.temporalwallet.notification;

//import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotifyObjectBody {
	
	//private Date timestamp;
	private String message;
	private boolean status;
	private MyData data;

}
