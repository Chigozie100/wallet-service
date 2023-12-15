package com.wayapaychat.temporalwallet.dao;


import com.wayapaychat.temporalwallet.config.SecurityConstants;
import com.wayapaychat.temporalwallet.pojo.UserDtoResponse;
import com.wayapaychat.temporalwallet.proxy.AuthProxy;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.pojo.UserDetailPojo;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

@Repository
@Slf4j
public class AuthUserServiceDAOImpl implements AuthUserServiceDAO {

//	@Autowired
//	private DBConnectConfig jdbcTemplate;

	@Autowired
	private AuthProxy authProxy;
	
//	@Autowired
//	private JdbcTemplate jdbcTemp;

	@Override
	public UserDetailPojo AuthUser(HttpServletRequest request,int user_id) {
		try {
			UserDtoResponse tokenCheckResponse = authProxy.getUserById(user_id,request.getHeader(SecurityConstants.HEADER_STRING),request.getHeader(SecurityConstants.CLIENT_ID),request.getHeader(SecurityConstants.CLIENT_TYPE));
			if(!tokenCheckResponse.isStatus())
				return null;

			return tokenCheckResponse.getData();
		} catch (FeignException ex) {
			log.error("An error Occured: Cause: {} \r\n Message: {}", ex.getCause(), ex.getMessage());
			log.error("::Error AuthUser {}",ex.contentUTF8());
			ex.printStackTrace();
			return null;
		}
	}
//	@Override
//	public UserDetailPojo AuthUser(int user_id) {
//		UserDetailPojo user = null;
//		StringBuilder query = new StringBuilder();
//		query.append("select id,email,first_name,surname,phone_number,is_active,");
//		query.append("is_deleted,is_corporate,is_admin from m_users where id = ?");
//		String sql = query.toString();
//		try {
//			UserDetailPojoMapper rowMapper = new UserDetailPojoMapper();
//			Object[] params = new Object[] { user_id };
//			user = jdbcTemplate.jdbcConnect().queryForObject(sql, rowMapper, params);
//			return user;
//		} catch (Exception ex) {
//			log.error("An error Occured: Cause: {} \r\n Message: {}", ex.getCause(), ex.getMessage());
//			return null;
//		}
//	}

//	@Override
//	public List<UserDetailPojo> getUser() {
//		List<UserDetailPojo> user = null;
//		StringBuilder query = new StringBuilder();
//		query.append("select id,email,first_name,surname,phone_number,is_active,");
//		query.append("is_deleted,is_corporate from m_users where id = ?");
//		String sql = query.toString();
//		try {
//			UserDetailPojoMapper rowMapper = new UserDetailPojoMapper();
//			user = jdbcTemplate.jdbcConnect().query(sql, rowMapper);
//			return user;
//		} catch (Exception ex) {
//			log.error("An error Occured: Cause: {} \r\n Message: {}", ex.getCause(), ex.getMessage());
//			return null;
//		}
//	}

//	@Override
//	public Integer getAuthCount(String user_id) {
//		String sql = "SELECT count(*) FROM  m_users ";
//		sql = sql + "WHERE id = ? ";
//		int count = 0;
//		try {
//			Object[] params = new Object[] { user_id };
//			count = jdbcTemplate.jdbcConnect().queryForObject(sql, Integer.class, params);
//		} catch (EmptyResultDataAccessException ex) {
//			log.error(ex.getMessage());
//		}
//		return count;
//	}

//	@Override
//	public Integer getId(int user_id) {
//		String sql = "SELECT id FROM  users ";
//		sql = sql + "WHERE user_id = ? ";
//		log.info(":" + user_id);
//		int count = 0;
//		try {
//			Object[] params = new Object[] { user_id };
//			count = jdbcTemp.queryForObject(sql, Integer.class, params);
//		} catch (EmptyResultDataAccessException ex) {
//			log.error(ex.getMessage());
//		}
//		return count;
//	}

}
