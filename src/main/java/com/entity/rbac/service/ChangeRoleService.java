package com.entity.rbac.service;

import com.entity.rbac.dto.ChangeRoleDTO.*;
import com.entity.rbac.entity.Entemp;
import com.entity.rbac.repository.EntempRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChangeRoleService {
    private final EntempRepository entempRepository;
    private final ELevelService eLevelService;
    private final StaffService staffService;

    private final Map<String, StaffChangeRoleRequestDTO> userDefaults = new ConcurrentHashMap<>();
    private static final Set<String> VALID_AREA_CODES = Set.of("21", "22", "23", "24", "25", "26", "27", "35");

    private static final List<LevelOptionDTO> LEVEL_OPTIONS = List.of(
        LevelOptionDTO.builder().code("NATIONAL").displayName("National").elevel(0).valueHint("0 - national").requiredDigits(0).build(),
        LevelOptionDTO.builder().code("AREA").displayName("Area").elevel(2).valueHint("2-digit area").requiredDigits(2).build(),
        LevelOptionDTO.builder().code("TERRITORY").displayName("Territory").elevel(4).valueHint("4-digit territory").requiredDigits(4).build(),
        LevelOptionDTO.builder().code("GROUP").displayName("Group").elevel(6).valueHint("6-digit Group").requiredDigits(6).build(),
        LevelOptionDTO.builder().code("EMPLOYEE").displayName("Employee").elevel(8).valueHint("8-Digits RO").requiredDigits(8).build()
    );

    private static final List<OrgFunctionOptionDTO> ORG_FUNCTION_OPTIONS = List.of(
        OrgFunctionOptionDTO.builder().code("FC").displayName("FC - Field Collection").description("Field Collection").build(),
        OrgFunctionOptionDTO.builder().code("CCP").displayName("CCP - Collection Processing").description("Collection Processing").build(),
        OrgFunctionOptionDTO.builder().code("WI").displayName("W&I - Taxpayer Services").description("Taxpayer Services").build()
    );

    public ChangeRoleConfigDTO getChangeRoleConfig(String seid) {
        boolean isStaff = staffService.isStaff(seid);
        CurrentRoleDTO currentRole = getCurrentRole(seid);

        ChangeRoleConfigDTO.ChangeRoleConfigDTOBuilder builder = ChangeRoleConfigDTO.builder()
            .seid(seid).userName(currentRole.getName()).isStaff(isStaff).mode(isStaff ? "STAFF" : "GENERAL").currentRole(currentRole);

        if (isStaff) {
            builder.staffOptions(getStaffUserOptions(seid, currentRole)).canChangeRole(true);
        } else {
            GeneralUserRolesDTO generalOptions = getGeneralUserRoles(seid, currentRole);
            builder.generalOptions(generalOptions).canChangeRole(generalOptions.getTotalRoles() > 1);
            if (generalOptions.getTotalRoles() <= 1) builder.disabledReason("You have only one role assigned");
        }
        return builder.build();
    }

    public GeneralUserRolesDTO getGeneralUserRoles(String seid, CurrentRoleDTO currentRole) {
        List<Entemp> assignments = entempRepository.findAllValidAssignments(seid);
        List<RoleOptionDTO> roleOptions = assignments.stream()
            .map(e -> RoleOptionDTO.builder().roid(String.valueOf(e.getRoid())).title(e.getTitle() != null ? e.getTitle().trim() : "Unknown")
                .grade(extractGrade(e)).displayText(buildRoleDisplayText(e)).elevel(e.getElevel())
                .isCurrent(String.valueOf(e.getRoid()).equals(currentRole.getRoid())).build())
            .sorted(Comparator.comparing(RoleOptionDTO::getDisplayText)).collect(Collectors.toList());
        return GeneralUserRolesDTO.builder().currentRole(currentRole).availableRoles(roleOptions).totalRoles(roleOptions.size()).build();
    }

    @Transactional
    public ChangeRoleResponseDTO changeRoleGeneral(String seid, GeneralChangeRoleRequestDTO request) {
        Long roid;
        try { roid = Long.parseLong(request.getRoid()); }
        catch (NumberFormatException e) { return ChangeRoleResponseDTO.builder().success(false).message("Invalid ROID format").build(); }

        Optional<Entemp> targetAssignment = entempRepository.findByRoidAndSeid(roid, seid);
        if (targetAssignment.isEmpty()) return ChangeRoleResponseDTO.builder().success(false).message("Assignment not found").build();

        entempRepository.resetAllAssignmentsForUser(seid);
        entempRepository.activateAssignment(roid, seid);

        Entemp newAssignment = targetAssignment.get();
        CurrentRoleDTO newRole = buildCurrentRoleDTO(newAssignment);
        return ChangeRoleResponseDTO.builder().success(true).message("Role changed successfully").newRole(newRole)
            .newElevel(newAssignment.getElevel()).newOrg(newAssignment.getOrg()).availableMenus(getMenusForElevel(newAssignment.getElevel(), false)).build();
    }

    public StaffUserOptionsDTO getStaffUserOptions(String seid, CurrentRoleDTO currentRole) {
        List<Entemp> assignments = entempRepository.findAllValidAssignments(seid);
        List<AssignmentOptionDTO> assignmentOptions = assignments.stream()
            .map(e -> AssignmentOptionDTO.builder().roid(String.valueOf(e.getRoid())).displayText(String.valueOf(e.getRoid()))
                .title(e.getTitle() != null ? e.getTitle().trim() : "").elevel(e.getElevel())
                .isCurrent(String.valueOf(e.getRoid()).equals(currentRole.getRoid())).build())
            .sorted(Comparator.comparing(AssignmentOptionDTO::getRoid)).collect(Collectors.toList());

        String currentOrg = currentRole.getOrg();
        List<OrgFunctionOptionDTO> orgOptions = ORG_FUNCTION_OPTIONS.stream()
            .map(o -> OrgFunctionOptionDTO.builder().code(o.getCode()).displayName(o.getDisplayName())
                .description(o.getDescription()).isCurrent(o.getCode().equals(currentOrg)).build()).collect(Collectors.toList());

        return StaffUserOptionsDTO.builder().currentRole(currentRole).levels(LEVEL_OPTIONS).assignments(assignmentOptions).orgFunctions(orgOptions)
            .levelValueHint("8-Digits RO, 6-digit Group, 4-digit territory, 2-digit area, 0-national").build();
    }

    @Transactional
    public ChangeRoleResponseDTO changeRoleStaff(String seid, StaffChangeRoleRequestDTO request) {
        ChangeRoleValidationDTO validation = validateStaffRequest(seid, request);
        if (!validation.isValid()) return ChangeRoleResponseDTO.builder().success(false).message(String.join("; ", validation.getErrors())).build();

        if (request.getAssignmentRoid() != null && !request.getAssignmentRoid().isEmpty()) {
            Long roid = Long.parseLong(request.getAssignmentRoid());
            entempRepository.resetAllAssignmentsForUser(seid);
            entempRepository.activateAssignment(roid, seid);
        }

        if (request.getOrgFunction() != null && !request.getOrgFunction().isEmpty()) {
            entempRepository.findCurrentActiveAssignment(seid).ifPresent(e -> entempRepository.updateOrg(e.getRoid(), request.getOrgFunction()));
        }

        if (request.isKeepAsDefault()) userDefaults.put(seid, request);

        CurrentRoleDTO newRole = getCurrentRole(seid);
        Integer effectiveElevel = getElevelForLevel(request.getLevel());
        return ChangeRoleResponseDTO.builder().success(true).message("Role changed successfully").newRole(newRole)
            .newLevel(request.getLevel()).newElevel(effectiveElevel).newOrg(request.getOrgFunction()).availableMenus(getMenusForElevel(effectiveElevel, true)).build();
    }

    public LevelValueValidationDTO validateLevelValue(String level, String value) {
        LevelOptionDTO levelOption = LEVEL_OPTIONS.stream().filter(l -> l.getCode().equals(level)).findFirst().orElse(null);
        if (levelOption == null) return LevelValueValidationDTO.builder().valid(false).errorMessage("Invalid level: " + level).build();

        int requiredDigits = levelOption.getRequiredDigits();
        if (requiredDigits == 0) return LevelValueValidationDTO.builder().valid(true).level(level).normalizedValue("00000000").displayName("National").build();

        if (value == null || value.trim().isEmpty()) return LevelValueValidationDTO.builder().valid(false).errorMessage("Level value required for " + level).build();

        String cleanValue = value.trim().replaceAll("[^0-9]", "");
        if (cleanValue.length() < requiredDigits) return LevelValueValidationDTO.builder().valid(false).errorMessage(level + " requires " + requiredDigits + " digits").build();

        String significantDigits = cleanValue.substring(0, Math.min(cleanValue.length(), requiredDigits));
        if (requiredDigits >= 2 && !VALID_AREA_CODES.contains(significantDigits.substring(0, 2)))
            return LevelValueValidationDTO.builder().valid(false).errorMessage("Invalid Area code. Valid: 21-27, 35").build();

        return LevelValueValidationDTO.builder().valid(true).level(level).value(value)
            .normalizedValue(String.format("%-8s", significantDigits).replace(' ', '0')).displayName(buildLevelDisplayName(level, significantDigits)).build();
    }

    public CurrentRoleDTO getCurrentRole(String seid) {
        Optional<Entemp> current = entempRepository.findCurrentActiveAssignment(seid);
        if (current.isPresent()) return buildCurrentRoleDTO(current.get());
        List<Entemp> assignments = entempRepository.findAllValidAssignments(seid);
        if (!assignments.isEmpty()) return buildCurrentRoleDTO(assignments.get(0));
        return CurrentRoleDTO.builder().name("Unknown").displayText("No role assigned").build();
    }

    public void clearUserDefault(String seid) { userDefaults.remove(seid); }

    // Helper methods
    private CurrentRoleDTO buildCurrentRoleDTO(Entemp e) {
        return CurrentRoleDTO.builder().name(e.getName() != null ? e.getName().trim() : "Unknown")
            .title(e.getTitle() != null ? e.getTitle().trim() : "Unknown").grade(extractGrade(e)).roid(String.valueOf(e.getRoid()))
            .displayText(buildRoleDisplayText(e)).elevel(e.getElevel())
            .areaCode(e.getAreacd() != null ? String.format("%02d", e.getAreacd()) : null)
            .podCode(e.getPodcd() != null ? e.getPodcd().trim() : null).org(e.getOrg() != null ? e.getOrg().trim() : "CF").build();
    }

    private String buildRoleDisplayText(Entemp e) {
        return (e.getTitle() != null ? e.getTitle().trim() : "Unknown") + " - " + extractGrade(e) + " - " + e.getRoid();
    }

    private String extractGrade(Entemp e) { return e.getElevel() != null ? "Grade " + (10 + Math.abs(e.getElevel())) : "Grade 11"; }
    private Integer getElevelForLevel(String level) {
        return LEVEL_OPTIONS.stream().filter(l -> l.getCode().equals(level)).map(LevelOptionDTO::getElevel).findFirst().orElse(8);
    }

    private String buildLevelDisplayName(String level, String value) {
        return switch (level) { case "NATIONAL" -> "National"; case "AREA" -> "Area " + value; case "TERRITORY" -> "Territory " + value; case "GROUP" -> "Group " + value; default -> "Employee " + value; };
    }

    private List<String> getMenusForElevel(Integer elevel, boolean isStaff) {
        List<String> menus = new ArrayList<>(List.of("VIEWS", "REPORTS", "CHANGE_ACCESS", "END_OF_MONTH"));
        if (elevel != null && (elevel == 6 || elevel == 7)) { menus.add("CASE_ASSIGNMENT"); menus.add("TIME_VERIFICATION"); }
        if ((elevel != null && elevel <= 4) || isStaff) menus.add("REALIGNMENT");
        if (isStaff) menus.add("UTILITIES");
        return menus;
    }

    private ChangeRoleValidationDTO validateStaffRequest(String seid, StaffChangeRoleRequestDTO request) {
        List<String> errors = new ArrayList<>();
        if (request.getLevel() == null || request.getLevel().isEmpty()) errors.add("Level is required");
        else {
            LevelValueValidationDTO levelValidation = validateLevelValue(request.getLevel(), request.getLevelValue());
            if (!levelValidation.isValid()) errors.add(levelValidation.getErrorMessage());
        }
        if (request.getAssignmentRoid() != null && !request.getAssignmentRoid().isEmpty()) {
            try {
                Long roid = Long.parseLong(request.getAssignmentRoid());
                if (entempRepository.findByRoidAndSeid(roid, seid).isEmpty()) errors.add("Invalid Assignment Number");
            } catch (NumberFormatException e) { errors.add("Assignment Number must be numeric"); }
        }
        if (request.getOrgFunction() != null && !request.getOrgFunction().isEmpty()) {
            if (ORG_FUNCTION_OPTIONS.stream().noneMatch(o -> o.getCode().equals(request.getOrgFunction()))) errors.add("Invalid Org/Function");
        }
        return ChangeRoleValidationDTO.builder().valid(errors.isEmpty()).errors(errors).warnings(new ArrayList<>()).build();
    }
}
