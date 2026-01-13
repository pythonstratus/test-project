package com.entity.rbac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Master DTO class containing all RBAC-related DTOs
 */
public class RbacDTO {

    // ==================== User Profile DTOs ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfileDTO {
        private String seid;
        private String name;
        private String title;
        private Integer elevel;
        private String elevelName;
        private String areaCode;
        private String podCode;
        private String org;
        private boolean isStaff;
        private boolean isLocked;
        private boolean hasMultipleAssignments;
        private int assignmentCount;
        private AssignmentDTO currentAssignment;
        private List<AssignmentDTO> allAssignments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignmentDTO {
        private Long roid;
        private String name;
        private String title;
        private Integer elevel;
        private String elevelName;
        private Integer areacd;
        private String podcd;
        private String org;
        private String eactive;
        private String primaryRoid;
        private boolean isCurrent;
        private boolean isStaffAssignment;
    }

    // ==================== ELEVEL DTOs ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ELevelDTO {
        private String seid;
        private Integer elevel;
        private String elevelName;
        private String description;
        private String dataAccessScope;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ELevelDefinitionDTO {
        private Integer elevel;
        private String name;
        private String description;
        private String dataAccessScope;
        private List<String> accessibleMenus;
    }

    // ==================== Menu DTOs ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuItemDTO {
        private String id;
        private String name;
        private String description;
        private boolean accessible;
        private String accessReason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuPermissionsDTO {
        private String seid;
        private Integer elevel;
        private boolean isStaff;
        private List<MenuItemDTO> menuItems;
        private int accessibleCount;
        private int totalCount;
    }

    // ==================== Staff DTOs ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffInfoDTO {
        private String seid;
        private boolean isStaff;
        private String staffRoid;
        private String currentOrg;
        private List<String> availableOrgs;
        private boolean hasUtilitiesAccess;
        private boolean hasRealignmentAccess;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffOrgDTO {
        private String code;
        private String name;
        private String description;
    }

    // ==================== Hierarchy DTOs ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HierarchyAccessDTO {
        private String seid;
        private Integer elevel;
        private List<String> accessibleAreaCodes;
        private List<String> accessiblePodCodes;
        private String scopeDescription;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AreaDTO {
        private String areaCode;
        private String areaName;
        private int employeeCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PodDTO {
        private String podCode;
        private String areaCode;
        private int employeeCount;
    }

    // ==================== Response Wrappers ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private String errorCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {
        private boolean success;
        private String message;
        private String errorCode;
        private Map<String, String> details;
    }
}
