package com.wayapaychat.temporalwallet.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.springframework.jdbc.core.RowMapper;
import com.wayapaychat.temporalwallet.dto.AccountTransChargeDTO;

public class AccountTransChargeMapper implements RowMapper<AccountTransChargeDTO>{
	
	@Override
	public AccountTransChargeDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		Date transactionDate = rs.getDate("tran_date"); 
		String tranType = rs.getString("tran_type");
		Date transactionTime = rs.getDate("created_at"); 
		String processorEmail = rs.getString("created_email");
		String senderEmail = rs.getString("email_address"); 
		String debitPhoneNo = rs.getString("mobile_no");
		String debitAccountNo = rs.getString("account_no"); 
		double debitTranAmount = rs.getDouble("tran_amount"); 
		String tranNarration = rs.getString("tran_narrate");
		String debitCredit = rs.getString("debit_credit");
		String tranId = rs.getString("tran_id");
		String creditAccountNo = rs.getString("credit_account"); 
		double creditTranAmount = rs.getDouble("credit_amount");
		String receiverEmail = rs.getString("email_address"); 
		String creditPhoneNo = rs.getString("mobile_no");
		String creditDebit = rs.getString("debit_credit");
		double chargeTranAmount = rs.getDouble("credit_charge");
		
		return new AccountTransChargeDTO(transactionDate, tranType, transactionTime, processorEmail,
				senderEmail, debitPhoneNo, debitAccountNo, debitTranAmount,
				tranNarration, debitCredit, tranId, creditAccountNo, creditTranAmount,
				receiverEmail, creditPhoneNo, creditDebit, chargeTranAmount);
	}

}
