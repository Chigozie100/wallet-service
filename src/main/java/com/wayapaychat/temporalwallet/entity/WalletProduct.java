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
	private String productCode;
	
	private String product_desc;
	
	private boolean sys_gen_acct_flg;
	
	private String int_paid_bacid;
	
	private String product_type; // PRODUCT TYPE TABLE
	
	private boolean int_paid_flg;
	
	private boolean int_coll_flg;
	
	private String int_coll_bacid;
	
	private boolean staff_product_flg;
	
	private boolean chq_book_flg;
	
	private String int_freq_type_cr;//M, D, Q, Y
	
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
	
	private String glSubHeadCode;
	
	@Column(nullable = false)
	private LocalDate rcre_time;

	public WalletProduct(String productCode, String product_desc, boolean sys_gen_acct_flg,
			String product_type, boolean int_paid_flg, boolean int_coll_flg,boolean staff_product_flg, 
			String int_freq_type_cr,boolean comm_paid_flg, String crncy_code, double cash_dr_limit, 
			double xfer_dr_limit, double cash_cr_limit,double xfer_cr_limit, String int_tbl_code, 
			double product_min_bal,boolean chq_book_flg,String glSubHeadCode) {
		super();
		this.del_flg = false;
		this.productCode = productCode;
		this.product_desc = product_desc;
		this.sys_gen_acct_flg = sys_gen_acct_flg;
		this.product_type = product_type;
		this.int_paid_flg = int_paid_flg;
		this.int_coll_flg = int_coll_flg;
		this.staff_product_flg = staff_product_flg;
		this.int_freq_type_cr = int_freq_type_cr;
		this.comm_paid_flg = comm_paid_flg;
		this.crncy_code = crncy_code;
		this.cash_dr_limit = cash_dr_limit;
		this.xfer_dr_limit = xfer_dr_limit;
		this.cash_cr_limit = cash_cr_limit;
		this.xfer_cr_limit = xfer_cr_limit;
		this.int_tbl_code = int_tbl_code;
		this.product_min_bal = product_min_bal;
		this.rcre_user_id = "WAYAADMIN";
		this.rcre_time = LocalDate.now();
		this.chq_book_flg = chq_book_flg;
		this.glSubHeadCode = glSubHeadCode;
	}
	
	

}