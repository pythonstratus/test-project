package com.entity.rbac.service;

import com.entity.rbac.dto.RbacDTO.StaffInfoDTO;
import com.entity.rbac.dto.RbacDTO.StaffOrgDTO;
import com.entity.rbac.entity.Entemp;
import com.entity.rbac.repository.EntempRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for Staff-related operations
 * Staff detection is based on ROID prefix 859062, NOT the ISSTAFF field
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StaffService {

    private final EntempRepository entempRepository;

    // Staff ROID prefix
    private static final String STAFF_ROID_PREFIX = "859062";

    // Valid staff ORG codes
    private static final List<StaffOrgDTO> STAFF_ORGS = List.of(
            StaffOrgDTO.builder().code("CF").name("Field Collection").description("Collection Field Operations").build(),
            StaffOrgDTO.builder().code("CP").name("Compliance").description("Compliance Operations").build(),
            StaffOrgDTO.builder().code("WI").name("Wage & Investment").description("Wage & Investment Division").build(),
            StaffOrgDTO.builder().code("AD").name("Advisory").description("Advisory Services").build()
    );

    /**
     * Check if user is staff based on ROID prefix
     * This is the PRIMARY method for staff detection
     */
    public boolean isStaff(String seid) {
        log.debug("Checking if user {} is staff", seid);
        int staffCount = entempRepository.countStaffAssignments(seid);
        boolean isStaff = staffCount > 0;
        log.debug("User {} staff status: {} (found {} staff assignments)", seid, isStaff, staffCount);
        return isStaff;
    }

    /**
     * Get staff info for a user
     */
    public StaffInfoDTO getStaffInfo(String seid) {
        log.debug("Getting staff info for user: {}", seid);

        List<Entemp> staffAssignments = entempRepository.findStaffAssignments(seid);
        boolean isStaff = !staffAssignments.isEmpty();

        String staffRoid = null;
        String currentOrg = null;

        if (isStaff && !staffAssignments.isEmpty()) {
            Entemp staffAssignment = staffAssignments.get(0);
            staffRoid = String.valueOf(staffAssignment.getRoid());
            currentOrg = staffAssignment.getOrg() != null ? staffAssignment.getOrg().trim() : "CF";
        }

        return StaffInfoDTO.builder()
                .seid(seid)
                .isStaff(isStaff)
                .staffRoid(staffRoid)
                .currentOrg(currentOrg)
                .availableOrgs(isStaff ? List.of("CF", "CP", "WI", "AD") : List.of())
                .hasUtilitiesAccess(isStaff)
                .hasRealignmentAccess(isStaff)
                .build();
    }

    /**
     * Get staff ORG for a user
     */
    public String getStaffOrg(String seid) {
        List<Entemp> staffAssignments = entempRepository.findStaffAssignments(seid);
        if (staffAssignments.isEmpty()) {
            return null;
        }
        String org = staffAssignments.get(0).getOrg();
        return org != null ? org.trim() : "CF";
    }

    /**
     * Update staff ORG
     */
    @Transactional
    public boolean updateStaffOrg(String seid, String newOrg) {
        log.info("Updating staff ORG for user {} to {}", seid, newOrg);

        // Validate ORG code
        boolean validOrg = STAFF_ORGS.stream().anyMatch(o -> o.getCode().equals(newOrg));
        if (!validOrg) {
            log.warn("Invalid ORG code: {}", newOrg);
            return false;
        }

        List<Entemp> staffAssignments = entempRepository.findStaffAssignments(seid);
        if (staffAssignments.isEmpty()) {
            log.warn("User {} is not staff, cannot update ORG", seid);
            return false;
        }

        // Update ORG for all staff assignments
        for (Entemp assignment : staffAssignments) {
            entempRepository.updateOrg(assignment.getRoid(), newOrg);
        }

        log.info("Successfully updated staff ORG for user {} to {}", seid, newOrg);
        return true;
    }

    /**
     * Get available staff ORG codes
     */
    public List<StaffOrgDTO> getAvailableOrgs() {
        return STAFF_ORGS;
    }

    /**
     * Check if a ROID is a staff ROID
     */
    public boolean isStaffRoid(Long roid) {
        if (roid == null) return false;
        return String.valueOf(roid).startsWith(STAFF_ROID_PREFIX);
    }

    /**
     * Check if a ROID string is a staff ROID
     */
    public boolean isStaffRoid(String roid) {
        if (roid == null) return false;
        return roid.startsWith(STAFF_ROID_PREFIX);
    }
}
