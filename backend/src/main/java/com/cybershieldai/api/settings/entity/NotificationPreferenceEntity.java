package com.cybershieldai.api.settings.entity;

import com.cybershieldai.api.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "notification_preferences")
public class NotificationPreferenceEntity {
    @Id
    private Long userId;
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @MapsId
    private UserEntity user;
    @Column(nullable = false)
    private boolean emailAlerts = true;
    @Column(nullable = false)
    private boolean inAppAlerts = true;
    @Column(nullable = false)
    private boolean weeklyDigest = true;
}
