package com.cybershieldai.api.settings;

import com.cybershieldai.api.settings.entity.NotificationPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreferenceEntity, Long> {
}
