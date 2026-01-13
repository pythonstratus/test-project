package com.entity.rbac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTOs for Change Access / Hierarchy Navigation functionality
 */
public class HierarchyDTO {

    // ==================== Hierarchy Navigation DTOs ====================

    /**
     * Represents a node in the organizational hierarchy
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HierarchyNodeDTO {
        private String code;              // "21030000"
        private String level;             // "AREA", "TERRITORY", "GROUP", "RO"
        private String displayName;       // "Territory 2103" or "C Castro"
        private Integer childCount;       // Number of items at next level
        private Integer elevelEquivalent; // E-Level for this hierarchy level
        private String parentCode;        // Parent's code (null for National)
    }

    /**
     * Response containing list of hierarchy nodes
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HierarchyListDTO {
        private String parentLevel;       // "NATIONAL", "AREA", etc.
        private String parentCode;        // Parent code (null for National)
        private String childLevel;        // Level of items in the list
        private Integer totalCount;       // Total number of items
        private List<HierarchyNodeDTO> items;
    }

    // ==================== Change Access Context DTOs ====================

    /**
     * Request to change viewing context
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeAccessRequestDTO {
        private String level;             // NATIONAL, AREA, TERRITORY, GROUP, RO
        private String code;              // Hierarchy code (not needed for NATIONAL)
        private String org;               // CF, CP, WI, AD (optional, for National level)
    }

    /**
     * Response after changing access context
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeAccessResponseDTO {
        private boolean success;
        private String message;
        private HierarchyNodeDTO newContext;
        private Integer originalElevel;   // User's actual E-Level
        private Integer contextElevel;    // E-Level of the viewing context
        private List<String> availableMenus;
        private DataScopeDTO dataScope;
    }

    /**
     * Current user context information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserContextDTO {
        private String seid;
        private String userName;
        private Integer actualElevel;
        private String actualElevelName;
        private HierarchyNodeDTO currentContext;
        private boolean canChangeAccess;
        private List<String> availableLevels;
        private String currentOrg;        // CF, CP, WI, AD
    }

    /**
     * Data scope based on current context
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataScopeDTO {
        private String level;
        private String areaCode;
        private String territoryCode;
        private String groupCode;
        private String roCode;
        private String org;
        private String scopeDescription;  // "All data in Territory 2103"
    }

    // ==================== Validation DTOs ====================

    /**
     * Response for hierarchy code validation
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HierarchyValidationDTO {
        private boolean valid;
        private String code;
        private String level;
        private Integer levelCode;        // E-Level equivalent
        private String displayName;
        private Integer childCount;
        private String errorMessage;      // If invalid
    }

    /**
     * Response for Change Access menu visibility check
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeAccessVisibilityDTO {
        private boolean visible;
        private String reason;
        private Integer accessibleLevels;
        private List<String> availableLevels;
    }

    // ==================== Organization DTOs ====================

    /**
     * Organization information for National level
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganizationDTO {
        private String code;              // CF, CP, WI, AD
        private String name;              // Full name
        private String description;
        private boolean active;
    }

    /**
     * List of available organizations
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganizationListDTO {
        private List<OrganizationDTO> organizations;
        private String currentOrg;        // User's current org selection
    }
}
