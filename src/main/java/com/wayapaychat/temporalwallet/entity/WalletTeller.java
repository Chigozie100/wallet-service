package com.wayapaychat.temporalwallet.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "m_wallet_teller", uniqueConstraints = {
        @UniqueConstraint(name = "UniqueSolIdAndCrncyCodeAndCashAcctAndDelFlg", columnNames = {"sol_id", "crncyCode", "adminCashAcct", "del_flg"})})
public class WalletTeller {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	private Long id;

	private boolean del_flg;

	private boolean entity_cre_flg;

	private String sol_id;
	
	private String crncyCode;
	
	@Column(nullable = false)
	private Long userId;
	
	@Column(nullable = false)
	private String adminCashAcct;
	
	@CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

	public WalletTeller(String sol_id, String crncyCode, Long userId, String adminCashAcct) {
		super();
		this.del_flg = false;
		this.entity_cre_flg = true;
		this.sol_id = sol_id;
		this.crncyCode = crncyCode;
		this.userId = userId;
		this.adminCashAcct = adminCashAcct;
		this.createdAt = new Date();
	}
	

}
