package com.wayapaychat.temporalwallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.wayapaychat.temporalwallet.entity.WalletProduct;

public interface WalletProductRepository extends JpaRepository<WalletProduct, Long> {
	
	@Query(value = "SELECT u FROM WalletProduct u " + "WHERE UPPER(u.productCode) = UPPER(:name) " + " AND u.glSubHeadCode = (:gl)"  + " AND u.del_flg = false")
	WalletProduct findByProductCode(String name, String gl);

}
