package com.wayapaychat.temporalwallet.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.wayapaychat.temporalwallet.dto.AccountLookUp;

public class AccountLookUpMapper implements RowMapper<AccountLookUp> {

	@Override
	public AccountLookUp mapRow(ResultSet rs, int rowNum) throws SQLException {
		Long vId = rs.getLong("vId");
		//Long userId = rs.getLong("user_id");
		//String email = rs.getString("email_address");
		String custName = rs.getString("cust_name"); 
		String vAccountNo = rs.getString("account_number"); 
		return new AccountLookUp(vId, custName, vAccountNo);
	}

}
