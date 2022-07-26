package com.wayapaychat.temporalwallet.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "m_recurrent_config")
public class RecurrentConfig {

    public enum Duration{DAY, MONTH, YEAR};

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Long id;

    private String officialAccountNumber;
    private boolean isActive;
    private boolean recurring = true;
    private BigDecimal amount;
    private Date payDate;
    private Integer interval;
    private Duration duration;
}
