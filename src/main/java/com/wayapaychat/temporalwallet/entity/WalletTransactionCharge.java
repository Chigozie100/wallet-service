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
@Table(name = "m_wallet_charge")
public class WalletTransactionCharge {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	private Long id;
	
	@Column(unique = true, nullable = false)
	private String chargeName;
	
	private String currencyCode;
	
	private double fixedAmount;
	
	private double fixedPercent;
	
	@Column(nullable = false)
	private String chargePerMode;
	//TRANSACTION,DAILY,MONTH,QUATERLY, YEAR
	
	private boolean isTaxable;
	
	private String chargeEvent;
	
	private boolean isDeleted;

	public WalletTransactionCharge(String chargeName, String currencyCode, double fixedAmount, double fixedPercent,
			String chargePerMode, boolean isTaxable, String chargeEvent) {
		super();
		this.chargeName = chargeName;
		this.currencyCode = currencyCode;
		this.fixedAmount = fixedAmount;
		this.fixedPercent = fixedPercent;
		this.chargePerMode = chargePerMode;
		this.isTaxable = isTaxable;
		this.chargeEvent = chargeEvent;
		this.isDeleted = false;
	}
	

}
