/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.feature.dao;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.IVersionByProvider;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;

@Repository
public interface IFeatureEntityRepository extends JpaRepository<FeatureEntity, Long> {

    public FeatureEntity findTop1VersionByProviderIdOrderByVersionAsc(String providerId);

    boolean existsByUrn(FeatureUniformResourceName urn);

    public FeatureEntity findByUrn(FeatureUniformResourceName urn);

    public List<FeatureEntity> findByUrnIn(List<FeatureUniformResourceName> urn);

    // FIXME remove just for test
    public long countByLastUpdateGreaterThan(OffsetDateTime from);

    /**
     * List existing provider identifiers in specified list
     */
    List<IVersionByProvider> findByProviderIdInOrderByVersionDesc(List<String> providerIds);
}
