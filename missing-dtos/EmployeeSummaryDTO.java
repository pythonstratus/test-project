package com.entity.rbac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Employee summary for listings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSummaryDTO {
    private Long roid;
    private String seid;
    private String name;
    private String title;
    private Integer grade;
    private Integer elevel;
    private String elevelName;
    private Integer areacd;
    private String areaName;
    private String podcd;
    private String org;
    private String eactive;
    private boolean isStaff;
}
