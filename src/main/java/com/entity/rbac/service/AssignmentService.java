package com.entity.rbac.service;

import com.entity.rbac.dto.RbacDTO.*;
import com.entity.rbac.entity.Entemp;
import com.entity.rbac.repository.EntempRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Assignment operations
 * Handles switching between multiple assignments for a user
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentService {

    private final EntempRepository entempRepository;
    private final ELevelService eLevelService;

    /**
     * Get all assignments for a user
     */
    public List<AssignmentDTO> getAssignments(String seid) {
        log.debug("Getting assignments for SEID: {}", seid);

        List<Entemp> assignments = entempRepository.findAllValidAssignments(seid);
        Optional<Entemp> current = entempRepository.findCurrentActiveAssignment(seid);
        Long currentRoid = current.map(Entemp::getRoid).orElse(null);

        return assignments.stream()
                .map(e -> buildAssignmentDTO(e, currentRoid))
                .collect(Collectors.toList());
    }

    /**
     * Get current active assignment
     */
    public Optional<AssignmentDTO> getCurrentAssignment(String seid) {
        return entempRepository.findCurrentActiveAssignment(seid)
                .map(e -> buildAssignmentDTO(e, e.getRoid()));
    }

    /**
     * Switch to a different assignment (two-step process)
     */
    @Transactional
    public ApiResponse<AssignmentDTO> switchAssignment(String seid, Long targetRoid) {
        log.info("User {} switching to assignment ROID: {}", seid, targetRoid);

        // Validate target assignment exists and belongs to user
        Optional<Entemp> targetOpt = entempRepository.findByRoidAndSeid(targetRoid, seid);
        if (targetOpt.isEmpty()) {
            log.warn("Assignment {} not found for user {}", targetRoid, seid);
            return ApiResponse.<AssignmentDTO>builder()
                    .success(false)
                    .message("Assignment not found or does not belong to you")
                    .errorCode("ASSIGNMENT_NOT_FOUND")
                    .build();
        }

        Entemp target = targetOpt.get();

        // Check if target is valid (ELEVEL > -2)
        if (target.getElevel() == null || target.getElevel() <= -2) {
            log.warn("Cannot switch to blocked/invalid assignment: {}", targetRoid);
            return ApiResponse.<AssignmentDTO>builder()
                    .success(false)
                    .message("Cannot switch to blocked or invalid assignment")
                    .errorCode("INVALID_ASSIGNMENT")
                    .build();
        }

        // Step 1: Reset ALL assignments for this user
        int resetCount = entempRepository.resetAllAssignmentsForUser(seid);
        log.debug("Reset {} assignments for user {}", resetCount, seid);

        // Step 2: Activate the selected assignment
        int activateCount = entempRepository.activateAssignment(targetRoid, seid);
        log.debug("Activated {} assignment(s) for ROID {}", activateCount, targetRoid);

        if (activateCount == 0) {
            log.error("Failed to activate assignment {} for user {}", targetRoid, seid);
            return ApiResponse.<AssignmentDTO>builder()
                    .success(false)
                    .message("Failed to activate assignment")
                    .errorCode("ACTIVATION_FAILED")
                    .build();
        }

        // Return the newly activated assignment
        AssignmentDTO newAssignment = buildAssignmentDTO(target, targetRoid);
        newAssignment.setEactive("A");
        newAssignment.setPrimaryRoid("Y");
        newAssignment.setCurrent(true);

        log.info("User {} successfully switched to assignment ROID: {}", seid, targetRoid);

        return ApiResponse.<AssignmentDTO>builder()
                .success(true)
                .message("Successfully switched to assignment: " + target.getTrimmedTitle())
                .data(newAssignment)
                .build();
    }

    /**
     * Check if user has multiple assignments
     */
    public boolean hasMultipleAssignments(String seid) {
        int count = entempRepository.countAssignmentsForUser(seid);
        return count > 1;
    }

    /**
     * Get assignment count for a user
     */
    public int getAssignmentCount(String seid) {
        return entempRepository.countAssignmentsForUser(seid);
    }

    /**
     * Build AssignmentDTO from Entemp
     */
    private AssignmentDTO buildAssignmentDTO(Entemp e, Long currentRoid) {
        return AssignmentDTO.builder()
                .roid(e.getRoid())
                .name(e.getTrimmedName())
                .title(e.getTrimmedTitle())
                .elevel(e.getElevel())
                .elevelName(eLevelService.getELevelName(e.getElevel()))
                .areacd(e.getAreacd())
                .podcd(e.getTrimmedPodcd())
                .org(e.getTrimmedOrg())
                .eactive(e.getEactive())
                .primaryRoid(e.getPrimaryRoid())
                .isCurrent(currentRoid != null && e.getRoid().equals(currentRoid))
                .isStaffAssignment(e.isStaffAssignment())
                .build();
    }
}
