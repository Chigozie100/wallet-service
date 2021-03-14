package com.wayapaychat.temporalwallet.entity;

import com.wayapaychat.temporalwallet.enumm.TransactionType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;


@Entity
@Data
@NoArgsConstructor
public class Transactions {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String refCode;

    private String description;

    @Enumerated(EnumType.ORDINAL)
    private TransactionType transactionType;

    @ManyToOne
    Accounts account;

    double amount;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

}

