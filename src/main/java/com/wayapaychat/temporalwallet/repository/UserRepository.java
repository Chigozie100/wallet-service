package com.wayapaychat.temporalwallet.repository;

import com.wayapaychat.temporalwallet.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
=======
>>>>>>> master
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
<<<<<<< HEAD
    Optional<Users> findByEmailAddress(String email);
    Optional<Users> findByUserId(Long id);
    
    @Query(value = "SELECT _user FROM Users _user " + "WHERE UPPER(_user.emailAddress) = UPPER(:value) OR "
			+ "_user.mobileNo LIKE CONCAT('%', :value) ")
	Optional<Users> findByEmailOrPhoneNumber(@Param("value") String value);
    
=======
    Optional<Users> findByUserId(long userId);
    Optional<Users> findByEmailAddress(String email);
>>>>>>> master
}
