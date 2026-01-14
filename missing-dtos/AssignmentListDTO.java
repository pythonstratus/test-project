package com.entity.rbac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * List of assignments with metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentListDTO {
    private String seid;
    private String userName;
    private Integer totalAssignments;
    private Long currentRoid;
    private boolean hasMultiple;
    private List<AssignmentItemDTO> assignments;

    /**
     * Single assignment item
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignmentItemDTO {
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
        private boolean isStaff;
    }
}
