package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.wayapaychat.temporalwallet.enumm.NonWayaPaymentStatusAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class NonWayaRedeemDTO {
	
	//@NotBlank(message = "EmailOrPhone must not be Null or Blank")
	//@Size(min=1, max=50, message = "EmailOrPhone must be between 1 to 50 digit")
    //private String emailOrPhoneNo;
	
//	@NotNull(message = "Merchant ID must not be Null or Blank")
//    private Long merchantId;
//
//	@NotNull
//	@Min(value = 100, message ="Amount must be greater or equal to 1000")
//    private BigDecimal amount;
//
//	@NotBlank(message = "tranCrncy must not Null or Blank")
//	@Size(min=3, max=5, message = "tranCrncy must be 3 alphanumeric (NGN)")
//    private String tranCrncy;
	
	@NotBlank(message = "Token must not Null or Blank")
	@Size(min=3, max=20, message = "Token must be between 1 to 20 digit")
    private String token;
	
//	@NotBlank(message = "Transaction Status must not Null or Blank")
//	@Size(min=3, max=10, message = "Transaction Status must be between 1 to 20 digit")
//    private String tranStatus;

	@NotBlank(message = "status action must not Null or Blank")
	private NonWayaPaymentStatusAction statusAction = NonWayaPaymentStatusAction.ACCEPTED;

	@NotBlank(message = "Receiver Name can not be Null Or Blank")
	private String receiverName;

	@NotBlank(message = "Receiver Name can not be Null Or Blank")
	private String beneficiaryProfileId;

	@NotBlank(message = "Receiver Name can not be Null Or Blank")
	private String beneficiaryUserId;

//	@NotBlank(message = "Sender Name can not be Blank or Null")
//	private String senderName;

//	public NonWayaRedeemDTO(
//			Long merchantId,
//			BigDecimal amount,
//			String tranCrncy,
//			String token,
//			String tranStatus,String receiverName, String senderName) {
//		super();
//		//this.emailOrPhoneNo = emailOrPhoneNo;
//		this.merchantId = merchantId;
//		this.amount = amount;
//		this.tranCrncy = tranCrncy;
//		this.token = token;
//		this.tranStatus = tranStatus;
//		this.receiverName = receiverName;
//		this.senderName = senderName;
//	}

	
}
