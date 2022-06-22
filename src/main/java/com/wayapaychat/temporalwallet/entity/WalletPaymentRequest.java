package com.wayapaychat.temporalwallet.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.wayapaychat.temporalwallet.dto.PaymentRequest;
import com.wayapaychat.temporalwallet.enumm.CategoryType;
import com.wayapaychat.temporalwallet.enumm.PaymentRequestStatus;

@Data
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "m_wallet_payment_request")
public class WalletPaymentRequest {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	private Long id;

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

    @Enumerated(EnumType.STRING)
    private PaymentRequestStatus status;

    private boolean rejected;

    private boolean wayauser;

    @NotBlank(message = "please specify the reason for this payment request")
    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(unique = true, nullable = false)
    private String reference;

    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Enumerated(EnumType.STRING)
    private CategoryType category;

	public WalletPaymentRequest(
			@NotBlank(message = "please enter the receiver's email") @Email(message = "please enter a valid email") String receiverEmail,
			@NotBlank(message = "please enter the receiver's phone number") String receiverPhoneNumber,
			@NotBlank(message = "please enter the receiver's name ") String receiverName,
			@NotBlank(message = "please enter the receivers id") String receiverId,
			@NotBlank(message = "please enter the senders id") String senderId,
			@NotNull(message = "please enter the amount") BigDecimal amount, boolean deleted,
			PaymentRequestStatus status, boolean rejected, boolean wayauser,
			@NotBlank(message = "please specify the reason for this payment request") String reason, String reference,
			LocalDateTime createdAt, CategoryType category) {
		super();
		this.receiverEmail = receiverEmail;
		this.receiverPhoneNumber = receiverPhoneNumber;
		this.receiverName = receiverName;
		this.receiverId = receiverId;
		this.senderId = senderId;
		this.amount = amount;
		this.deleted = deleted;
		this.status = status;
		this.rejected = rejected;
		this.wayauser = wayauser;
		this.reason = reason;
		this.reference = reference;
		this.createdAt = createdAt;
		this.category = category;
	}
	
	public WalletPaymentRequest(PaymentRequest request) {
		super();
		this.receiverEmail = request.getReceiverEmail();
		this.receiverPhoneNumber = request.getReceiverPhoneNumber();
		this.receiverName = request.getReceiverName();
		this.receiverId = request.getReceiverId();
		this.senderId = request.getSenderId();
		this.amount = request.getAmount();
		this.deleted = request.isDeleted();
		this.status = request.getStatus();
		this.rejected = request.isRejected();
		this.wayauser = request.isWayauser();
		this.reason = request.getReason();
		this.reference = request.getReference();
		this.createdAt = request.getCreatedAt();
		this.category = request.getTransactionCategory();
	}
    

}
