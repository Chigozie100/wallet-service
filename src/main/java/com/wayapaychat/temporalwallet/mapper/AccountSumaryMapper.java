package com.wayapaychat.temporalwallet.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
 
import com.wayapaychat.temporalwallet.dto.AccountSumary;

public class AccountSumaryMapper implements RowMapper<AccountSumary> {

	@Override
	public AccountSumary mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		Long uId = rs.getLong("user_id");
		String custName = rs.getString("cust_name"); 
		String accountNo = rs.getString("account_no"); 
		String debitLimit = rs.getString("cust_debit_limit"); 
		String email = rs.getString("email_address"); 
		String phone = rs.getString("mobile_no");

		return new AccountSumary(uId, custName, accountNo, debitLimit, email, phone);
	}

}
