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
	
	@Column(nullable = false)
	private String productCode;
	
	@Column(unique = true, nullable = false)
	private String productName;
	
	@Column(nullable = false)
	private String productType; //Product Type
	
	@Column(nullable = false)
	private String currencyCode; //Currency
	
	@Column(nullable = false)
	private String glSubHeadCode;

	public WalletProductCode(String productCode, String productName, String productType,
			String currencyCode, String glSubHeadCode) {
		super();
		this.del_flg = false;
		this.productCode = productCode;
		this.productName = productName;
		this.productType = productType;
		this.currencyCode = currencyCode;
		this.glSubHeadCode = glSubHeadCode;
	}

	
	

}
