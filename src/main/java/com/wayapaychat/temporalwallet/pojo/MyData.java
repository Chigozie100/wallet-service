package com.wayapaychat.temporalwallet.pojo;

import java.util.List;

import org.springframework.beans.BeanUtils;
import com.waya.security.auth.pojo.UserIdentityData;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
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
    public static MyData newInstance(UserIdentityData _userData) {
        MyData myData = new MyData();
        BeanUtils.copyProperties(_userData, myData);
        return myData;
    }

}
