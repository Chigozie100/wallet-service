package com.wayapaychat.temporalwallet.mapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
 
import com.wayapaychat.temporalwallet.dto.CustomerTransactionSumary;

public class CustomerTransactionSumaryMapper implements RowMapper<CustomerTransactionSumary> {

	@Override
	public CustomerTransactionSumary mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		BigDecimal totalBalance = rs.getBigDecimal("total_balance"); 
        BigDecimal totalDeposit = rs.getBigDecimal("total_credit_balance"); 
        BigDecimal totalWithdrawal = rs.getBigDecimal("total_debit_balance"); 
		return new CustomerTransactionSumary(totalBalance, totalDeposit, totalWithdrawal) ;

	}

}
