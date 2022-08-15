package com.wayapaychat.temporalwallet.entity;

import java.time.LocalDate;

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
import lombok.ToString;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"user"})
@Table(name = "m_wallet_account")
public class WalletAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	private Long id;

	private boolean del_flg;

	private boolean entity_cre_flg;

	private String sol_id;
	
	private String  bacid;

	@Column(unique = true, nullable = false)
	private String accountNo;

	@Column(unique = true)
	private String nubanAccountNo = "0";

	@Column(nullable = false)
	private String acct_name;

	@Column(nullable = false)
	private String gl_code;

	@Column(nullable = false)
	private String product_code;

	private String acct_ownership;

	private String frez_code;

	private String frez_reason_code;

	@Column(nullable = false)
	private LocalDate acct_opn_date;

	@Column(nullable = false)
	private boolean acct_cls_flg;

	private double clr_bal_amt;

	private double un_clr_bal_amt;

	@Column(nullable = false)
	private String hashed_no;

	private boolean int_paid_flg;

	private boolean int_coll_flg;

	private String lchg_user_id;

	private LocalDate lchg_time;

	@Column(nullable = false)
	private String rcre_user_id;

	@Column(nullable = false)
	private LocalDate rcre_time;

	@Column(nullable = false)
	private String acct_crncy_code;

	private double lien_amt;

	@Column(nullable = false)
	private String product_type;

	private double cum_dr_amt;

	private double cum_cr_amt;
	
	private boolean chq_alwd_flg;
	
	private double cash_dr_limit;
	
	private double xfer_dr_limit;
	
	private double cash_cr_limit;
	
	private double xfer_cr_limit;
	
	private LocalDate acct_cls_date;
	
	private LocalDate last_tran_date;
	
	private String last_tran_id_dr;
	
	private String last_tran_id_cr;
	
	private boolean walletDefault;
	
	private String lien_reason;
	
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="cif_id")  
    private WalletUser user;

	public WalletAccount(String sol_id, String accountNo, String acct_name, WalletUser user,
			String gl_code, String product_code, String acct_ownership, String hashed_no,
			boolean int_paid_flg, boolean int_coll_flg, String rcre_user_id,
			LocalDate rcre_time, String acct_crncy_code, String product_type, boolean walletDefault) {
		super();
		this.del_flg = false;
		this.entity_cre_flg = true;  
		this.sol_id = sol_id;
		this.accountNo = accountNo;
		this.acct_name = acct_name;
		this.gl_code = gl_code;
		this.product_code = product_code;
		this.acct_ownership = acct_ownership;
		this.acct_opn_date = LocalDate.now();
		this.acct_cls_flg = false;
		this.clr_bal_amt = 0;
		this.un_clr_bal_amt = 0;
		this.hashed_no = hashed_no;
		this.int_paid_flg = int_paid_flg;
		this.int_coll_flg = int_coll_flg;
		this.rcre_user_id = rcre_user_id;
		this.rcre_time = rcre_time;
		this.acct_crncy_code = acct_crncy_code;
		this.lien_amt = 0;
		this.product_type = product_type;
		this.cum_dr_amt = 0;
		this.cum_cr_amt = 0;
		this.user = user;
		this.walletDefault = walletDefault;
	}

	public WalletAccount(String sol_id, String bacid,String accountNo, String acct_name, WalletUser user, String gl_code, 
			String product_code,String acct_ownership,String hashed_no, boolean int_paid_flg,boolean int_coll_flg, 
			String rcre_user_id, LocalDate rcre_time,String acct_crncy_code,String product_type, 
			boolean chq_alwd_flg, double cash_dr_limit, double xfer_dr_limit, double cash_cr_limit,
			double xfer_cr_limit, boolean walletDefault) {
		super();
		this.del_flg = false;
		this.entity_cre_flg = true;
		this.sol_id = sol_id;
		this.bacid = bacid;
		this.accountNo = accountNo;
		this.acct_name = acct_name;
		this.gl_code = gl_code;
		this.product_code = product_code;
		this.acct_ownership = acct_ownership;
		this.acct_opn_date = LocalDate.now();;
		this.acct_cls_flg = false;
		this.clr_bal_amt = 0;
		this.un_clr_bal_amt = 0;
		this.hashed_no = hashed_no;
		this.int_paid_flg = int_paid_flg;
		this.int_coll_flg = int_coll_flg;
		this.rcre_user_id = rcre_user_id;
		this.rcre_time = rcre_time;
		this.acct_crncy_code = acct_crncy_code;
		this.lien_amt = 0;
		this.product_type = product_type;
		this.cum_dr_amt = 0;
		this.cum_cr_amt = 0;
		this.chq_alwd_flg = chq_alwd_flg;
		this.cash_dr_limit = cash_dr_limit;
		this.xfer_dr_limit = xfer_dr_limit;
		this.cash_cr_limit = cash_cr_limit;
		this.xfer_cr_limit = xfer_cr_limit;
		this.user = user;
		this.walletDefault = walletDefault;
	}

	public WalletAccount(String sol_id, String bacid,String accountNo, String nubanAccountNo, String acct_name, WalletUser user, String gl_code,
						 String product_code,String acct_ownership,String hashed_no, boolean int_paid_flg,boolean int_coll_flg,
						 String rcre_user_id, LocalDate rcre_time,String acct_crncy_code,String product_type,
						 boolean chq_alwd_flg, double cash_dr_limit, double xfer_dr_limit, double cash_cr_limit,
						 double xfer_cr_limit, boolean walletDefault) {
		super();
		this.del_flg = false;
		this.entity_cre_flg = true;
		this.sol_id = sol_id;
		this.bacid = bacid;
		this.accountNo = accountNo;
		this.nubanAccountNo = nubanAccountNo;
		this.acct_name = acct_name;
		this.gl_code = gl_code;
		this.product_code = product_code;
		this.acct_ownership = acct_ownership;
		this.acct_opn_date = LocalDate.now();;
		this.acct_cls_flg = false;
		this.clr_bal_amt = 0;
		this.un_clr_bal_amt = 0;
		this.hashed_no = hashed_no;
		this.int_paid_flg = int_paid_flg;
		this.int_coll_flg = int_coll_flg;
		this.rcre_user_id = rcre_user_id;
		this.rcre_time = rcre_time;
		this.acct_crncy_code = acct_crncy_code;
		this.lien_amt = 0;
		this.product_type = product_type;
		this.cum_dr_amt = 0;
		this.cum_cr_amt = 0;
		this.chq_alwd_flg = chq_alwd_flg;
		this.cash_dr_limit = cash_dr_limit;
		this.xfer_dr_limit = xfer_dr_limit;
		this.cash_cr_limit = cash_cr_limit;
		this.xfer_cr_limit = xfer_cr_limit;
		this.user = user;
		this.walletDefault = walletDefault;
	}


}
