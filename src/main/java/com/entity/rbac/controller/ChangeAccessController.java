package com.entity.rbac.controller;

import com.entity.rbac.dto.HierarchyDTO.*;
import com.entity.rbac.service.ChangeAccessService;
import com.entity.rbac.service.HierarchyNavigationService;
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
@Tag(name = "Change Access", description = "APIs for hierarchy navigation and changing access context")
public class ChangeAccessController {
    private final ChangeAccessService changeAccessService;
    private final HierarchyNavigationService hierarchyNavigationService;

    // Context Management
    @GetMapping("/context/{seid}")
    @Operation(summary = "Get current user context")
    public ResponseEntity<UserContextDTO> getUserContext(@PathVariable String seid) {
        return ResponseEntity.ok(changeAccessService.getUserContext(seid));
    }

    @PostMapping("/context/{seid}/change")
    @Operation(summary = "Change access context")
    public ResponseEntity<ChangeAccessResponseDTO> changeAccess(@PathVariable String seid, @RequestBody ChangeAccessRequestDTO request) {
        return ResponseEntity.ok(changeAccessService.changeAccess(seid, request));
    }

    @PostMapping("/context/{seid}/reset")
    @Operation(summary = "Reset user context")
    public ResponseEntity<UserContextDTO> resetContext(@PathVariable String seid) {
        return ResponseEntity.ok(changeAccessService.resetContext(seid));
    }

    // Visibility Check
    @GetMapping("/menu/{seid}/change-access-visible")
    @Operation(summary = "Check Change Access visibility")
    public ResponseEntity<ChangeAccessVisibilityDTO> getChangeAccessVisibility(@PathVariable String seid) {
        return ResponseEntity.ok(changeAccessService.getChangeAccessVisibility(seid));
    }

    // Hierarchy Navigation
    @GetMapping("/hierarchy/{seid}/nav/areas")
    @Operation(summary = "Get all Areas for navigation")
    public ResponseEntity<HierarchyListDTO> getAreas(@PathVariable String seid) {
        return ResponseEntity.ok(hierarchyNavigationService.getAreas(seid));
    }

    @GetMapping("/hierarchy/{seid}/area/{areaCode}/territories")
    @Operation(summary = "Get Territories in Area")
    public ResponseEntity<HierarchyListDTO> getTerritories(@PathVariable String seid, @PathVariable String areaCode) {
        String normalizedCode = normalizeCode(areaCode, 2) + "000000";
        return ResponseEntity.ok(hierarchyNavigationService.getTerritories(seid, normalizedCode));
    }

    @GetMapping("/hierarchy/{seid}/territory/{territoryCode}/groups")
    @Operation(summary = "Get Groups in Territory")
    public ResponseEntity<HierarchyListDTO> getGroups(@PathVariable String seid, @PathVariable String territoryCode) {
        String normalizedCode = normalizeCode(territoryCode, 4) + "0000";
        return ResponseEntity.ok(hierarchyNavigationService.getGroups(seid, normalizedCode));
    }

    @GetMapping("/hierarchy/{seid}/group/{groupCode}/officers")
    @Operation(summary = "Get Revenue Officers in Group")
    public ResponseEntity<HierarchyListDTO> getRevenueOfficers(@PathVariable String seid, @PathVariable String groupCode) {
        String normalizedCode = normalizeCode(groupCode, 6) + "00";
        return ResponseEntity.ok(hierarchyNavigationService.getRevenueOfficers(seid, normalizedCode));
    }

    // Validation
    @GetMapping("/hierarchy/validate/{code}")
    @Operation(summary = "Validate hierarchy code")
    public ResponseEntity<HierarchyValidationDTO> validateCode(@PathVariable String code) {
        String paddedCode = padCode(code);
        return ResponseEntity.ok(changeAccessService.validateHierarchyCode(paddedCode));
    }

    // Search
    @GetMapping("/hierarchy/{seid}/search")
    @Operation(summary = "Search hierarchy")
    public ResponseEntity<List<HierarchyNodeDTO>> searchHierarchy(@PathVariable String seid, @RequestParam String q) {
        return ResponseEntity.ok(hierarchyNavigationService.searchHierarchy(seid, q));
    }

    // Organizations
    @GetMapping("/organizations/{seid}")
    @Operation(summary = "Get available organizations")
    public ResponseEntity<OrganizationListDTO> getOrganizations(@PathVariable String seid) {
        return ResponseEntity.ok(changeAccessService.getOrganizations(seid));
    }

    @PostMapping("/organizations/{seid}/change/{orgCode}")
    @Operation(summary = "Change organization")
    public ResponseEntity<ChangeAccessResponseDTO> changeOrganization(@PathVariable String seid, @PathVariable String orgCode) {
        return ResponseEntity.ok(changeAccessService.changeOrganization(seid, orgCode));
    }

    // Helpers
    private String normalizeCode(String code, int significantDigits) {
        if (code == null) return "00";
        String cleaned = code.replaceAll("0+$", "");
        if (cleaned.isEmpty()) cleaned = "0";
        if (cleaned.length() > significantDigits) cleaned = cleaned.substring(0, significantDigits);
        return String.format("%" + significantDigits + "s", cleaned).replace(' ', '0');
    }

    private String padCode(String code) {
        if (code == null) return "00000000";
        String cleaned = code.trim();
        if (cleaned.length() >= 8) return cleaned.substring(0, 8);
        return String.format("%-8s", cleaned).replace(' ', '0');
    }
}
