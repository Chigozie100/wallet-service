package com.wayapaychat.temporalwallet.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountPojo {

	private Long id;
	private String firstName;
    private String lastName;
    private Long externalId;
    private int savingsProductId;
    private String emailAddress;
    private String mobileNo;
}
