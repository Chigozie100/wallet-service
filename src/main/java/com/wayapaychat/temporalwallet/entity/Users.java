package com.wayapaychat.temporalwallet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.List;


@Entity
@Data
@NoArgsConstructor
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    private Long userId;

    @JsonIgnore
    @OneToMany
    private List<Account> accounts;

    @JsonIgnore
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

}
