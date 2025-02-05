package com.redshift.user_management_system.repository;

import com.redshift.user_management_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
//    Optional<User> findByMailId(String mailId);
//    Optional<User> findByUsername(String userName);
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.mailId = :identifier")
    Optional<User> findByUsernameOrMailId(@Param("identifier") String identifier);
}
