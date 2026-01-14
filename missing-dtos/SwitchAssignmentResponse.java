package com.entity.rbac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response after switching assignment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwitchAssignmentResponse {
    private boolean success;
    private String message;
    private Long previousRoid;
    private Long newRoid;
    private AssignmentListDTO.AssignmentItemDTO newAssignment;
    private Integer newElevel;
    private String newElevelName;
    private List<String> availableMenus;
}
