/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.wayapaychat.temporalwallet.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 *
 * @author Olawale
 */
@Data
public class Analysis {
    
   private Map<String, BigDecimal> sumResponse;
   private Map<String, String> countResponse;
}
