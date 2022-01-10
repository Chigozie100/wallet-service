package com.wayapaychat.temporalwallet.mapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import com.wayapaychat.temporalwallet.pojo.TransWallet;

public class TransWalletMapper implements RowMapper<TransWallet> {

	@Override
	public TransWallet mapRow(ResultSet rs, int rowNum) throws SQLException {
		String acctNo = rs.getString("acct_num");
		String paymentRef = rs.getString("payment_reference");
		BigDecimal tranAmount = rs.getBigDecimal("tran_amount");
		String tranCrncy = rs.getString("tran_crncy_code");
		Date tranDate = rs.getDate("tran_date");
		String tranNarrate = rs.getString("tran_narrate");
		String tranType = rs.getString("tran_type");
		String partTranType = rs.getString("part_tran_type");
		String tranId = rs.getString("tran_id");
		String status = rs.getString("categoies");
		return new TransWallet(acctNo, paymentRef, tranAmount, tranCrncy, tranDate,
				tranNarrate, tranType, partTranType, tranId, status);
	}

}
