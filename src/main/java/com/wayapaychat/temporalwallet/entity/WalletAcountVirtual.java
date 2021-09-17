package com.wayapaychat.temporalwallet.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "m_wallet_account_virtual")
public class WalletAcountVirtual {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	private Long id;
    
	@Column(unique = true, nullable = false)
	private Long virtuId;

	private String bankName;

	private String bankCode;
    
	@Column(unique = true, nullable = false)
	private String accountNumber;

	private String accountName;
    
	@Column(nullable = false)
	private String userId;

	private boolean deleted;
	
	private BigDecimal actualBalance; 

	public WalletAcountVirtual(Long virtuId, String bankName, String bankCode, String accountNumber,
			String accountName, String userId, boolean deleted) {
		super();
		this.virtuId = virtuId;
		this.bankName = bankName;
		this.bankCode = bankCode;
		this.accountNumber = accountNumber;
		this.accountName = accountName;
		this.userId = userId;
		this.deleted = deleted;
		this.actualBalance = BigDecimal.ZERO;
	}	

}
