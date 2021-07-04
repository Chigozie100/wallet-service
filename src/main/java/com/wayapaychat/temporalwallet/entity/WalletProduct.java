package com.wayapaychat.temporalwallet.entity;

import java.time.LocalDate;

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
@Table(name = "m_wallet_product")
public class WalletProduct {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
	private Long id;
	
	private boolean del_flg;
	
	@Column(nullable = false)
	private String product_code;
	
	private String product_desc;
	
	private boolean sys_gen_acct_flg;
	
	private String int_paid_bacid;
	
	private String product_type; // PRODUCT TYPE TABLE
	
	private boolean int_paid_flg;
	
	private boolean int_coll_flg;
	
	private boolean staff_product_flg;
	
	private String int_freq_type_cr;
	
	private String comm_paid_bacid;
	
	private boolean comm_paid_flg;
	
	@Column(nullable = false)
	private String crncy_code;//CURRENCY TABLE
		
	private double cash_dr_limit;
	
	private double xfer_dr_limit;
	
	private double cash_cr_limit;
	
	private double xfer_cr_limit;
	
	@Column(nullable = false)
	private String int_tbl_code;//INT TABLE
	
	private String mic_event_code;//EVENT TABLE
	
	private double product_min_bal;
	
	private String min_avg_bal;
	
	@Column(nullable = false)
	private String rcre_user_id;
	
	@Column(nullable = false)
	private LocalDate rcre_time;

}