package com.wayapaychat.temporalwallet.pojo;

import lombok.Data;

@Data
public class UserDetailPojo {
	
	public Long id;
	
	private String email;
	
	private String firstName;
	
	private String surname;
	
	private String phoneNo;
	
	private boolean is_active;
	
	private boolean is_deleted;

	public UserDetailPojo(Long id, String email, String firstName, String surname, String phoneNo, boolean is_active,
			boolean is_deleted) {
		super();
		this.id = id;
		this.email = email;
		this.firstName = firstName;
		this.surname = surname;
		this.phoneNo = phoneNo;
		this.is_active = is_active;
		this.is_deleted = is_deleted;
	}
	

}
