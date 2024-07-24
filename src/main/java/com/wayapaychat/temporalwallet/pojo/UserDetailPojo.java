package com.wayapaychat.temporalwallet.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.waya.security.auth.pojo.UserIdentityData;
import lombok.*;
import org.springframework.beans.BeanUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data @AllArgsConstructor @NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class UserDetailPojo {

	@JsonProperty("userId")
	private Long id;
	private String accessType = "";
	private boolean isEmailVerified;
	private String phoneNumber = "";
	private String firstName = "";
	private String lastName = "";
	private String email = "";
	private boolean isAdmin;
	private boolean isPhoneVerified;
	private boolean isAccountLocked;
	private boolean isAccountExpired;
	private boolean isCredentialsExpired;
	private boolean isActive;
	private boolean isAccountDeleted;
	private String referenceCode = "";
	private boolean pinCreated;
	private boolean isCorporate;
	private String gender = "";
	private String middleName = "";
	private String dateOfBirth = "";
	private String profileImage = "";
	private String district = "";
	private String address = "";
	private String city = "";
	private String state = "";
	private String merchantId = "";
	private String createdAt = "";
	private String transactionLimit;
	private Set<String> roles = new HashSet<>();
	private Set<String> permits = new HashSet<>();

//	private Long id;
//	private String email;
//	private String phoneNumber;
//	private String referenceCode;
//	private String firstName;
//	private String surname;
//	private String password;
//	private boolean phoneVerified;
//	private boolean emailVerified;
//	private boolean pinCreated;
//	private boolean corporate;
//	private boolean admin;
//	private boolean deleted;
//	private List<String> roles;
//	private List<String> permits;
//	private String transactionLimit;
//	private String userLocation;
//	private String token;
//	private ProfileSubscriptionDto profileSubscription;
//	public Long id;
//
//	private String email;
//
//	private String firstName;
//
//	private String surname;
//
//	private String phoneNo;
//
//	private boolean is_active;
//
//	private boolean is_deleted;
//
//	private boolean is_corporate;
//
//	private boolean is_admin;
//
//	public UserDetailPojo(Long id, String email, String firstName, String surname, String phoneNo, boolean is_active,
//			boolean is_deleted, boolean is_corporate, boolean is_admin) {
//		super();
//		this.id = id;
//		this.email = email;
//		this.firstName = firstName;
//		this.surname = surname;
//		this.phoneNo = phoneNo;
//		this.is_active = is_active;
//		this.is_deleted = is_deleted;
//		this.is_corporate = is_corporate;
//		this.is_admin = is_admin;
//	}
	

}
