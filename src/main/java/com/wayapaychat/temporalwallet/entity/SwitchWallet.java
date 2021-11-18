package com.wayapaychat.temporalwallet.entity;

import java.time.LocalDateTime;

<<<<<<< HEAD
import javax.persistence.Column;
=======
>>>>>>> master
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
<<<<<<< HEAD
import javax.persistence.Table;
=======
>>>>>>> master

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

<<<<<<< HEAD

=======
>>>>>>> master
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
<<<<<<< HEAD
@Table(name = "m_wallet_switch")
=======
>>>>>>> master
public class SwitchWallet {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
<<<<<<< HEAD
	
	private boolean isSwitched;
	
	private LocalDateTime switchCodeTime;
	
	private LocalDateTime lastSwitchTime;
	
	private LocalDateTime createdSwitchTime;
	
	private String switchIdentity;
	
	@Column(nullable = false, unique = true)
    private String switchCode;

	public SwitchWallet(LocalDateTime createdSwitchTime, String switchIdentity, String switchCode) {
		super();
		this.isSwitched = false;
		this.createdSwitchTime = LocalDateTime.now();
		this.switchIdentity = switchIdentity;
		this.switchCode = switchCode;
	}
	
	
=======
	private boolean mifosWallet;
	private boolean temporalWallet;
	private LocalDateTime switchTime;
>>>>>>> master

}
