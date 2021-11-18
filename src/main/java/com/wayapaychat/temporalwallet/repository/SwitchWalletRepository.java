package com.wayapaychat.temporalwallet.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.entity.SwitchWallet;

@Repository
public interface SwitchWalletRepository extends JpaRepository<SwitchWallet, Long> {
	
	Optional<SwitchWallet> findBySwitchCode(String code);
	
	@Query(value = "SELECT u FROM SwitchWallet u " + "WHERE UPPER(u.switchIdentity) = UPPER(:value) ")
	List<SwitchWallet> findBySwitchIdent(String value);

}
