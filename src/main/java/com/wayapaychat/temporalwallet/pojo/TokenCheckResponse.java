package com.wayapaychat.temporalwallet.pojo;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenCheckResponse {

	private Date timeStamp;
	private boolean status;
	private String message;
	private MyData data;
}
