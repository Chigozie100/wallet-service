package com.wayapaychat.temporalwallet.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import com.wayapaychat.temporalwallet.dto.AccountStatementDTO;

public class AccountStatementMapper implements RowMapper<AccountStatementDTO> {

	@Override
	public AccountStatementDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		Date transactionDate = rs.getDate("tran_date"); 
		String tranType = rs.getString("tran_type");
		Date transactionTime = rs.getDate("created_at"); 
		String senderEmail = rs.getString("created_email");
		String receiverEmail = rs.getString("email_address"); 
		String phoneNo = rs.getString("mobile_no");
		String accountNo = rs.getString("account_no"); 
		double tranAmount = rs.getDouble("tran_amount"); 
		String tranNarration = rs.getString("tran_narrate");
		String debitCredit = rs.getString("debit_credit");
		String tranId = rs.getString("tran_id");
		//return new AccountStatementDTO(transactionDate, tranType, transactionTime, senderEmail,
		//		receiverEmail, phoneNo, accountNo, tranAmount, tranNarration, debitCredit, tranId);
		
		return new AccountStatementDTO(transactionDate, tranType, transactionTime, senderEmail,
				receiverEmail, phoneNo, accountNo, tranAmount, tranNarration,
				debitCredit, tranId);
	}

}
