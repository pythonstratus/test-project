package com.entity.rbac.service;

import com.entity.rbac.dto.RbacDTO.*;
import com.entity.rbac.entity.EntityUser;
import com.entity.rbac.entity.Entemp;
import com.entity.rbac.exception.UserNotFoundException;
import com.entity.rbac.repository.EntityUserRepository;
import com.entity.rbac.repository.EntempRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for User operations
 * Aggregates user profile information from multiple sources
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final EntempRepository entempRepository;
    private final EntityUserRepository entityUserRepository;
    private final ELevelService eLevelService;
    private final StaffService staffService;

    /**
     * Get full user profile
     */
    public UserProfileDTO getUserProfile(String seid) {
        log.debug("Getting user profile for SEID: {}", seid);

        // Get all assignments for the user
        List<Entemp> assignments = entempRepository.findAllValidAssignments(seid);

        if (assignments.isEmpty()) {
            log.warn("No assignments found for SEID: {}", seid);
            throw new UserNotFoundException("User not found: " + seid);
        }

        // Find current active assignment
        Optional<Entemp> currentAssignment = entempRepository.findCurrentActiveAssignment(seid);
        Entemp activeAssignment = currentAssignment.orElse(assignments.get(0));

        // Get ELEVEL
        Integer elevel = activeAssignment.getElevel();
        String elevelName = eLevelService.getELevelName(elevel);

        // Check staff status
        boolean isStaff = staffService.isStaff(seid);

        // Check locked status
        boolean isLocked = false;
        Optional<EntityUser> entityUser = entityUserRepository.findByUserSeid(seid);
        if (entityUser.isPresent()) {
            isLocked = entityUser.get().isLocked();
        }

        // Build assignment DTOs
        List<AssignmentDTO> assignmentDTOs = assignments.stream()
                .map(e -> buildAssignmentDTO(e, activeAssignment.getRoid()))
                .collect(Collectors.toList());

        return UserProfileDTO.builder()
                .seid(seid)
                .name(activeAssignment.getTrimmedName())
                .title(activeAssignment.getTrimmedTitle())
                .elevel(elevel)
                .elevelName(elevelName)
                .areaCode(activeAssignment.getAreacd() != null ?
                        String.format("%02d", activeAssignment.getAreacd()) : null)
                .podCode(activeAssignment.getTrimmedPodcd())
                .org(activeAssignment.getTrimmedOrg())
                .isStaff(isStaff)
                .isLocked(isLocked)
                .hasMultipleAssignments(assignments.size() > 1)
                .assignmentCount(assignments.size())
                .currentAssignment(buildAssignmentDTO(activeAssignment, activeAssignment.getRoid()))
                .allAssignments(assignmentDTOs)
                .build();
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
                .isCurrent(e.getRoid().equals(currentRoid))
                .isStaffAssignment(e.isStaffAssignment())
                .build();
    }

    /**
     * Check if user exists
     */
    public boolean userExists(String seid) {
        return !entempRepository.findAllValidAssignments(seid).isEmpty();
    }

    /**
     * Check if user is locked
     */
    public boolean isUserLocked(String seid) {
        return entityUserRepository.findByUserSeid(seid)
                .map(EntityUser::isLocked)
                .orElse(false);
    }

    /**
     * Get user's current assignment
     */
    public Optional<AssignmentDTO> getCurrentAssignment(String seid) {
        return entempRepository.findCurrentActiveAssignment(seid)
                .map(e -> buildAssignmentDTO(e, e.getRoid()));
    }

    /**
     * Get all assignments for a user
     */
    public List<AssignmentDTO> getAllAssignments(String seid) {
        List<Entemp> assignments = entempRepository.findAllValidAssignments(seid);
        Optional<Entemp> current = entempRepository.findCurrentActiveAssignment(seid);
        Long currentRoid = current.map(Entemp::getRoid).orElse(null);

        return assignments.stream()
                .map(e -> buildAssignmentDTO(e, currentRoid))
                .collect(Collectors.toList());
    }
}
