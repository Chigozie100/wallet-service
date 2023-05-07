/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.wayapaychat.temporalwallet.response;

import lombok.Data;

/**
 *
 * @author Olawale
 */
@Data
public class TransactionAnalysis {
    
    private CategoryAnalysis categoryAnalysis;
    private OverallAnalysis overallAnalysis;
}
