package com.wayapaychat.temporalwallet.dao;

import java.util.List;

import com.wayapaychat.temporalwallet.pojo.UserDetailPojo;

public interface AuthUserServiceDAO {
	
	UserDetailPojo AuthUser(int user_id);
	
	List<UserDetailPojo> getUser();
	
	Integer getAuthCount(String user_id);
	
	Integer getId(int user_id);
	

}
