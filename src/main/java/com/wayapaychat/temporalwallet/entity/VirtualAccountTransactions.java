package com.wayapaychat.temporalwallet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wayapaychat.temporalwallet.enumm.WalletTransStatus;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class VirtualAccountTransactions {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Long id;

    @NotNull
    private String tranId;

    @Column(name = "beneficiary_name")
    private String beneficiaryName;

    @Column(name = "sender_name")
    private String senderName;

    @NotNull
    private String creditAccountNumber;

    @NotNull
    @JsonIgnore
    private String debitAccountNumber;

    @NotNull
    private BigDecimal tranAmount;

    private BigDecimal chargeAmount;

    private BigDecimal vatAmount;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updateAt;

    private String transactionType;

    private String tranCrncy;

    private String eventId;

    @Enumerated(EnumType.STRING)
    private WalletTransStatus status;  // PENDING  // REVERSED  // SUCCESSFUL
}
