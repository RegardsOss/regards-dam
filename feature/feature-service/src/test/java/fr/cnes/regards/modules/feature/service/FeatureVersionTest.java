/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.feature.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.modules.feature.dao.ILightFeatureCreationRequestRepository;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;
import fr.cnes.regards.modules.feature.dto.urn.FeatureIdentifier;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;

@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=feature_version" })
@ActiveProfiles(value = { "noscheduler", "nohandler" })
public class FeatureVersionTest extends AbstractFeatureMultitenantServiceTest {

    @Autowired
    protected ILightFeatureCreationRequestRepository featureCreationRequestLightRepo;

    @Autowired
    private IFeatureCreationService featureService;

    @Test
    public void multipleVersionTest() {

        // Init 2 requests
        List<FeatureCreationRequestEvent> events = new ArrayList<>();
        super.initFeatureCreationRequestEvent(events, 2);
        featureService.registerRequests(events);

        // V1 & V2 for first feature
        savePreviousVersions(events.get(0).getFeature(), 2);
        // V1 & V2 & V3 for second one
        savePreviousVersions(events.get(1).getFeature(), 3);

        featureService.scheduleRequests();

        waitFeature(7, null, 60_000);

        // No assertion ... constraint violation occurs if concurrent versions are saved!
    }

    /**
     * Save a set of versions
     * @param feature feature version to save
     * @param versionNumber number of version to save. Must be greater than or equals to 1
     */
    private void savePreviousVersions(Feature feature, Integer versionNumber) {
        UUID uuid = UUID.nameUUIDFromBytes(feature.getId().getBytes());

        for (int i = 1; i <= versionNumber; i++) {
            // Version 1
            feature.setUrn(FeatureUniformResourceName.build(FeatureIdentifier.FEATURE, feature.getEntityType(),
                                                            runtimeTenantResolver.getTenant(), uuid, i));
            featureRepo.save(FeatureEntity.build("sessionOwner", "session", feature, null));
        }
    }
}
