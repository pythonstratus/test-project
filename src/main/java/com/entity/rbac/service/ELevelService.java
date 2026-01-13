package com.entity.rbac.service;

import com.entity.rbac.dto.RbacDTO.ELevelDTO;
import com.entity.rbac.dto.RbacDTO.ELevelDefinitionDTO;
import com.entity.rbac.entity.Entemp;
import com.entity.rbac.repository.EntempRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for ELEVEL operations
 * Retrieves and interprets ELEVEL values
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ELevelService {

    private final EntempRepository entempRepository;

    // ELEVEL definitions
    private static final Map<Integer, ELevelDefinitionDTO> ELEVEL_DEFINITIONS = Map.of(
            0, ELevelDefinitionDTO.builder()
                    .elevel(0).name("National")
                    .description("Full system access")
                    .dataAccessScope("All Areas, All PODs")
                    .accessibleMenus(List.of("VIEWS", "REPORTS", "CHANGE_ACCESS", "END_OF_MONTH", "REALIGNMENT"))
                    .build(),
            2, ELevelDefinitionDTO.builder()
                    .elevel(2).name("Area")
                    .description("Area-level access")
                    .dataAccessScope("All PODs within assigned Area")
                    .accessibleMenus(List.of("VIEWS", "REPORTS", "CHANGE_ACCESS", "END_OF_MONTH", "REALIGNMENT"))
                    .build(),
            4, ELevelDefinitionDTO.builder()
                    .elevel(4).name("Territory")
                    .description("Territory-level access")
                    .dataAccessScope("All PODs within Territory")
                    .accessibleMenus(List.of("VIEWS", "REPORTS", "CHANGE_ACCESS", "END_OF_MONTH", "REALIGNMENT"))
                    .build(),
            6, ELevelDefinitionDTO.builder()
                    .elevel(6).name("Group Manager")
                    .description("Group Manager access")
                    .dataAccessScope("All employees within assigned Group/POD")
                    .accessibleMenus(List.of("VIEWS", "REPORTS", "CHANGE_ACCESS", "END_OF_MONTH", "CASE_ASSIGNMENT", "TIME_VERIFICATION"))
                    .build(),
            7, ELevelDefinitionDTO.builder()
                    .elevel(7).name("Acting Group Manager")
                    .description("Acting GM access")
                    .dataAccessScope("All employees within assigned Group/POD")
                    .accessibleMenus(List.of("VIEWS", "REPORTS", "CHANGE_ACCESS", "END_OF_MONTH", "CASE_ASSIGNMENT", "TIME_VERIFICATION"))
                    .build(),
            8, ELevelDefinitionDTO.builder()
                    .elevel(8).name("Employee")
                    .description("Standard employee access")
                    .dataAccessScope("Own assigned cases only")
                    .accessibleMenus(List.of("VIEWS", "REPORTS", "CHANGE_ACCESS", "END_OF_MONTH"))
                    .build(),
            -1, ELevelDefinitionDTO.builder()
                    .elevel(-1).name("Not Supported")
                    .description("Title/ICS combination not supported")
                    .dataAccessScope("No data access")
                    .accessibleMenus(List.of())
                    .build(),
            -2, ELevelDefinitionDTO.builder()
                    .elevel(-2).name("Blocked/Vacant")
                    .description("User is blocked or position is vacant")
                    .dataAccessScope("No data access")
                    .accessibleMenus(List.of())
                    .build()
    );

    /**
     * Get ELEVEL for a user with fallback strategy
     */
    public Integer getELevel(String seid) {
        log.debug("Getting ELEVEL for SEID: {}", seid);

        // Try to find using priority order
        List<Entemp> assignments = entempRepository.findAssignmentsWithPriority(seid);

        if (assignments.isEmpty()) {
            log.warn("No assignments found for SEID: {}", seid);
            return -2; // Blocked/Vacant
        }

        // Return the ELEVEL from the highest priority assignment
        Integer elevel = assignments.get(0).getElevel();
        log.debug("Found ELEVEL {} for SEID {}", elevel, seid);
        return elevel != null ? elevel : -2;
    }

    /**
     * Get ELEVEL name
     */
    public String getELevelName(Integer elevel) {
        if (elevel == null) return "Unknown";
        ELevelDefinitionDTO def = ELEVEL_DEFINITIONS.get(elevel);
        return def != null ? def.getName() : "Unknown (" + elevel + ")";
    }

    /**
     * Get ELEVEL description
     */
    public String getELevelDescription(Integer elevel) {
        if (elevel == null) return "Unknown";
        ELevelDefinitionDTO def = ELEVEL_DEFINITIONS.get(elevel);
        return def != null ? def.getDescription() : "Unknown";
    }

    /**
     * Get full ELEVEL DTO for a user
     */
    public ELevelDTO getELevelDTO(String seid) {
        Integer elevel = getELevel(seid);
        ELevelDefinitionDTO def = ELEVEL_DEFINITIONS.get(elevel);

        return ELevelDTO.builder()
                .seid(seid)
                .elevel(elevel)
                .elevelName(def != null ? def.getName() : "Unknown")
                .description(def != null ? def.getDescription() : "Unknown")
                .dataAccessScope(def != null ? def.getDataAccessScope() : "Unknown")
                .build();
    }

    /**
     * Get ELEVEL definition
     */
    public ELevelDefinitionDTO getELevelDefinition(Integer elevel) {
        return ELEVEL_DEFINITIONS.get(elevel);
    }

    /**
     * Get all ELEVEL definitions
     */
    public List<ELevelDefinitionDTO> getAllELevelDefinitions() {
        return new ArrayList<>(ELEVEL_DEFINITIONS.values());
    }

    /**
     * Check if ELEVEL is valid (not blocked or unsupported)
     */
    public boolean isValidELevel(Integer elevel) {
        return elevel != null && elevel >= 0;
    }
}
