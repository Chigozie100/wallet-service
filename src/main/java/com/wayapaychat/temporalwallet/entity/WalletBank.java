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
@Table(name = "m_wallet_bank")
public class WalletBank {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
	private Long id;
	
	private boolean del_flg;
	
	private boolean entity_cre_flg;
	
	private String sol_id;
	
	@Column(unique = true, nullable = false)
	private String account_no;
	
	@Column(nullable = false)
	private String acct_name;
	
	@Column(nullable = false)
	private Long user_id;
	
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

	public WalletBank(String sol_id, String account_no, String acct_name, Long user_id,
			String gl_code, String product_code, String acct_ownership, String hashed_no,
			boolean int_paid_flg, boolean int_coll_flg, String rcre_user_id,
			LocalDate rcre_time, String acct_crncy_code, String product_type) {
		super();
		this.del_flg = false;
		this.entity_cre_flg = false;  
		this.sol_id = sol_id;
		this.account_no = account_no;
		this.acct_name = acct_name;
		this.user_id = user_id;
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
	}
	
	

}
