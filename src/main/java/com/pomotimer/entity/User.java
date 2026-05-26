package com.pomotimer.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String nickname;

    private String avatar;

    private Integer totalLeaves;

    private Integer totalFocusMinutes;

    private Integer consecutiveDays;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        totalLeaves = totalLeaves == null ? 0 : totalLeaves;
        totalFocusMinutes = totalFocusMinutes == null ? 0 : totalFocusMinutes;
        consecutiveDays = consecutiveDays == null ? 0 : consecutiveDays;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}