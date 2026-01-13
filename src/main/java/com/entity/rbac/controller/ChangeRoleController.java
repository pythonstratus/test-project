package com.entity.rbac.controller;

import com.entity.rbac.dto.ChangeRoleDTO.*;
import com.entity.rbac.service.ChangeRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rbac/role")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Change Role", description = "APIs for changing user roles (General and Staff modes)")
public class ChangeRoleController {
    private final ChangeRoleService changeRoleService;

    // Configuration
    @GetMapping("/config/{seid}")
    @Operation(summary = "Get change role configuration")
    public ResponseEntity<ChangeRoleConfigDTO> getChangeRoleConfig(@PathVariable String seid) {
        return ResponseEntity.ok(changeRoleService.getChangeRoleConfig(seid));
    }

    @GetMapping("/current/{seid}")
    @Operation(summary = "Get current role")
    public ResponseEntity<CurrentRoleDTO> getCurrentRole(@PathVariable String seid) {
        return ResponseEntity.ok(changeRoleService.getCurrentRole(seid));
    }

    // General User
    @GetMapping("/general/{seid}/options")
    @Operation(summary = "Get general user role options")
    public ResponseEntity<GeneralUserRolesDTO> getGeneralUserRoles(@PathVariable String seid) {
        CurrentRoleDTO currentRole = changeRoleService.getCurrentRole(seid);
        return ResponseEntity.ok(changeRoleService.getGeneralUserRoles(seid, currentRole));
    }

    @PostMapping("/general/{seid}/change")
    @Operation(summary = "Change role (General user)")
    public ResponseEntity<ChangeRoleResponseDTO> changeRoleGeneral(@PathVariable String seid, @RequestBody GeneralChangeRoleRequestDTO request) {
        return ResponseEntity.ok(changeRoleService.changeRoleGeneral(seid, request));
    }

    // Staff User
    @GetMapping("/staff/{seid}/options")
    @Operation(summary = "Get staff user role options")
    public ResponseEntity<StaffUserOptionsDTO> getStaffUserOptions(@PathVariable String seid) {
        CurrentRoleDTO currentRole = changeRoleService.getCurrentRole(seid);
        return ResponseEntity.ok(changeRoleService.getStaffUserOptions(seid, currentRole));
    }

    @PostMapping("/staff/{seid}/change")
    @Operation(summary = "Change role (Staff user)")
    public ResponseEntity<ChangeRoleResponseDTO> changeRoleStaff(@PathVariable String seid, @RequestBody StaffChangeRoleRequestDTO request) {
        return ResponseEntity.ok(changeRoleService.changeRoleStaff(seid, request));
    }

    // Validation
    @GetMapping("/validate/level-value")
    @Operation(summary = "Validate level value")
    public ResponseEntity<LevelValueValidationDTO> validateLevelValue(@RequestParam String level, @RequestParam(required = false) String value) {
        return ResponseEntity.ok(changeRoleService.validateLevelValue(level, value));
    }

    // Defaults
    @DeleteMapping("/staff/{seid}/default")
    @Operation(summary = "Clear saved default")
    public ResponseEntity<Void> clearDefault(@PathVariable String seid) {
        changeRoleService.clearUserDefault(seid);
        return ResponseEntity.ok().build();
    }

    // Dropdowns
    @GetMapping("/levels")
    @Operation(summary = "Get available levels")
    public ResponseEntity<List<LevelOptionDTO>> getLevels() {
        return ResponseEntity.ok(List.of(
            LevelOptionDTO.builder().code("NATIONAL").displayName("National").elevel(0).valueHint("0 - national").requiredDigits(0).build(),
            LevelOptionDTO.builder().code("AREA").displayName("Area").elevel(2).valueHint("2-digit area").requiredDigits(2).build(),
            LevelOptionDTO.builder().code("TERRITORY").displayName("Territory").elevel(4).valueHint("4-digit territory").requiredDigits(4).build(),
            LevelOptionDTO.builder().code("GROUP").displayName("Group").elevel(6).valueHint("6-digit Group").requiredDigits(6).build(),
            LevelOptionDTO.builder().code("EMPLOYEE").displayName("Employee").elevel(8).valueHint("8-Digits RO").requiredDigits(8).build()
        ));
    }

    @GetMapping("/org-functions")
    @Operation(summary = "Get available org/functions")
    public ResponseEntity<List<OrgFunctionOptionDTO>> getOrgFunctions() {
        return ResponseEntity.ok(List.of(
            OrgFunctionOptionDTO.builder().code("FC").displayName("FC - Field Collection").description("Field Collection").build(),
            OrgFunctionOptionDTO.builder().code("CCP").displayName("CCP - Collection Processing").description("Collection Processing").build(),
            OrgFunctionOptionDTO.builder().code("WI").displayName("W&I - Taxpayer Services").description("Taxpayer Services").build()
        ));
    }
}
