package com.entity.rbac.repository;

import com.entity.rbac.entity.EntityUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for ENTITY_USER table
 */
@Repository
public interface EntityUserRepository extends JpaRepository<EntityUser, String> {

    /**
     * Find by SEID (trimmed comparison)
     */
    @Query("SELECT u FROM EntityUser u WHERE TRIM(u.userSeid) = TRIM(:seid)")
    Optional<EntityUser> findByUserSeid(@Param("seid") String seid);

    /**
     * Check if user is locked
     */
    @Query("SELECT CASE WHEN u.islocked = 'Y' THEN true ELSE false END " +
           "FROM EntityUser u WHERE TRIM(u.userSeid) = TRIM(:seid)")
    boolean isUserLocked(@Param("seid") String seid);
}
