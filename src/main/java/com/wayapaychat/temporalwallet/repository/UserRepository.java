/*
 * package com.wayapaychat.temporalwallet.repository;
 * 
 * import com.wayapaychat.temporalwallet.entity.Users; import
 * org.springframework.data.jpa.repository.JpaRepository; import
 * org.springframework.data.jpa.repository.Query; import
 * org.springframework.data.repository.query.Param; import
 * org.springframework.stereotype.Repository;
 * 
 * import java.util.Optional;
 * 
 * @Repository public interface UserRepository extends JpaRepository<Users,
 * Long> { Optional<Users> findByEmailAddress(String email); Optional<Users>
 * findByUserId(Long id);
 * 
 * @Query(value = "SELECT _user FROM Users _user " +
 * "WHERE UPPER(_user.emailAddress) = UPPER(:value) OR " +
 * "_user.mobileNo LIKE CONCAT('%', :value) ") Optional<Users>
 * findByEmailOrPhoneNumber(@Param("value") String value);
 * 
 * }
 */