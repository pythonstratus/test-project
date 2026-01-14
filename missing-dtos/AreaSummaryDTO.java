package com.entity.rbac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Area summary with counts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaSummaryDTO {
    private String areaCode;
    private String areaName;
    private Integer employeeCount;
    private Integer territoryCount;
    private Integer groupCount;
}
