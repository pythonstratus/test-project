package com.entity.rbac.service;

import com.entity.rbac.dto.RbacDTO.MenuItemDTO;
import com.entity.rbac.dto.RbacDTO.MenuPermissionsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for Menu Permission operations
 * Determines which menu items are accessible based on ELEVEL and Staff status
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MenuPermissionService {

    private final ELevelService eLevelService;
    private final StaffService staffService;

    // Menu definitions
    private static final Map<String, MenuItemDTO> MENU_DEFINITIONS = new LinkedHashMap<>();

    static {
        MENU_DEFINITIONS.put("VIEWS", MenuItemDTO.builder()
                .id("VIEWS").name("Views").description("Data views and queries").build());
        MENU_DEFINITIONS.put("REPORTS", MenuItemDTO.builder()
                .id("REPORTS").name("Reports & Queries").description("Generate reports").build());
        MENU_DEFINITIONS.put("CHANGE_ACCESS", MenuItemDTO.builder()
                .id("CHANGE_ACCESS").name("Change Access").description("Change viewing context").build());
        MENU_DEFINITIONS.put("END_OF_MONTH", MenuItemDTO.builder()
                .id("END_OF_MONTH").name("End of Month").description("Month-end processing").build());
        MENU_DEFINITIONS.put("CASE_ASSIGNMENT", MenuItemDTO.builder()
                .id("CASE_ASSIGNMENT").name("Case Assignment").description("Assign cases to employees").build());
        MENU_DEFINITIONS.put("TIME_VERIFICATION", MenuItemDTO.builder()
                .id("TIME_VERIFICATION").name("Weekly Time Verification").description("Verify time entries").build());
        MENU_DEFINITIONS.put("REALIGNMENT", MenuItemDTO.builder()
                .id("REALIGNMENT").name("Realignment").description("Organizational realignment").build());
        MENU_DEFINITIONS.put("UTILITIES", MenuItemDTO.builder()
                .id("UTILITIES").name("Utilities").description("System utilities").build());
    }

    /**
     * Get menu permissions for a user
     */
    public MenuPermissionsDTO getMenuPermissions(String seid) {
        log.debug("Getting menu permissions for SEID: {}", seid);

        Integer elevel = eLevelService.getELevel(seid);
        boolean isStaff = staffService.isStaff(seid);

        List<MenuItemDTO> menuItems = new ArrayList<>();
        int accessibleCount = 0;

        for (Map.Entry<String, MenuItemDTO> entry : MENU_DEFINITIONS.entrySet()) {
            String menuId = entry.getKey();
            MenuItemDTO template = entry.getValue();

            boolean accessible = isMenuAccessible(menuId, elevel, isStaff);
            String accessReason = getAccessReason(menuId, elevel, isStaff, accessible);

            menuItems.add(MenuItemDTO.builder()
                    .id(template.getId())
                    .name(template.getName())
                    .description(template.getDescription())
                    .accessible(accessible)
                    .accessReason(accessReason)
                    .build());

            if (accessible) accessibleCount++;
        }

        return MenuPermissionsDTO.builder()
                .seid(seid)
                .elevel(elevel)
                .isStaff(isStaff)
                .menuItems(menuItems)
                .accessibleCount(accessibleCount)
                .totalCount(menuItems.size())
                .build();
    }

    /**
     * Check if a specific menu is accessible
     */
    public boolean isMenuAccessible(String menuId, Integer elevel, boolean isStaff) {
        if (elevel == null || elevel < 0) {
            return false; // Blocked or unsupported
        }

        return switch (menuId) {
            // Always accessible for valid ELEVEL
            case "VIEWS", "REPORTS", "CHANGE_ACCESS", "END_OF_MONTH" -> true;

            // Case Assignment: ELEVEL 6 or 7 only
            case "CASE_ASSIGNMENT" -> elevel == 6 || elevel == 7;

            // Time Verification: ELEVEL 6 or 7 only
            case "TIME_VERIFICATION" -> elevel == 6 || elevel == 7;

            // Realignment: ELEVEL 0, 2, 4 OR Staff (any ELEVEL)
            case "REALIGNMENT" -> elevel <= 4 || isStaff;

            // Utilities: Staff only
            case "UTILITIES" -> isStaff;

            default -> false;
        };
    }

    /**
     * Get reason for access decision
     */
    private String getAccessReason(String menuId, Integer elevel, boolean isStaff, boolean accessible) {
        if (!accessible) {
            return switch (menuId) {
                case "CASE_ASSIGNMENT", "TIME_VERIFICATION" -> "Requires ELEVEL 6 or 7 (Group Manager)";
                case "REALIGNMENT" -> "Requires ELEVEL 0-4 or Staff status";
                case "UTILITIES" -> "Requires Staff status";
                default -> "Access denied";
            };
        }

        return switch (menuId) {
            case "REALIGNMENT" -> isStaff ? "Staff access" : "ELEVEL " + elevel + " access";
            case "UTILITIES" -> "Staff access";
            case "CASE_ASSIGNMENT", "TIME_VERIFICATION" -> "Group Manager access (ELEVEL " + elevel + ")";
            default -> "Standard access";
        };
    }

    /**
     * Get list of accessible menu IDs for a user
     */
    public List<String> getAccessibleMenuIds(String seid) {
        Integer elevel = eLevelService.getELevel(seid);
        boolean isStaff = staffService.isStaff(seid);

        List<String> accessibleMenus = new ArrayList<>();
        for (String menuId : MENU_DEFINITIONS.keySet()) {
            if (isMenuAccessible(menuId, elevel, isStaff)) {
                accessibleMenus.add(menuId);
            }
        }
        return accessibleMenus;
    }

    /**
     * Check if user can see Change Access menu
     * (Users with only one access level should NOT see it)
     */
    public boolean canSeeChangeAccessMenu(String seid) {
        Integer elevel = eLevelService.getELevel(seid);
        // If ELEVEL allows drill-down (0, 2, 4, 6), show Change Access
        // ELEVEL 8 (Employee) may still see it if they have multiple assignments
        return elevel != null && elevel >= 0;
    }
}
