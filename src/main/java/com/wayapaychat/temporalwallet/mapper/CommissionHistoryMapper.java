package com.wayapaychat.temporalwallet.mapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import com.wayapaychat.temporalwallet.dto.CommissionHistoryDTO;

public class CommissionHistoryMapper implements RowMapper<CommissionHistoryDTO> {

	@Override
	public CommissionHistoryDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		Long id = rs.getLong("user_id");
		String email = rs.getString("email_address");
		String acct_name = rs.getString("acct_name");
		String account_no = rs.getString("account_no");
		BigDecimal balance = rs.getBigDecimal("clr_bal_amt");
		Date custDate = rs.getDate("acct_opn_date");
		return new CommissionHistoryDTO(id, acct_name, account_no, email, balance, custDate);
	}

}
