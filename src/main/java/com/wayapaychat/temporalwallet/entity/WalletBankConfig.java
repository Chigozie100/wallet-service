package com.wayapaychat.temporalwallet.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "m_wallet_config")
public class WalletBankConfig {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
	private Long id;
	
	@Column(nullable = false)
	private boolean del_flg;
	
	@Column(nullable = false)
	private String codeDesc;
		
	@Column(unique = true, nullable = false)
	private String codeValue;
	
	private String codeSymbol;
	
	@JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="code_id")  
    private WalletConfig config;

	public WalletBankConfig(String codeDesc, String codeValue, String codeSymbol,WalletConfig config) {
		super();
		this.del_flg = false;
		this.codeValue = codeValue;
		this.codeDesc = codeDesc;
		this.codeSymbol = codeSymbol;
		this.config = config;
	}
	
	public WalletBankConfig(String codeDesc, String codeValue, String codeSymbol) {
		super();
		this.del_flg = false;
		this.codeValue = codeValue;
		this.codeDesc = codeDesc;
		this.codeSymbol = codeSymbol;
	}
	

}