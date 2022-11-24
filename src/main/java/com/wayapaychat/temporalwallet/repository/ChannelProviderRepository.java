package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.ChannelProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChannelProviderRepository extends JpaRepository<ChannelProvider, Long> {

    @Query("select c from ChannelProvider c where c.name =:#{#name}")
    Optional<ChannelProvider> findByName(String name);

    @Query("select c from ChannelProvider c where c.isActive = true")
    Optional<ChannelProvider> findByActive();
}
