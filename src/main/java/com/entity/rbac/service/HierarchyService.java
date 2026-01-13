package com.entity.rbac.service;

import com.entity.rbac.dto.RbacDTO.*;
import com.entity.rbac.entity.Entemp;
import com.entity.rbac.repository.EntempRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Hierarchy operations
 * Handles Area/POD data scoping based on ELEVEL
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HierarchyService {

    private final EntempRepository entempRepository;
    private final ELevelService eLevelService;

    // Area names
    private static final Map<String, String> AREA_NAMES = Map.of(
            "21", "Area 21 - Northeast",
            "22", "Area 22 - Mid-Atlantic",
            "23", "Area 23 - Southeast",
            "24", "Area 24 - Central",
            "25", "Area 25 - Southwest",
            "26", "Area 26 - Western",
            "27", "Area 27 - Northwest",
            "35", "Area 35 - Special Operations"
    );

    /**
     * Get hierarchy access info for a user
     */
    public HierarchyAccessDTO getHierarchyAccess(String seid) {
        log.debug("Getting hierarchy access for SEID: {}", seid);

        Integer elevel = eLevelService.getELevel(seid);
        Optional<Entemp> assignment = entempRepository.findCurrentActiveAssignment(seid);

        List<String> accessibleAreas = new ArrayList<>();
        List<String> accessiblePods = new ArrayList<>();
        String scopeDescription;

        if (elevel == null || elevel < 0) {
            scopeDescription = "No data access";
        } else if (elevel == 0) {
            // National - all areas
            accessibleAreas = new ArrayList<>(AREA_NAMES.keySet());
            Collections.sort(accessibleAreas);
            scopeDescription = "National access - All Areas";
        } else if (assignment.isPresent()) {
            Entemp e = assignment.get();
            String areaCode = e.getAreacd() != null ? String.format("%02d", e.getAreacd()) : null;
            String podCode = e.getTrimmedPodcd();

            switch (elevel) {
                case 2 -> {
                    // Area level
                    if (areaCode != null) accessibleAreas.add(areaCode);
                    accessiblePods = getPodsForArea(e.getAreacd());
                    scopeDescription = "Area " + areaCode + " - All PODs";
                }
                case 4 -> {
                    // Territory level
                    if (areaCode != null) accessibleAreas.add(areaCode);
                    if (podCode != null && podCode.length() >= 2) {
                        accessiblePods = getPodsForTerritory(e.getAreacd(), podCode.substring(0, 2));
                    }
                    scopeDescription = "Territory access within Area " + areaCode;
                }
                case 6, 7 -> {
                    // Group Manager level
                    if (areaCode != null) accessibleAreas.add(areaCode);
                    if (podCode != null) accessiblePods.add(podCode);
                    scopeDescription = "Group " + podCode + " in Area " + areaCode;
                }
                default -> {
                    // Employee level (8)
                    if (areaCode != null) accessibleAreas.add(areaCode);
                    if (podCode != null) accessiblePods.add(podCode);
                    scopeDescription = "Own assignments only";
                }
            }
        } else {
            scopeDescription = "Unknown access scope";
        }

        return HierarchyAccessDTO.builder()
                .seid(seid)
                .elevel(elevel)
                .accessibleAreaCodes(accessibleAreas)
                .accessiblePodCodes(accessiblePods)
                .scopeDescription(scopeDescription)
                .build();
    }

    /**
     * Get all areas with employee counts
     */
    public List<AreaDTO> getAllAreas() {
        List<Object[]> areaCounts = entempRepository.getAreaCountsWithTerritories();

        return areaCounts.stream()
                .map(row -> {
                    Integer areacd = ((Number) row[0]).intValue();
                    String areaCode = String.format("%02d", areacd);
                    return AreaDTO.builder()
                            .areaCode(areaCode)
                            .areaName(AREA_NAMES.getOrDefault(areaCode, "Area " + areaCode))
                            .employeeCount(((Number) row[1]).intValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get PODs for an area
     */
    public List<String> getPodsForArea(Integer areacd) {
        if (areacd == null) return List.of();
        return entempRepository.findDistinctPodcdByAreacd(areacd);
    }

    /**
     * Get PODs for a territory
     */
    public List<String> getPodsForTerritory(Integer areacd, String territoryPrefix) {
        if (areacd == null || territoryPrefix == null) return List.of();
        return entempRepository.findDistinctPodcdByAreacd(areacd).stream()
                .filter(pod -> pod != null && pod.startsWith(territoryPrefix))
                .collect(Collectors.toList());
    }

    /**
     * Get accessible areas for a user
     */
    public List<AreaDTO> getAccessibleAreas(String seid) {
        HierarchyAccessDTO access = getHierarchyAccess(seid);
        List<AreaDTO> allAreas = getAllAreas();

        return allAreas.stream()
                .filter(area -> access.getAccessibleAreaCodes().contains(area.getAreaCode()))
                .collect(Collectors.toList());
    }

    /**
     * Check if user can access an area
     */
    public boolean canAccessArea(String seid, String areaCode) {
        HierarchyAccessDTO access = getHierarchyAccess(seid);
        return access.getAccessibleAreaCodes().contains(areaCode);
    }

    /**
     * Check if user can access a POD
     */
    public boolean canAccessPod(String seid, String podCode) {
        HierarchyAccessDTO access = getHierarchyAccess(seid);
        // For national (ELEVEL 0), can access all PODs
        if (access.getElevel() != null && access.getElevel() == 0) {
            return true;
        }
        return access.getAccessiblePodCodes().contains(podCode);
    }
}
