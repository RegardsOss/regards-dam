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
package fr.cnes.regards.modules.feature.domain.request;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import fr.cnes.regards.framework.jpa.json.JsonBinaryType;
import fr.cnes.regards.modules.feature.dto.StorageMetadata;

/**
 * @author Kevin Marchois
 *
 */
@TypeDefs({ @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class) })
@Embeddable
public class FeatureCreationMetadataEntity extends FeatureMetadataEntity {

    /** If we override duplicated feature or create a new version */
    @Column(name = "override_previous_version", nullable = true)
    private boolean override;

    public static FeatureCreationMetadataEntity build(String sessionOwner, String session,
            List<StorageMetadata> storages, boolean override) {
        FeatureCreationMetadataEntity f = new FeatureCreationMetadataEntity();
        f.setSessionOwner(sessionOwner);
        f.setSession(session);
        f.setStorages(storages);
        f.setOverride(override);

        return f;
    }

    public boolean isOverride() {
        return override;
    }

    public void setOverride(boolean overridePreviousVersion) {
        this.override = overridePreviousVersion;
    }

}
