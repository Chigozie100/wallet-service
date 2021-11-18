package com.wayapaychat.temporalwallet.entity;

import java.time.LocalDateTime;

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
@Table(name = "m_wallet_switch")
public class SwitchWallet {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
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

}
