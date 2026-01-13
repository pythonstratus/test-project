package com.entity.rbac.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ENTITY_USER", schema = "ENTITYDEV")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityUser {

    @Id
    @Column(name = "USER_SEID", length = 5)
    private String userSeid;

    @Column(name = "ISSTAFF", length = 1)
    private String isstaff;

    @Column(name = "ISLOCKED", length = 1)
    private String islocked;

    // Helper methods
    /**
     * WARNING: Do NOT use this for staff detection!
     * Staff is detected by ROID prefix 859062, not this field.
     */
    @Deprecated
    public boolean isStaffFlag() {
        return "Y".equalsIgnoreCase(isstaff);
    }

    public boolean isLocked() {
        return "Y".equalsIgnoreCase(islocked);
    }

    public String getTrimmedSeid() {
        return userSeid != null ? userSeid.trim() : null;
    }
}
