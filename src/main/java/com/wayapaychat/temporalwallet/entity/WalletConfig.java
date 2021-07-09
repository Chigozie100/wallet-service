package com.wayapaychat.temporalwallet.entity;

import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
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
@Table(name = "m_config")
public class WalletConfig {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
	private Long id;
	
	@Column(nullable = false)
	private boolean del_flg;
	
	@Column(unique = true, nullable = false)
	private String codeName;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)  
	@JoinColumn(name="code_id")  
	@OrderColumn(name="position")  
	private Collection<WalletBankConfig> bankConfig; 

	public WalletConfig(String codeName, Collection<WalletBankConfig> bankConfig) {
		super();
		this.del_flg = false;
		this.codeName = codeName;
		this.bankConfig = bankConfig;
	}
	

}
