package com.wayapaychat.temporalwallet.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.wayapaychat.temporalwallet.enumm.PaymentStatus;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "m_wallet_qrcode_payment", uniqueConstraints = {
		@UniqueConstraint(name = "UniqueRefNoAndDelFlgAndDate", columnNames = { "referenceNo",
				"del_flg", "tranDate"}) })
public class WalletQRCodePayment {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(unique = true, nullable = false)
	private Long id;

	private boolean del_flg = false;

	private boolean process_flg = false;

	private String fullName;

	private BigDecimal amount;

	private String reason;

	private String referenceNo;
	
	@NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@JsonDeserialize(using = LocalDateDeserializer.class)
	@JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate tranDate;

	@Enumerated(EnumType.STRING)
	private PaymentStatus status;

	@Column(nullable = false)
	private Long payeeId;

	private Long payerId;

	@Column(nullable = false)
	private String crncyCode;

	@CreationTimestamp
	@ApiModelProperty(hidden = true)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	private LocalDateTime createdAt;

	@CreationTimestamp
	@ApiModelProperty(hidden = true)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	private LocalDateTime updatedAt;

	private String payerBy;

	private String payerEmail;

	private LocalDateTime payerAt;

	public WalletQRCodePayment(String fullName, BigDecimal amount, String reason, String referenceNo,
			@NotNull LocalDate tranDate, PaymentStatus status, Long payeeId, String crncyCode) {
		super();
		this.fullName = fullName;
		this.amount = amount;
		this.reason = reason;
		this.referenceNo = referenceNo;
		this.tranDate = tranDate;
		this.status = status;
		this.payeeId = payeeId;
		this.crncyCode = crncyCode;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}
	

}
