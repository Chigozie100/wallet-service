package com.wayapaychat.temporalwallet.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VirtualAccountSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Long id;
    private String bank;
    private String bankCode;


    private String virtualAccountCode;

    @Column(unique = true, nullable = false)
    private String callbackUrl;

    @Column(unique = true, nullable = false)
    private String accountNo;

    private Long merchantId;

    private String email;


}
