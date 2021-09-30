package com.wayapaychat.temporalwallet.dto;

import java.time.LocalDate;

import com.wayapaychat.temporalwallet.entity.WalletAccount;

import lombok.Data;

@Data
public class NewWalletAccount {
	
	private Long id;

	private boolean del_flg;

	private boolean entity_cre_flg;

	private String sol_id;
	
	private String  bacid;

	private String accountNo;

	private String acct_name;

	private String gl_code;

	private String product_code;

	private String acct_ownership;

	private String frez_code;

	private String frez_reason_code;

	private LocalDate acct_opn_date;

	private boolean acct_cls_flg;

	private double clr_bal_amt;

	private double un_clr_bal_amt;

	private String hashed_no;

	private boolean int_paid_flg;

	private boolean int_coll_flg;

	private String lchg_user_id;

	private LocalDate lchg_time;

	private String rcre_user_id;

	private LocalDate rcre_time;

	private String acct_crncy_code;

	private double lien_amt;

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
	
	private Long userId;

	public NewWalletAccount(WalletAccount acct, Long userId) {
		super();
		this.id = acct.getId();
		this.del_flg = acct.isDel_flg();
		this.entity_cre_flg = acct.isEntity_cre_flg();
		this.sol_id = acct.getSol_id();
		this.bacid = acct.getBacid();
		this.accountNo = acct.getAccountNo();
		this.acct_name = acct.getAcct_name();
		this.gl_code = acct.getGl_code();
		this.product_code = acct.getProduct_code();
		this.acct_ownership = acct.getAcct_ownership();
		this.frez_code = acct.getFrez_code();
		this.frez_reason_code = acct.getFrez_reason_code();
		this.acct_opn_date = acct.getAcct_opn_date();
		this.acct_cls_flg = acct.isAcct_cls_flg();
		this.clr_bal_amt = acct.getClr_bal_amt();
		this.un_clr_bal_amt = acct.getUn_clr_bal_amt();
		this.hashed_no = acct.getHashed_no();
		this.int_paid_flg = acct.isInt_paid_flg();
		this.int_coll_flg = acct.isInt_coll_flg();
		this.lchg_user_id = acct.getLchg_user_id();
		this.lchg_time = acct.getLchg_time();
		this.rcre_user_id = acct.getRcre_user_id();
		this.rcre_time = acct.getRcre_time();
		this.acct_crncy_code = acct.getAcct_crncy_code();
		this.lien_amt = acct.getLien_amt();
		this.product_type = acct.getProduct_type();
		this.cum_dr_amt = acct.getCum_dr_amt();
		this.cum_cr_amt = acct.getCum_cr_amt();
		this.chq_alwd_flg = acct.isChq_alwd_flg();
		this.cash_dr_limit = acct.getCash_dr_limit();
		this.xfer_dr_limit = acct.getXfer_dr_limit();
		this.cash_cr_limit = acct.getCash_cr_limit();
		this.xfer_cr_limit = acct.getXfer_cr_limit();
		this.acct_cls_date = acct.getAcct_cls_date();
		this.last_tran_date = acct.getLast_tran_date();
		this.last_tran_id_dr = acct.getLast_tran_id_dr();
		this.last_tran_id_cr = acct.getLast_tran_id_cr();
		this.walletDefault = acct.isWalletDefault();
		this.lien_reason = acct.getLien_reason();
		this.userId = userId;
	}
	
	

}
