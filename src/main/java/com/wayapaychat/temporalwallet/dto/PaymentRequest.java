package com.wayapaychat.temporalwallet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.wayapaychat.temporalwallet.enumm.CategoryType;
import com.wayapaychat.temporalwallet.enumm.PaymentRequestStatus;

import lombok.Data;

@Data
public class PaymentRequest {
	
	@NotBlank(message = "please enter the receiver's email")
    @Email(message = "please enter a valid email")
    private String receiverEmail;

    @NotBlank(message = "please enter the receiver's phone number")
    private String receiverPhoneNumber;

    @NotBlank(message = "please enter the receiver's name ")
    private String receiverName;

    @NotBlank(message = "please enter the receivers id")
    private String receiverId;

    @NotBlank(message = "please enter the senders id")
    private String senderId;

    @NotNull(message = "please enter the amount")
    private BigDecimal amount;
    //once the payment request is sent to kafka and after the maximum retires is exceeded change this field to true.
    // to avoid duplicates of this exact kind of payment request it means the request failed, we can still changed the field to failed
    private boolean deleted;

    private PaymentRequestStatus status;

    private boolean rejected;

    private boolean wayauser;

    private String reason;

    private String reference;
    
    private CategoryType transactionCategory;

    private LocalDateTime createdAt;

    private boolean isWayaOfficial = false;

}
