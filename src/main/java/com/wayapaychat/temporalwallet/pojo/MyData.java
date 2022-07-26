package com.wayapaychat.temporalwallet.pojo;

import java.util.List;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MyData {
	
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
    private String userLocation;
    private String token;
    public MyData(String email) {
    	this.email = email;
    }

}
