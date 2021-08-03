package com.wayapaychat.temporalwallet.dto;

import lombok.Data;

@Data
public class AuthData {
	
	 private String email;
	 
	 private String phoneNumber;
	 
	 private String firstName;
	 
	 private String lastName;
	 
	 private String referenceCode;
	 
	 private boolean pinCreated;
	 
	 private boolean active;
	 
	 private boolean corporate;
	 
	 private boolean admin;
	 
	 private boolean phoneVerified;
	 
	 private boolean emailVerified;
	 
	 private boolean accountDeleted;
	 
	 private boolean accountExpired;
	 
	 private boolean credentialsExpired;
	 
	 private boolean accountLocked;
	
	 private long userId;

}
