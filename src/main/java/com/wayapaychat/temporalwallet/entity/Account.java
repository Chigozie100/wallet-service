package com.wayapaychat.temporalwallet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wayapaychat.temporalwallet.enumm.AccountType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.List;


@Entity
@Data
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNo;


    @Column(unique = true, nullable = false)
    private String accountName;

    private double balance = 0.00;

    @Enumerated(EnumType.ORDINAL)
    private AccountType accountType = AccountType.SAVINGS;

    @JsonIgnore
    @ManyToOne
    private User user;

    @JsonIgnore
    @OneToMany
    private List<Transaction> transactions;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

}