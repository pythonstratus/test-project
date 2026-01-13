package com.entity.rbac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTOs for Change Role functionality
 * Supports both General User (simple) and Staff User (advanced) modes
 */
public class ChangeRoleDTO {

    // ==================== Current Role Display ====================

    /**
     * Represents the user's current role for display
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentRoleDTO {
        private String name;              // "Vainer Sarah"
        private String title;             // "Revenue Officer"
        private String grade;             // "Grade 11"
        private String roid;              // "25072307"
        private String displayText;       // "Revenue Officer - Grade 11 - 25072307"
        private Integer elevel;
        private String areaCode;
        private String podCode;
        private String org;
    }

    // ==================== General User (Simple Mode) ====================

    /**
     * A role option for general users (shown in single dropdown)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleOptionDTO {
        private String roid;              // "25072307"
        private String title;             // "Revenue Officer"
        private String grade;             // "Grade 11"
        private String displayText;       // "Revenue Officer - Grade 11 - 25072307"
        private Integer elevel;
        private boolean isCurrent;        // Is this the current active role?
    }

    /**
     * Response for general user's available roles
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneralUserRolesDTO {
        private CurrentRoleDTO currentRole;
        private List<RoleOptionDTO> availableRoles;
        private int totalRoles;
    }

    /**
     * Request to change role (general user - simple mode)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneralChangeRoleRequestDTO {
        private String roid;              // The ROID to switch to
    }

    // ==================== Staff User (Advanced Mode) ====================

    /**
     * Level option for staff dropdown
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LevelOptionDTO {
        private String code;              // "NATIONAL", "AREA", "TERRITORY", "GROUP", "EMPLOYEE"
        private String displayName;       // "National", "Area", etc.
        private Integer elevel;           // 0, 2, 4, 6, 8
        private String valueHint;         // "0 digits", "2 digits", "4 digits", etc.
        private Integer requiredDigits;   // 0, 2, 4, 6, 8
    }

    /**
     * Assignment number option for staff dropdown
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignmentOptionDTO {
        private String roid;              // "85906265"
        private String displayText;       // "85906265" or with description
        private String title;
        private Integer elevel;
        private boolean isCurrent;
    }

    /**
     * Org/Function option for staff dropdown
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrgFunctionOptionDTO {
        private String code;              // "FC", "CCP", "WI"
        private String displayName;       // "FC - Field Collection"
        private String description;       // "Field Collection"
        private boolean isCurrent;
    }

    /**
     * Response for staff user's change role options
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffUserOptionsDTO {
        private CurrentRoleDTO currentRole;
        private List<LevelOptionDTO> levels;
        private List<AssignmentOptionDTO> assignments;
        private List<OrgFunctionOptionDTO> orgFunctions;
        private String levelValueHint;    // "8-Digits RO, 6-digit Group, ..."
        private String currentDefaultRoid; // If user has a saved default
    }

    /**
     * Request to change role (staff user - advanced mode)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffChangeRoleRequestDTO {
        private String level;             // "NATIONAL", "AREA", "TERRITORY", "GROUP", "EMPLOYEE"
        private String levelValue;        // The hierarchy code
        private String assignmentRoid;    // Selected assignment ROID
        private String orgFunction;       // "FC", "CCP", "WI"
        private boolean keepAsDefault;    // Save this selection as default
    }

    // ==================== Response DTOs ====================

    /**
     * Response after changing role
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeRoleResponseDTO {
        private boolean success;
        private String message;
        private CurrentRoleDTO newRole;
        private String newLevel;
        private Integer newElevel;
        private String newOrg;
        private DataScopeDTO dataScope;
        private List<String> availableMenus;
    }

    /**
     * Data scope after role change
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataScopeDTO {
        private String level;
        private String levelValue;
        private String areaCode;
        private String territoryCode;
        private String groupCode;
        private String org;
        private String scopeDescription;
    }

    // ==================== Configuration DTO ====================

    /**
     * Change Role dialog configuration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeRoleConfigDTO {
        private String seid;
        private String userName;
        private boolean isStaff;          // Determines which UI to show
        private String mode;              // "GENERAL" or "STAFF"
        private CurrentRoleDTO currentRole;
        private GeneralUserRolesDTO generalOptions;
        private StaffUserOptionsDTO staffOptions;
        private boolean canChangeRole;
        private String disabledReason;
    }

    // ==================== Validation DTOs ====================

    /**
     * Level value validation result
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LevelValueValidationDTO {
        private boolean valid;
        private String level;
        private String value;
        private String normalizedValue;
        private String displayName;
        private String errorMessage;
    }

    /**
     * Full change role request validation
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeRoleValidationDTO {
        private boolean valid;
        private List<String> errors;
        private List<String> warnings;
    }
}
