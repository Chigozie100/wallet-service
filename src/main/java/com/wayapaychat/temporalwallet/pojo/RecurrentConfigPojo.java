package com.wayapaychat.temporalwallet.pojo;

import com.wayapaychat.temporalwallet.entity.RecurrentConfig;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class RecurrentConfigPojo {
    @NotNull
    private String officialAccountNumber;

    @NotNull
    private BigDecimal amount;
    @NotNull
    private Date payDate;
    @NotNull
    private Integer interval;
    @NotNull
    private RecurrentConfig.Duration duration;
}
