package com.entity.rbac.repository;

import com.entity.rbac.entity.Enttitles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ENTTITLES table
 * Decision table for ELEVEL lookup
 */
@Repository
public interface EnttitlesRepository extends JpaRepository<Enttitles, Enttitles.EnttitlesId> {

    /**
     * Find ELEVEL by ICS Access Level and Title
     */
    @Query("SELECT e.elevel FROM Enttitles e WHERE e.icsacclevel = :icsacc AND TRIM(e.title) = TRIM(:title)")
    Optional<Integer> findElevelByIcsaccAndTitle(@Param("icsacc") String icsacc, @Param("title") String title);

    /**
     * Find all entries for an ICS Access Level
     */
    List<Enttitles> findByIcsacclevel(String icsacclevel);

    /**
     * Find all entries for an ELEVEL
     */
    List<Enttitles> findByElevel(Integer elevel);
}
