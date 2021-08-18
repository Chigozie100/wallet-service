package com.wayapaychat.temporalwallet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.entity.WalletProductCode;

@Repository
public interface WalletProductCodeRepository extends JpaRepository<WalletProductCode, Long> {
	
	@Query(value = "SELECT u FROM WalletProductCode u " + "WHERE UPPER(u.productCode) = UPPER(:name) " + " AND u.glSubHeadCode = (:gl)"  + " AND u.del_flg = false")
	WalletProductCode findByProductGLCode(String name, String gl);
	
	@Query(value = "SELECT u FROM WalletProductCode u " + "WHERE UPPER(u.productCode) = UPPER(:code) " + " AND u.del_flg = false")
	WalletProductCode findByProduct(String code);
	
	@Query(value = "SELECT u FROM WalletProductCode u " + "WHERE u.del_flg = false " + " AND u.productType IN ('SBA','CAA','ODA')")
	List<WalletProductCode> findByAllProduct();

}
