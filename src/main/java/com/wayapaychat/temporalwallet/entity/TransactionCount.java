package com.wayapaychat.temporalwallet.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "m_wallet_transaction_count")
public class TransactionCount implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    private String transactionReference;
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;


}
