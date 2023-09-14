package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SendMoneyToEmailOrPhone {
 
    private BigDecimal amount;
    @NotBlank(message = "Account must not Null or Blank")
	@Size(min=10, max=15, message = "Account must be 10 digit")
	private String senderAccountNumber;
    private String senderUserId;
    private String emailOrPhone;
    private String paymentReference;
    private String tranNarration;
    private String tranCrncy;
	private String tranType;
	private String transactionCategory;
    private Boolean isAccountNumber;
    private String senderName;
    private String receiverName;
    private String senderProfileId;
			
}
