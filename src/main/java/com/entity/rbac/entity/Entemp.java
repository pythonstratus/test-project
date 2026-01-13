package com.entity.rbac.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * ENTEMP - Employee Master Table
 * Contains employee information including ELEVEL, Area/POD assignments
 */
@Entity
@Table(name = "ENTEMP", schema = "ENTITYDEV")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entemp {

    @Id
    @Column(name = "ROID")
    private Long roid;

    @Column(name = "NAME", length = 35)
    private String name;

    @Column(name = "GRADE")
    private Integer grade;

    @Column(name = "TYPE", length = 1)
    private String type;

    @Column(name = "ICSACC", length = 1)
    private String icsacc;

    @Column(name = "BADGE", length = 10)
    private String badge;

    @Column(name = "TITLE", length = 25)
    private String title;

    @Column(name = "AREACD")
    private Integer areacd;

    @Column(name = "PHONE")
    private Integer phone;

    @Column(name = "EXT")
    private Integer ext;

    @Column(name = "SEID", length = 5)
    private String seid;

    @Column(name = "EMAIL", length = 50)
    private String email;

    @Column(name = "POSTYPE", length = 1)
    private String postype;

    @Column(name = "AREA", length = 1)
    private String area;

    @Column(name = "TOUR")
    private Integer tour;

    @Column(name = "PODIND", length = 1)
    private String podind;

    @Column(name = "TPSIND", length = 1)
    private String tpsind;

    @Column(name = "CSUIND", length = 1)
    private String csuind;

    @Column(name = "AIDEIND", length = 1)
    private String aideind;

    @Column(name = "FLEXIND", length = 1)
    private String flexind;

    @Column(name = "EMPDT")
    private LocalDate empdt;

    @Column(name = "ADJDT")
    private LocalDate adjdt;

    @Column(name = "ADJREASON", length = 4)
    private String adjreason;

    @Column(name = "ADJPERCENT")
    private Integer adjpercent;

    @Column(name = "PREVID")
    private Long previd;

    @Column(name = "EACTIVE", length = 1)
    private String eactive;

    @Column(name = "UNIX", length = 8)
    private String unix;

    @Column(name = "ELEVEL")
    private Integer elevel;

    @Column(name = "EXTRDT")
    private LocalDate extrdt;

    @Column(name = "PRIMARY_ROID", length = 1)
    private String primaryRoid;

    @Column(name = "PODCD", length = 3)
    private String podcd;

    @Column(name = "ORG", length = 2)
    private String org;

    @Column(name = "LASTLOGIN")
    private LocalDate lastlogin;

    @Column(name = "GS9CNT")
    private Integer gs9cnt;

    @Column(name = "GS11CNT")
    private Integer gs11cnt;

    @Column(name = "GS12CNT")
    private Integer gs12cnt;

    @Column(name = "GS13CNT")
    private Integer gs13cnt;

    @Column(name = "LOGOFF")
    private LocalDate logoff;

    @Column(name = "IP_ADDR", length = 39)
    private String ipAddr;

    // ==================== Helper Methods ====================

    /**
     * Check if EACTIVE = 'Y' (valid but not session-active)
     */
    public boolean isValid() {
        return "Y".equalsIgnoreCase(eactive);
    }

    /**
     * Check if EACTIVE = 'A' (session active)
     */
    public boolean isSessionActive() {
        return "A".equalsIgnoreCase(eactive);
    }

    /**
     * Check if PRIMARY_ROID = 'Y'
     */
    public boolean isPrimary() {
        return "Y".equalsIgnoreCase(primaryRoid);
    }

    /**
     * Check if this is the current active assignment (EACTIVE='A' AND PRIMARY_ROID='Y')
     */
    public boolean isCurrentActiveAssignment() {
        return isSessionActive() && isPrimary();
    }

    /**
     * Check if ELEVEL = -2 (Blocked/Vacant)
     */
    public boolean isBlocked() {
        return elevel != null && elevel == -2;
    }

    /**
     * Check if ELEVEL = -1 (Not Supported)
     */
    public boolean isNotSupported() {
        return elevel != null && elevel == -1;
    }

    /**
     * Check if this is a staff assignment (ROID starts with 859062)
     */
    public boolean isStaffAssignment() {
        return roid != null && String.valueOf(roid).startsWith("859062");
    }

    /**
     * Get trimmed SEID
     */
    public String getTrimmedSeid() {
        return seid != null ? seid.trim() : null;
    }

    /**
     * Get trimmed name
     */
    public String getTrimmedName() {
        return name != null ? name.trim() : null;
    }

    /**
     * Get trimmed title
     */
    public String getTrimmedTitle() {
        return title != null ? title.trim() : null;
    }

    /**
     * Get trimmed PODCD
     */
    public String getTrimmedPodcd() {
        return podcd != null ? podcd.trim() : null;
    }

    /**
     * Get trimmed ORG
     */
    public String getTrimmedOrg() {
        return org != null ? org.trim() : null;
    }
}
