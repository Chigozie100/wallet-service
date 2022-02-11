package com.wayapaychat.temporalwallet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wayapaychat.temporalwallet.entity.Provider;


@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {
    List<Provider> findByIsActive(boolean isActive);
}
