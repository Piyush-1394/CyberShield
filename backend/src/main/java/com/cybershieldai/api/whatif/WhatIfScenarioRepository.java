package com.cybershieldai.api.whatif;

import com.cybershieldai.api.whatif.entity.WhatIfScenarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WhatIfScenarioRepository extends JpaRepository<WhatIfScenarioEntity, Long> {
    List<WhatIfScenarioEntity> findByOrganizationId(Long organizationId);
}
