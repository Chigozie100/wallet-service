package com.wayapaychat.temporalwallet.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "m_wallet_interest", uniqueConstraints = {
        @UniqueConstraint(name = "UniqueCrncyCodeAndIntCodeAndVersionAndDelFlg", 
        		columnNames = {"intTblCode", "crncyCode", "int_version_num", "del_flg"})})
public class WalletInterest {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
	private Long id;
	
	private boolean del_flg;
	
	private boolean entity_cre_flg;
	
	@Column(nullable = false)
	private String crncyCode; //
	
	@Column(nullable = false)
	private String intTblCode; //
	
	@Column(nullable = false)
	private String int_version_num; //
	
	private double int_rate_pcnt;
	
	@Column(nullable = false)
	private String int_slab_drcr;
	
	private double begin_slab_amt;
	
	private double end_slab_amt;
	
	private String penal_portion_ind = "F";
	
	private double penal_int_pcnt;

	public WalletInterest(String crncyCode, String intTblCode,String int_version_num, 
			double int_rate_pcnt, String int_slab_drcr, double begin_slab_amt,
			double end_slab_amt,double penal_int_pcnt) {
		super();
		this.del_flg = false;
		this.entity_cre_flg = true;
		this.crncyCode = crncyCode;
		this.intTblCode = intTblCode;
		this.int_version_num = int_version_num;
		this.int_rate_pcnt = int_rate_pcnt;
		this.int_slab_drcr = int_slab_drcr;
		this.begin_slab_amt = begin_slab_amt;
		this.end_slab_amt = end_slab_amt;
		this.penal_int_pcnt = penal_int_pcnt;
	}
	
	

}
