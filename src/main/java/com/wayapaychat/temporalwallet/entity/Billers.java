package com.wayapaychat.temporalwallet.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
public class Billers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String billerAggregatorCode;
    private String billerWayaPayCode;
    private boolean active;
    private Long categoryId;
    private String categoryName;
    private String categoryCode;
    private String aggregatorName;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

}