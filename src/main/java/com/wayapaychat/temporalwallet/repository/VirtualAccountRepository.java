package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.VirtualAccountHook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VirtualAccountRepository extends JpaRepository<VirtualAccountHook, Long> {

    @Override
    Optional<VirtualAccountHook> findById(Long aLong);

    Optional<VirtualAccountHook> findByUsernameAndPassword(String username, String password);

}
