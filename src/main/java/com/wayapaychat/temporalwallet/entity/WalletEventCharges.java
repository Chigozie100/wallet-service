package com.wayapaychat.temporalwallet.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.*;

@Data
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "m_wallet_event")
public class WalletEventCharges {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
	private Long id;
	
	@Column(nullable = false)
	private boolean del_flg;
	
	@Column(unique = true, nullable = false)
	private String eventId;
	
	private BigDecimal tranAmt;
	
	@Column(nullable = false)
	private String placeholder;
	
	@Column(nullable = false)
	private boolean taxable;
	
	private BigDecimal taxAmt;
	
	@Column(nullable = false)
	private String crncyCode;
	
	@Column(nullable = false)
	private String tranNarration;
	
	@Column(nullable = false)
	private boolean chargeCustomer;
	
	@Column(nullable = false)
	private boolean chargeWaya;
	
	private boolean processflg = false;

	public WalletEventCharges(String eventId, BigDecimal tranAmt, String placeholder,
			boolean taxable, BigDecimal taxAmt, String tranNarration, boolean chargeCustomer, 
			boolean chargeWaya, String crncyCode) {
		super();
		this.del_flg = false;
		this.eventId = eventId;
		this.tranAmt = tranAmt;
		this.placeholder = placeholder;
		this.taxable = taxable;
		this.taxAmt = taxAmt;
		this.tranNarration = tranNarration;
		this.chargeCustomer = chargeCustomer;
		this.chargeWaya = chargeWaya;
		this.crncyCode = crncyCode;
		//COMMISSION PAYMENT COMPAYM
		//SMS CHARGE         SMSCHG
		//AIRTIME COLLECTION AITCOL
	}
	
	

}
