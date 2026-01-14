package com.entity.rbac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Hierarchy node for tree navigation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HierarchyNodeDTO {
    private String code;
    private String level;
    private String displayName;
    private String parentCode;
    private Integer childCount;
    private Integer elevel;
    private List<HierarchyNodeDTO> children;
}
