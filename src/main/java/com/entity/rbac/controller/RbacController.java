package com.entity.rbac.controller;

import com.entity.rbac.dto.RbacDTO.*;
import com.entity.rbac.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rbac")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "RBAC Core", description = "Core RBAC APIs for user profiles, ELEVEL, and menu permissions")
public class RbacController {
    private final UserService userService;
    private final ELevelService eLevelService;
    private final MenuPermissionService menuPermissionService;
    private final StaffService staffService;
    private final HierarchyService hierarchyService;
    private final AssignmentService assignmentService;

    @GetMapping("/users/{seid}/profile")
    @Operation(summary = "Get user profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable String seid) {
        return ResponseEntity.ok(userService.getUserProfile(seid));
    }

    @GetMapping("/users/{seid}/elevel")
    @Operation(summary = "Get user ELEVEL")
    public ResponseEntity<ELevelDTO> getELevel(@PathVariable String seid) {
        return ResponseEntity.ok(eLevelService.getELevelDTO(seid));
    }

    @GetMapping("/elevel/definitions")
    @Operation(summary = "Get all ELEVEL definitions")
    public ResponseEntity<List<ELevelDefinitionDTO>> getELevelDefinitions() {
        return ResponseEntity.ok(eLevelService.getAllELevelDefinitions());
    }

    @GetMapping("/menu/{seid}/permissions")
    @Operation(summary = "Get menu permissions")
    public ResponseEntity<MenuPermissionsDTO> getMenuPermissions(@PathVariable String seid) {
        return ResponseEntity.ok(menuPermissionService.getMenuPermissions(seid));
    }

    @GetMapping("/menu/{seid}/accessible")
    @Operation(summary = "Get accessible menu IDs")
    public ResponseEntity<List<String>> getAccessibleMenus(@PathVariable String seid) {
        return ResponseEntity.ok(menuPermissionService.getAccessibleMenuIds(seid));
    }

    @GetMapping("/staff/{seid}")
    @Operation(summary = "Get staff info")
    public ResponseEntity<StaffInfoDTO> getStaffInfo(@PathVariable String seid) {
        return ResponseEntity.ok(staffService.getStaffInfo(seid));
    }

    @GetMapping("/staff/{seid}/is-staff")
    @Operation(summary = "Check if user is staff")
    public ResponseEntity<Boolean> isStaff(@PathVariable String seid) {
        return ResponseEntity.ok(staffService.isStaff(seid));
    }

    @PostMapping("/staff/{seid}/org/{orgCode}")
    @Operation(summary = "Update staff ORG")
    public ResponseEntity<Boolean> updateStaffOrg(@PathVariable String seid, @PathVariable String orgCode) {
        return ResponseEntity.ok(staffService.updateStaffOrg(seid, orgCode));
    }

    @GetMapping("/staff/orgs")
    @Operation(summary = "Get available ORG codes")
    public ResponseEntity<List<StaffOrgDTO>> getAvailableOrgs() {
        return ResponseEntity.ok(staffService.getAvailableOrgs());
    }

    @GetMapping("/hierarchy/{seid}")
    @Operation(summary = "Get hierarchy access info")
    public ResponseEntity<HierarchyAccessDTO> getHierarchyAccess(@PathVariable String seid) {
        return ResponseEntity.ok(hierarchyService.getHierarchyAccess(seid));
    }

    @GetMapping("/hierarchy/{seid}/areas")
    @Operation(summary = "Get accessible areas")
    public ResponseEntity<List<AreaDTO>> getAccessibleAreas(@PathVariable String seid) {
        return ResponseEntity.ok(hierarchyService.getAccessibleAreas(seid));
    }

    @GetMapping("/assignments/{seid}")
    @Operation(summary = "Get all assignments")
    public ResponseEntity<List<AssignmentDTO>> getAssignments(@PathVariable String seid) {
        return ResponseEntity.ok(assignmentService.getAssignments(seid));
    }

    @GetMapping("/assignments/{seid}/current")
    @Operation(summary = "Get current assignment")
    public ResponseEntity<AssignmentDTO> getCurrentAssignment(@PathVariable String seid) {
        return ResponseEntity.ok(assignmentService.getCurrentAssignment(seid).orElse(null));
    }

    @PostMapping("/assignments/{seid}/switch/{roid}")
    @Operation(summary = "Switch to different assignment")
    public ResponseEntity<ApiResponse<AssignmentDTO>> switchAssignment(@PathVariable String seid, @PathVariable Long roid) {
        return ResponseEntity.ok(assignmentService.switchAssignment(seid, roid));
    }

    @GetMapping("/assignments/{seid}/has-multiple")
    @Operation(summary = "Check if user has multiple assignments")
    public ResponseEntity<Boolean> hasMultipleAssignments(@PathVariable String seid) {
        return ResponseEntity.ok(assignmentService.hasMultipleAssignments(seid));
    }
}
