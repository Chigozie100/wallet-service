package com.wayapaychat.temporalwallet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wayapaychat.temporalwallet.enumm.AccountType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.List;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Accounts {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNo;

    @Column(nullable = false)
    private String accountName;

    private double balance = 0.00;
    private double lagerBalance = 0.00;

//    @Column(unique = true)
    private boolean isDefault = false;
//    @Column(unique = true)
    private boolean active;
    private boolean closed;
    private boolean approved;
    private boolean rejected;
    private boolean setSubmittedAndPendingApproval;
    private boolean withdrawnByApplicant;
    private String code;
    private String value;
    
    private Long productId;

    @Enumerated(EnumType.ORDINAL)
    private AccountType accountType = AccountType.SAVINGS;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private Users user;

    @JsonIgnore
    @OneToMany
    private List<Transactions> transactions;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

}