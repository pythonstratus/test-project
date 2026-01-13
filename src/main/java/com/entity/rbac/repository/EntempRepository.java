package com.entity.rbac.repository;

import com.entity.rbac.entity.Entemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ENTEMP table
 * Handles employee data, assignments, and hierarchy queries
 */
@Repository
public interface EntempRepository extends JpaRepository<Entemp, Long> {

    // ==================== Basic Queries ====================

    /**
     * Find by SEID (trimmed comparison)
     */
    @Query("SELECT e FROM Entemp e WHERE TRIM(e.seid) = TRIM(:seid)")
    List<Entemp> findBySeid(@Param("seid") String seid);

    /**
     * Find by ROID
     */
    Optional<Entemp> findByRoid(Long roid);

    /**
     * Find by ROID and SEID
     */
    @Query("SELECT e FROM Entemp e WHERE e.roid = :roid AND TRIM(e.seid) = TRIM(:seid)")
    Optional<Entemp> findByRoidAndSeid(@Param("roid") Long roid, @Param("seid") String seid);

    // ==================== Assignment Queries ====================

    /**
     * Find current active assignment (EACTIVE='A' AND PRIMARY_ROID='Y')
     * This is the primary method to get a user's current assignment
     */
    @Query("SELECT e FROM Entemp e WHERE TRIM(e.seid) = TRIM(:seid) " +
           "AND e.eactive = 'A' AND e.primaryRoid = 'Y' AND e.elevel > -2")
    Optional<Entemp> findCurrentActiveAssignment(@Param("seid") String seid);

    /**
     * Find all valid assignments for a user (ELEVEL > -2)
     * Used to populate role dropdowns
     */
    @Query("SELECT e FROM Entemp e WHERE TRIM(e.seid) = TRIM(:seid) AND e.elevel > -2 " +
           "ORDER BY e.roid")
    List<Entemp> findAllValidAssignments(@Param("seid") String seid);

    /**
     * Find any valid assignment with priority order:
     * 1. EACTIVE='A' AND PRIMARY_ROID='Y'
     * 2. PRIMARY_ROID='Y'
     * 3. EACTIVE='A'
     * 4. EACTIVE='Y'
     */
    @Query("SELECT e FROM Entemp e WHERE TRIM(e.seid) = TRIM(:seid) AND e.elevel > -2 " +
           "ORDER BY CASE " +
           "  WHEN e.eactive = 'A' AND e.primaryRoid = 'Y' THEN 1 " +
           "  WHEN e.primaryRoid = 'Y' THEN 2 " +
           "  WHEN e.eactive = 'A' THEN 3 " +
           "  WHEN e.eactive = 'Y' THEN 4 " +
           "  ELSE 5 END")
    List<Entemp> findAssignmentsWithPriority(@Param("seid") String seid);

    /**
     * Count assignments for a user
     */
    @Query("SELECT COUNT(e) FROM Entemp e WHERE TRIM(e.seid) = TRIM(:seid) AND e.elevel > -2")
    int countAssignmentsForUser(@Param("seid") String seid);

    // ==================== Assignment Switch Operations ====================

    /**
     * Reset all assignments for a user (set EACTIVE='Y', PRIMARY_ROID='N')
     * Step 1 of the two-step assignment switch
     */
    @Modifying
    @Query("UPDATE Entemp e SET e.eactive = 'Y', e.primaryRoid = 'N' " +
           "WHERE TRIM(e.seid) = TRIM(:seid) AND e.elevel > -2")
    int resetAllAssignmentsForUser(@Param("seid") String seid);

    /**
     * Activate a specific assignment (set EACTIVE='A', PRIMARY_ROID='Y')
     * Step 2 of the two-step assignment switch
     */
    @Modifying
    @Query("UPDATE Entemp e SET e.eactive = 'A', e.primaryRoid = 'Y' " +
           "WHERE e.roid = :roid AND TRIM(e.seid) = TRIM(:seid)")
    int activateAssignment(@Param("roid") Long roid, @Param("seid") String seid);

    /**
     * Update the ORG field for an assignment
     */
    @Modifying
    @Query("UPDATE Entemp e SET e.org = :org WHERE e.roid = :roid")
    int updateOrg(@Param("roid") Long roid, @Param("org") String org);

    // ==================== Staff Queries ====================

    /**
     * Find all staff assignments (ROID starts with 859062)
     */
    @Query(value = "SELECT * FROM ENTEMP WHERE TRIM(SEID) = TRIM(:seid) " +
                   "AND CAST(ROID AS VARCHAR(20)) LIKE '859062%' AND ELEVEL > -2",
           nativeQuery = true)
    List<Entemp> findStaffAssignments(@Param("seid") String seid);

    /**
     * Check if user has staff assignment
     */
    @Query(value = "SELECT COUNT(*) FROM ENTEMP WHERE TRIM(SEID) = TRIM(:seid) " +
                   "AND CAST(ROID AS VARCHAR(20)) LIKE '859062%' AND ELEVEL > -2",
           nativeQuery = true)
    int countStaffAssignments(@Param("seid") String seid);

    /**
     * Find all staff users
     */
    @Query(value = "SELECT * FROM ENTEMP WHERE CAST(ROID AS VARCHAR(20)) LIKE '859062%' " +
                   "AND ELEVEL > -2", nativeQuery = true)
    List<Entemp> findAllStaffUsers();

