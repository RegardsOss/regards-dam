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
package fr.cnes.regards.modules.feature.service.plugin;

import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.feature.domain.request.FeatureReferenceRequest;
import fr.cnes.regards.modules.feature.dto.Feature;

/**
 * Default plugin notification sender
 * @author Kevin Marchois
 *
 */
@Plugin(author = "REGARDS Team", description = "Default recipient sender", id = "DefaultFeatureGenerator",
        version = "1.0.0", contact = "regards@c-s.fr", license = "GPLv3", owner = "CNES",
        url = "https://regardsoss.github.io/")
public class DefaultFeatureGenerator implements IFeatureCreationRequestEventGenerator {

    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    @Override
    public Feature createFeatureRequestEvent(FeatureReferenceRequest reference) {

        return Feature.build("id " + reference.getLocation(), null, IGeometry.point(IGeometry.position(10.0, 20.0)),
                             EntityType.DATA, runtimeTenantResolver.getTenant());
    }

}
