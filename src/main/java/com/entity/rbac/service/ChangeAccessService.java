package com.entity.rbac.service;

import com.entity.rbac.dto.HierarchyDTO.*;
import com.entity.rbac.entity.Entemp;
import com.entity.rbac.repository.EntempRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for Change Access functionality
 * Allows users to change their viewing context to different hierarchy levels
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChangeAccessService {

    private final EntempRepository entempRepository;
    private final ELevelService eLevelService;
    private final MenuPermissionService menuPermissionService;
    private final HierarchyNavigationService hierarchyNavigationService;

    private final Map<String, UserContextDTO> userContexts = new ConcurrentHashMap<>();

    private static final Set<String> VALID_AREA_CODES = Set.of("21", "22", "23", "24", "25", "26", "27", "35");

    private static final List<OrganizationDTO> ORGANIZATIONS = List.of(
            OrganizationDTO.builder().code("CF").name("Field Collection").description("Collection Field Operations").active(true).build(),
            OrganizationDTO.builder().code("AD").name("Advisory").description("Advisory Services").active(true).build(),
            OrganizationDTO.builder().code("CP").name("Compliance").description("Compliance Operations").active(true).build(),
            OrganizationDTO.builder().code("WI").name("Wage & Investment").description("Wage & Investment Division").active(true).build()
    );

    private static final Map<Integer, String> ELEVEL_TO_LEVEL = Map.of(0, "NATIONAL", 2, "AREA", 4, "TERRITORY", 6, "GROUP", 7, "GROUP", 8, "RO");

    public UserContextDTO getUserContext(String seid) {
        if (userContexts.containsKey(seid)) return userContexts.get(seid);
        return initializeUserContext(seid);
    }

    private UserContextDTO initializeUserContext(String seid) {
        Integer elevel = eLevelService.getELevel(seid);
        String elevelName = eLevelService.getELevelName(elevel);
        HierarchyNodeDTO defaultContext = getDefaultContext(seid, elevel);
        List<String> availableLevels = getAvailableLevels(elevel);
        String currentOrg = getCurrentOrg(seid);

        UserContextDTO context = UserContextDTO.builder()
                .seid(seid).userName(getUserName(seid)).actualElevel(elevel).actualElevelName(elevelName)
                .currentContext(defaultContext).canChangeAccess(availableLevels.size() > 1)
                .availableLevels(availableLevels).currentOrg(currentOrg).build();

        userContexts.put(seid, context);
        return context;
    }

    public ChangeAccessResponseDTO changeAccess(String seid, ChangeAccessRequestDTO request) {
        log.info("User {} changing access to level: {}, code: {}", seid, request.getLevel(), request.getCode());

        HierarchyValidationDTO validation = validateChangeAccessRequest(seid, request);
        if (!validation.isValid()) {
            return ChangeAccessResponseDTO.builder().success(false).message(validation.getErrorMessage()).build();
        }

        Integer actualElevel = eLevelService.getELevel(seid);
        if (!isAuthorizedForLevel(actualElevel, request.getLevel(), request.getCode())) {
            return ChangeAccessResponseDTO.builder().success(false).message("You are not authorized to access this level").build();
        }

        HierarchyNodeDTO newContext = buildContextNode(request);
        Integer contextElevel = getElevelForLevel(request.getLevel());
        List<String> availableMenus = getMenusForContextLevel(contextElevel);
        DataScopeDTO dataScope = buildDataScope(request);

        UserContextDTO userContext = getUserContext(seid);
        userContext.setCurrentContext(newContext);
        if (request.getOrg() != null) userContext.setCurrentOrg(request.getOrg());
        userContexts.put(seid, userContext);

        return ChangeAccessResponseDTO.builder().success(true).message("Access changed successfully")
                .newContext(newContext).originalElevel(actualElevel).contextElevel(contextElevel)
                .availableMenus(availableMenus).dataScope(dataScope).build();
    }

    public UserContextDTO resetContext(String seid) {
        userContexts.remove(seid);
        return initializeUserContext(seid);
    }

    public ChangeAccessVisibilityDTO getChangeAccessVisibility(String seid) {
        Integer elevel = eLevelService.getELevel(seid);
        List<String> availableLevels = getAvailableLevels(elevel);
        boolean visible = availableLevels.size() > 1;
        return ChangeAccessVisibilityDTO.builder().visible(visible)
                .reason(visible ? "User has access to multiple hierarchy levels" : "User has access to only one level")
                .accessibleLevels(availableLevels.size()).availableLevels(availableLevels).build();
    }

    public OrganizationListDTO getOrganizations(String seid) {
        return OrganizationListDTO.builder().organizations(ORGANIZATIONS).currentOrg(getCurrentOrg(seid)).build();
    }

    public ChangeAccessResponseDTO changeOrganization(String seid, String orgCode) {
        boolean validOrg = ORGANIZATIONS.stream().anyMatch(o -> o.getCode().equals(orgCode));
        if (!validOrg) return ChangeAccessResponseDTO.builder().success(false).message("Invalid organization code: " + orgCode).build();

        Integer elevel = eLevelService.getELevel(seid);
        if (elevel != 0) return ChangeAccessResponseDTO.builder().success(false).message("Only National level users can change organization").build();

        UserContextDTO context = getUserContext(seid);
        context.setCurrentOrg(orgCode);
        userContexts.put(seid, context);

        return ChangeAccessResponseDTO.builder().success(true).message("Organization changed to " + orgCode)
                .dataScope(DataScopeDTO.builder().level("NATIONAL").org(orgCode).scopeDescription("All data for " + getOrgName(orgCode)).build()).build();
    }

    public HierarchyValidationDTO validateHierarchyCode(String code) {
        if (code == null || code.length() != 8) return HierarchyValidationDTO.builder().valid(false).code(code).errorMessage("Code must be exactly 8 digits").build();
        if (!code.matches("\\d{8}")) return HierarchyValidationDTO.builder().valid(false).code(code).errorMessage("Code must contain only digits").build();

        String level = determineLevelFromCode(code);
        Integer elevel = getElevelForLevel(level);
        String areaCode = code.substring(0, 2);

        if (!areaCode.equals("00") && !VALID_AREA_CODES.contains(areaCode)) {
            return HierarchyValidationDTO.builder().valid(false).code(code).errorMessage("Invalid Area code. Valid: 21-27, 35").build();
        }

        Integer childCount = hierarchyNavigationService.getChildCount(code, level);
        return HierarchyValidationDTO.builder().valid(true).code(code).level(level).levelCode(elevel)
                .displayName(buildDisplayName(code, level)).childCount(childCount).build();
    }

    // Helper methods
    private HierarchyValidationDTO validateChangeAccessRequest(String seid, ChangeAccessRequestDTO request) {
        if ("NATIONAL".equals(request.getLevel())) return HierarchyValidationDTO.builder().valid(true).level(request.getLevel()).build();
        if (request.getCode() == null || request.getCode().isEmpty()) return HierarchyValidationDTO.builder().valid(false).errorMessage("Code is required for " + request.getLevel() + " level").build();
        return validateHierarchyCode(request.getCode());
    }

    private String determineLevelFromCode(String code) {
        if (code.equals("00000000")) return "NATIONAL";
        if (code.endsWith("000000")) return "AREA";
        if (code.endsWith("0000")) return "TERRITORY";
        if (code.endsWith("00")) return "GROUP";
        return "RO";
    }

    private Integer getElevelForLevel(String level) {
        return switch (level) { case "NATIONAL" -> 0; case "AREA" -> 2; case "TERRITORY" -> 4; case "GROUP" -> 6; default -> 8; };
    }

    private List<String> getAvailableLevels(Integer elevel) {
        List<String> levels = new ArrayList<>();
        if (elevel <= 0) levels.add("NATIONAL");
        if (elevel <= 2) levels.add("AREA");
        if (elevel <= 4) levels.add("TERRITORY");
        if (elevel <= 6) levels.add("GROUP");
        levels.add("RO");
        return levels;
    }

    private boolean isAuthorizedForLevel(Integer userElevel, String targetLevel, String targetCode) {
        return userElevel <= getElevelForLevel(targetLevel);
    }

    private List<String> getMenusForContextLevel(Integer contextElevel) {
        List<String> menus = new ArrayList<>(List.of("VIEWS", "REPORTS", "CHANGE_ACCESS", "END_OF_MONTH"));
        if (contextElevel == 6 || contextElevel == 7) { menus.add("CASE_ASSIGNMENT"); menus.add("TIME_VERIFICATION"); }
        if (contextElevel <= 4) menus.add("REALIGNMENT");
        return menus;
    }

    private HierarchyNodeDTO buildContextNode(ChangeAccessRequestDTO request) {
        String code = "NATIONAL".equals(request.getLevel()) ? "00000000" : request.getCode();
        return HierarchyNodeDTO.builder().code(code).level(request.getLevel()).displayName(buildDisplayName(code, request.getLevel())).elevelEquivalent(getElevelForLevel(request.getLevel())).build();
    }

    private String buildDisplayName(String code, String level) {
        if (code == null) return level;
        return switch (level) { case "NATIONAL" -> "National"; case "AREA" -> "Area " + code.substring(0, 2); case "TERRITORY" -> "Territory " + code.substring(0, 4); case "GROUP" -> "Group " + code.substring(0, 6); default -> "RO " + code; };
    }

    private DataScopeDTO buildDataScope(ChangeAccessRequestDTO request) {
        String code = request.getCode();
        DataScopeDTO.DataScopeDTOBuilder builder = DataScopeDTO.builder().level(request.getLevel()).org(request.getOrg());
        if (code != null && code.length() == 8) {
            if (!code.substring(0, 2).equals("00")) builder.areaCode(code.substring(0, 2));
            if (!code.substring(2, 4).equals("00")) builder.territoryCode(code.substring(0, 4));
            if (!code.substring(4, 6).equals("00")) builder.groupCode(code.substring(0, 6));
        }
        builder.scopeDescription(buildScopeDescription(request.getLevel(), code));
        return builder.build();
    }

    private String buildScopeDescription(String level, String code) {
        return switch (level) { case "NATIONAL" -> "All data nationwide"; case "AREA" -> "All data in Area " + (code != null ? code.substring(0, 2) : ""); case "TERRITORY" -> "All data in Territory " + (code != null ? code.substring(0, 4) : ""); case "GROUP" -> "All data in Group " + (code != null ? code.substring(0, 6) : ""); default -> "Data for RO " + code; };
    }

    private HierarchyNodeDTO getDefaultContext(String seid, Integer elevel) {
        Optional<Entemp> assignment = entempRepository.findCurrentActiveAssignment(seid);
        if (assignment.isPresent()) {
            Entemp e = assignment.get();
            String level = ELEVEL_TO_LEVEL.getOrDefault(elevel, "RO");
            String code = buildCodeFromAssignment(e, level);
            return HierarchyNodeDTO.builder().code(code).level(level).displayName(buildDisplayName(code, level)).elevelEquivalent(elevel).build();
        }
        return HierarchyNodeDTO.builder().code("00000000").level(elevel == 0 ? "NATIONAL" : "RO").displayName(elevel == 0 ? "National" : "Unknown").elevelEquivalent(elevel).build();
    }

    private String buildCodeFromAssignment(Entemp e, String level) {
        String areaStr = e.getAreacd() != null ? String.format("%02d", e.getAreacd()) : "00";
        String podStr = e.getPodcd() != null ? String.format("%-6s", e.getPodcd().trim()).replace(' ', '0') : "000000";
        if (podStr.length() > 6) podStr = podStr.substring(0, 6);
        return switch (level) { case "NATIONAL" -> "00000000"; case "AREA" -> areaStr + "000000"; case "TERRITORY" -> areaStr + podStr.substring(0, 2) + "0000"; case "GROUP" -> areaStr + podStr.substring(0, 4) + "00"; default -> areaStr + podStr; };
    }

    private String getUserName(String seid) { return entempRepository.findCurrentActiveAssignment(seid).map(Entemp::getName).orElse("Unknown"); }
    private String getCurrentOrg(String seid) { return entempRepository.findCurrentActiveAssignment(seid).map(Entemp::getOrg).orElse("CF"); }
    private String getOrgName(String code) { return ORGANIZATIONS.stream().filter(o -> o.getCode().equals(code)).map(OrganizationDTO::getName).findFirst().orElse(code); }
}