    // ==================== Hierarchy Navigation Queries ====================

    /**
     * Get distinct area codes with count of territories
     */
    @Query(value = "SELECT e.AREACD, COUNT(DISTINCT SUBSTR(e.PODCD, 1, 2)) as territory_count " +
                   "FROM ENTEMP e " +
                   "WHERE e.ELEVEL > -2 AND e.AREACD IS NOT NULL " +
                   "GROUP BY e.AREACD " +
                   "ORDER BY e.AREACD",
           nativeQuery = true)
    List<Object[]> getAreaCountsWithTerritories();

    /**
     * Count distinct areas
     */
    @Query(value = "SELECT COUNT(DISTINCT AREACD) FROM ENTEMP WHERE ELEVEL > -2 AND AREACD IS NOT NULL",
           nativeQuery = true)
    Integer countDistinctAreas();

    /**
     * Get territories within an area with count of groups
     */
    @Query(value = "SELECT SUBSTR(e.PODCD, 1, 2) as territory, COUNT(DISTINCT SUBSTR(e.PODCD, 1, 4)) as group_count " +
                   "FROM ENTEMP e " +
                   "WHERE e.AREACD = :areacd AND e.ELEVEL > -2 AND e.PODCD IS NOT NULL " +
                   "GROUP BY SUBSTR(e.PODCD, 1, 2) " +
                   "ORDER BY SUBSTR(e.PODCD, 1, 2)",
           nativeQuery = true)
    List<Object[]> getTerritoryCountsForArea(@Param("areacd") Integer areacd);

    /**
     * Count territories in an area
     */
    @Query(value = "SELECT COUNT(DISTINCT SUBSTR(PODCD, 1, 2)) FROM ENTEMP " +
                   "WHERE AREACD = :areacd AND ELEVEL > -2 AND PODCD IS NOT NULL",
           nativeQuery = true)
    Integer countTerritoriesInArea(@Param("areacd") Integer areacd);

    /**
     * Get groups within a territory with count of ROs
     */
    @Query(value = "SELECT SUBSTR(e.PODCD, 1, 4) as grp, COUNT(*) as ro_count " +
                   "FROM ENTEMP e " +
                   "WHERE e.AREACD = :areacd " +
                   "  AND SUBSTR(e.PODCD, 1, 2) = :territoryPrefix " +
                   "  AND e.ELEVEL > -2 AND e.PODCD IS NOT NULL " +
                   "GROUP BY SUBSTR(e.PODCD, 1, 4) " +
                   "ORDER BY SUBSTR(e.PODCD, 1, 4)",
           nativeQuery = true)
    List<Object[]> getGroupCountsForTerritory(@Param("areacd") Integer areacd,
                                               @Param("territoryPrefix") String territoryPrefix);

    /**
     * Count groups in a territory
     */
    @Query(value = "SELECT COUNT(DISTINCT SUBSTR(PODCD, 1, 4)) FROM ENTEMP " +
                   "WHERE AREACD = :areacd AND SUBSTR(PODCD, 1, 2) = :territoryPrefix " +
                   "AND ELEVEL > -2 AND PODCD IS NOT NULL",
           nativeQuery = true)
    Integer countGroupsInTerritory(@Param("areacd") Integer areacd,
                                   @Param("territoryPrefix") String territoryPrefix);

    /**
     * Find all employees in a group
     */
    @Query("SELECT e FROM Entemp e WHERE e.areacd = :areacd " +
           "AND e.podcd LIKE :podPrefix% AND e.elevel > -2")
    List<Entemp> findByAreacdAndPodcdStartingWith(@Param("areacd") Integer areacd,
                                                   @Param("podPrefix") String podPrefix);

    /**
     * Count ROs in a group
     */
    @Query(value = "SELECT COUNT(*) FROM ENTEMP " +
                   "WHERE AREACD = :areacd AND SUBSTR(PODCD, 1, 4) = :groupPrefix " +
                   "AND ELEVEL > -2",
           nativeQuery = true)
    Integer countROsInGroup(@Param("areacd") Integer areacd,
                            @Param("groupPrefix") String groupPrefix);

    // ==================== Search Queries ====================

    /**
     * Search by name (case-insensitive)
     */
    @Query("SELECT e FROM Entemp e WHERE UPPER(e.name) LIKE UPPER(CONCAT('%', :searchTerm, '%')) " +
           "AND e.elevel > -2")
    List<Entemp> findByNameContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    // ==================== Area/POD Queries ====================

    /**
     * Find all employees in an area
     */
    @Query("SELECT e FROM Entemp e WHERE e.areacd = :areacd AND e.elevel > -2")
    List<Entemp> findByAreacd(@Param("areacd") Integer areacd);

    /**
     * Get distinct POD codes for an area
     */
    @Query(value = "SELECT DISTINCT PODCD FROM ENTEMP WHERE AREACD = :areacd " +
                   "AND ELEVEL > -2 AND PODCD IS NOT NULL ORDER BY PODCD",
           nativeQuery = true)
    List<String> findDistinctPodcdByAreacd(@Param("areacd") Integer areacd);
}
