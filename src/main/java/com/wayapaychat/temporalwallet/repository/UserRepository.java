package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
}
