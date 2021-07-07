package com.wayapaychat.temporalwallet.dao;

import java.util.List;

import com.wayapaychat.temporalwallet.pojo.UserDetailPojo;

public interface AuthUserServiceDAO {
	
	public UserDetailPojo AuthUser(int user_id);
	
	public List<UserDetailPojo> getUser();
	
	public Integer getAuthCount(String user_id);
	
	public Integer getId(int user_id);
	

}
