package com.wayapaychat.temporalwallet.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;


@Data
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VirtualAccountHook {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Long id;
    private String bank;
    private String bankCode;

    @Column(unique = true, nullable = false)
    private String virtualAccountCode;

    @Column(unique = true, nullable = false)
    private String callbackUrl;

    @Column(unique = true, nullable = false)
    private String username;
    private String password;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
