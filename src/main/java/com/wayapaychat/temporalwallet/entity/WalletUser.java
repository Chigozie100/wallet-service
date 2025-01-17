package com.wayapaychat.temporalwallet.entity;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;

import javax.persistence.*;

@Data
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "m_wallet_user", uniqueConstraints = {
		@UniqueConstraint(name = "UniqueEmailAndPhoneNumberAndUserIdAndProfileIdAndDelFlg", columnNames = {"mobileNo", "emailAddress", "userId", "profile_id", "del_flg"})})
public class WalletUser {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	private Long id;

	private boolean del_flg;
	private boolean entity_cre_flg;
	private String sol_id;

	@Column(nullable = false)
	private Long userId;

	private String firstName;
	private String lastName;

	@Column(unique = true, nullable = false)
	private String emailAddress;

	@Column(unique = true, nullable = false)
	private String mobileNo;

	private String cust_name;
	private String cust_title_code;
	private String cust_sex;
	private Date dob;
	private String cust_issue_id;
	private Date cust_exp_issue_date;
	private LocalDate cust_opn_date;
	private double cust_debit_limit;
	private double oneTimeTransactionLimit;

	@Column(nullable = false)
	private String rcre_user_id;

	@Column(nullable = false)
	private LocalDate rcre_time;

	@JsonIgnore
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "cif_id")
	private List<WalletAccount> account;

	private boolean isVirtualAccount = false;
	private boolean isCorporate = false;

	@Column(name = "account_type")
	private String accountType;

	@Column(name = "profile_id")
	private String profileId;

	@Column(name = "is_simulated", nullable = false)
	private boolean isSimulated;

	public WalletUser(String sol_id, Long userId, String firstName,
					  String lastName, String emailAddress, String mobileNo, String cust_name, String cust_title_code,
					  String cust_sex, Date dob, String cust_issue_id, Date cust_exp_issue_date, LocalDate cust_opn_date,
					  double cust_debit_limit, List<WalletAccount> account, boolean isSimulated) {
		this.del_flg = false;
		this.entity_cre_flg = true;
		this.sol_id = sol_id;
		this.userId = userId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.emailAddress = emailAddress;
		this.mobileNo = mobileNo;
		this.cust_name = cust_name;
		this.cust_title_code = cust_title_code;
		this.cust_sex = cust_sex;
		this.dob = dob;
		this.cust_issue_id = cust_issue_id;
		this.cust_exp_issue_date = cust_exp_issue_date;
		this.cust_opn_date = cust_opn_date;
		this.cust_debit_limit = cust_debit_limit;
		this.rcre_user_id = "WAYADMIN";
		this.rcre_time = LocalDate.now();
		this.account = account;
		this.isSimulated = isSimulated;
	}

    
	public WalletUser(String sol_id, Long userId, String firstName,
			String lastName, String emailAddress, String mobileNo, String cust_name, String cust_title_code,
			String cust_sex, Date dob, String cust_issue_id, Date cust_exp_issue_date, LocalDate cust_opn_date,
			double cust_debit_limit) {
		super();
		this.del_flg = false;
		this.entity_cre_flg = true;
		this.sol_id = sol_id;
		this.userId = userId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.emailAddress = emailAddress;
		this.mobileNo = mobileNo;
		this.cust_name = cust_name;
		this.cust_title_code = cust_title_code;
		this.cust_sex = cust_sex;
		this.dob = dob;
		this.cust_issue_id = cust_issue_id;
		this.cust_exp_issue_date = cust_exp_issue_date;
		this.cust_opn_date = cust_opn_date;
		this.cust_debit_limit = cust_debit_limit;
		this.rcre_user_id = "WAYADMIN";
		this.rcre_time = LocalDate.now();
	}


	public WalletUser(String sol_id, Long userId, String firstName,
					  String lastName, String emailAddress, String mobileNo, String cust_name, String cust_title_code,
					  String cust_sex, Date dob, String cust_issue_id, Date cust_exp_issue_date, LocalDate cust_opn_date,
					  double cust_debit_limit, boolean isCorporate) {
		super();
		this.del_flg = false;
		this.entity_cre_flg = true;
		this.sol_id = sol_id;
		this.userId = userId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.emailAddress = emailAddress;
		this.mobileNo = mobileNo;
		this.cust_name = cust_name;
		this.cust_title_code = cust_title_code;
		this.cust_sex = cust_sex;
		this.dob = dob;
		this.cust_issue_id = cust_issue_id;
		this.cust_exp_issue_date = cust_exp_issue_date;
		this.cust_opn_date = cust_opn_date;
		this.cust_debit_limit = cust_debit_limit;
		this.rcre_user_id = "WAYADMIN";
		this.rcre_time = LocalDate.now();
		this.isCorporate = isCorporate;
	}
    

}
