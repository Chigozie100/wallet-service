package com.wayapaychat.temporalwallet.entity;

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
@Table(name = "m_wallet_account_gl")
public class WalletGLAccount {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	private Long id;
	
	@Column(nullable = false)
	private String sol_id;
	
	private boolean del_flg;
	
	private boolean entity_cre_flg;
	
	private String crncyCode;
	
	@Column(unique = true, nullable = false)
	private String glName;
	
	private String glCode;
	
	@Column(unique = true, nullable = false)
	private String glSubHeadCode;

	public WalletGLAccount(String sol_id,String glName,String glCode, String glSubHeadCode, String crncyCode) {
		super();
		this.sol_id = sol_id;
		this.del_flg = false;
		this.entity_cre_flg = true;
		this.glName = glName;
		this.glCode = glCode;
		this.glSubHeadCode = glSubHeadCode;
		this.crncyCode = crncyCode;
	}
	

}
