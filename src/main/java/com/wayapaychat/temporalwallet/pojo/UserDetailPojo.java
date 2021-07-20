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
	
	private boolean is_corporate;
	
	private boolean is_admin;

	public UserDetailPojo(Long id, String email, String firstName, String surname, String phoneNo, boolean is_active,
			boolean is_deleted, boolean is_corporate, boolean is_admin) {
		super();
		this.id = id;
		this.email = email;
		this.firstName = firstName;
		this.surname = surname;
		this.phoneNo = phoneNo;
		this.is_active = is_active;
		this.is_deleted = is_deleted;
		this.is_corporate = is_corporate;
		this.is_admin = is_admin;
	}
	

}
