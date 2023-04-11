/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.wayapaychat.temporalwallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.entity.TranasctionPropertie;

/**
 *
 * @author Olawale
 */
@Repository
public interface TransactionPropertyRepository extends JpaRepository<TranasctionPropertie, Long>{
    
    @Query("select q from TranasctionPropertie q where q.userId =: userId")
    TranasctionPropertie findUserById(long userId);
    
}
