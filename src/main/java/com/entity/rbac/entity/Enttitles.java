package com.entity.rbac.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

/**
 * ENTTITLES - Decision Table
 * Maps ICS Access Level + Title → ELEVEL
 */
@Entity
@Table(name = "ENTTITLES", schema = "ENTITYDEV")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(Enttitles.EnttitlesId.class)
public class Enttitles {

    @Column(name = "ELEVEL", nullable = false)
    private Integer elevel;

    @Id
    @Column(name = "ICSACCLEVEL", length = 1, nullable = false)
    private String icsacclevel;

    @Id
    @Column(name = "TITLE", length = 25)
    private String title;

    // Composite Key Class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnttitlesId implements Serializable {
        private String icsacclevel;
        private String title;
    }
}
