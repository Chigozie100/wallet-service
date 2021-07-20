package com.wayapaychat.temporalwallet.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.wayapaychat.temporalwallet.pojo.UserDetailPojo;

public class UserDetailPojoMapper implements RowMapper<UserDetailPojo> {

	@Override
	public UserDetailPojo mapRow(ResultSet rs, int rowNum) throws SQLException {
		Long id = rs.getLong("id");
		String email = rs.getString("email");
		String firstName = rs.getString("first_name");
		String surname = rs.getString("surname");
		String phoneNo = rs.getString("phone_number");
		boolean is_active = rs.getBoolean("is_active");
		boolean is_deleted = rs.getBoolean("is_deleted");
		boolean is_corporate = rs.getBoolean("is_corporate");
		boolean is_admin = rs.getBoolean("is_admin");
		return new UserDetailPojo(id,email,firstName,surname,phoneNo,is_active,is_deleted,is_corporate,is_admin);
	}

}
