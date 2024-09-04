package com.wayapaychat.temporalwallet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VirtualAccountSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Long id;
    private String virtualAccountCode;

    @Column(unique = true, nullable = false)
    private String callbackUrl;

    @Column(unique = true, nullable = false)
    private String accountNo;

    private String businessId;

    private String email;


}
