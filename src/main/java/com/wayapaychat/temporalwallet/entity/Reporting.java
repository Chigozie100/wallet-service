/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.wayapaychat.temporalwallet.entity;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

/**
 *
 * @author Olawale
 */
@Entity
@Data
@Table(name = "reporting")
public class Reporting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNo;
    @Column(nullable = false)
    private String acct_name;

    private String accountType;

    private double cum_dr_amt;

    private double cum_cr_amt;
    
    private String event_id;
    
    private double trans_amt;
    
    private double charge_amount;
    
    private String trans_type;
    
    private double vat_amount;
    
    private String trans_ref;
    
    private String related_trans_id;
    
    private LocalDate trans_date;
    
    private String trans_category;
    
    private String trans_status;
}
