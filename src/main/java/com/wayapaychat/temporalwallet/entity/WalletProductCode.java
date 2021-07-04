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
@Table(name = "m_product_code")
public class WalletProductCode {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
	private Long id;
	
	@Column(nullable = false)
	private boolean del_flg;
	
	@Column(unique = true, nullable = false)
	private String product_code;
	
	@Column(unique = true, nullable = false)
	private String product_name;
	
	@Column(nullable = false)
	private String product_type;

	public WalletProductCode(String product_code, String product_name, String product_type) {
		super();
		this.del_flg = false;
		this.product_code = product_code;
		this.product_name = product_name;
		this.product_type = product_type;
	}
	

}
