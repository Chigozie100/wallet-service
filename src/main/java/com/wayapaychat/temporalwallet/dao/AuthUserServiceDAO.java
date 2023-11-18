package com.wayapaychat.temporalwallet.dao;

import java.util.List;

import com.wayapaychat.temporalwallet.pojo.UserDetailPojo;

import javax.servlet.http.HttpServletRequest;

public interface AuthUserServiceDAO {
	
	UserDetailPojo AuthUser(HttpServletRequest request, int user_id);
	
//	List<UserDetailPojo> getUser();
//
//	Integer getAuthCount(String user_id);
//
//	Integer getId(int user_id);
	

}
