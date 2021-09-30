package com.wayapaychat.temporalwallet.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "m_wallet_sms")
public class WalletTransactionNotification {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	private Long id;
	
	private String debitAccountNo;
	
	private String creditAccountNo;
	
	private String providerId;
	
	private String tranMessage;
	
	private String debitMobileNo;
	
	private String creditMobileNo;
	
	@CreationTimestamp
    @ApiModelProperty(hidden = true)
    private LocalDateTime createdSmsDate;

    private LocalDateTime deliveredSmsDate;
    //Acct:**3923\r\nDR:N20000.00\r\nDesc:ATM WITHDRAWAL BY EMMANUEL NJOKU\r\nDT:26/JUL/21 16:23PM\r\nBal:N1,314.81CR\r\nHelp:08030907963\r\nDial*755*03# for a loan

	public WalletTransactionNotification(String debitAccountNo, String creditAccountNo,
			String tranMessage, String debitMobileNo, String creditMobileNo) {
		super();
		this.debitAccountNo = debitAccountNo;
		this.creditAccountNo = creditAccountNo;
		this.tranMessage = tranMessage;
		this.debitMobileNo = debitMobileNo;
		this.creditMobileNo = creditMobileNo;
		this.createdSmsDate = LocalDateTime.now();
	}
    
}
