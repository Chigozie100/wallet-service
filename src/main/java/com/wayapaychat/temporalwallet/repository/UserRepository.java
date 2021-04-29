package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByUserId(long userId);
}
