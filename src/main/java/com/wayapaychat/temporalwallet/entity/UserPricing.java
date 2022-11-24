package com.wayapaychat.temporalwallet.entity;

import com.wayapaychat.temporalwallet.enumm.PriceCategory;
import com.wayapaychat.temporalwallet.enumm.ProductPriceStatus;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
@Data
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Long id;

    private long userId;

    private BigDecimal generalAmount;

    private BigDecimal customAmount;

    private BigDecimal capPrice;

    private BigDecimal discount;

    private String fullName;

    private String product;

    private String code;

    @Enumerated(EnumType.STRING)
    private PriceCategory priceType = PriceCategory.FIXED;

    @Enumerated(EnumType.STRING)
    private ProductPriceStatus status;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;



}
