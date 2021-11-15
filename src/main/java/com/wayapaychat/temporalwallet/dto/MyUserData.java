package com.wayapaychat.temporalwallet.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MyUserData {
	
	private Long id;
    private String email;
    private String phoneNumber;
    private String referenceCode;
    private String firstName;
    private String surname;
    private String password;
    private boolean phoneVerified;
    private boolean emailVerified;
    private boolean pinCreated;
    private boolean corporate;
    private List<String> roles;
    private List<String> permits;
    private String transactionLimit;
    
    public MyUserData(String email) {
    	this.email = email;
    }

}
