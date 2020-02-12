/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.feature.dto.urn;

import java.util.UUID;
import java.util.regex.Pattern;

import javax.persistence.Convert;

import fr.cnes.regards.framework.oais.validator.RegardsOaisUrn;
import fr.cnes.regards.framework.urn.EntityType;
import fr.cnes.regards.framework.urn.UniformResourceName;
import fr.cnes.regards.modules.feature.dto.urn.converter.FeatureUrnConverter;

/**
 * allow us to create URN with the following format:
 * URN:FeatureIdentifier:entityType:tenant:UUID(entityId):Vversion[,order][:REVrevision]
 *
 * <br/>
 * Example:
 * <ul>
 * <li>URN:FEATURE:Collection:CDPP::1</li>
 * <li>URN:FEATURE:Collection:CDPP::1,5:REV2</li>
 * </ul>
 *
 * @author Kevin Marchois
 */
@RegardsOaisUrn
@Convert(converter = FeatureUrnConverter.class)
public class FeatureUniformResourceName extends UniformResourceName {

    public static final int MAX_SIZE = 132;

    public static FeatureUniformResourceName build(FeatureIdentifier identifier, EntityType entityType, String tenant,
            UUID entityId, int version) {
        FeatureUniformResourceName urn = new FeatureUniformResourceName();
        urn.build(identifier.name(), entityType, tenant, entityId, version, null, null);
        return urn;
    }

    /**
     * take this kind of String
     * URN:OAISIdentifier:entityType:tenant:UUID(entityId):version[,order][:REVrevision]
     * and return a new instance of {@link FeatureUniformResourceName}
     *
     * @param urn String respecting the following regex
     *            URN:.+:.+:.+:.+:\\d{1,3}(,\\d+)?(:REV.+)?
     * @return a new instance of {@link FeatureUniformResourceName}
     * @throws IllegalArgumentException if the given string does not respect the urn
     *                                  pattern
     */
    public static FeatureUniformResourceName fromString(String urn) {
        final Pattern pattern = Pattern.compile(URN_PATTERN);
        if (!pattern.matcher(urn).matches()) {
            throw new IllegalArgumentException();
        }
        final String[] stringFragment = urn.split(DELIMITER);
        final FeatureIdentifier identifier = FeatureIdentifier.valueOf(stringFragment[1]);
        final EntityType entityType = EntityType.valueOf(stringFragment[2]);
        final String tenant = stringFragment[3];
        final UUID entityId = UUID.fromString(stringFragment[4]);
        final String[] versionWithOrder = stringFragment[5].split(",");
        if (versionWithOrder.length == 2) {
            // Order is precised
            if (stringFragment.length == 7) {
                // Revision is precised
                final String revisionString = stringFragment[6];
                // so we have all fields
                return FeatureUniformResourceName
                        .build(identifier, entityType, tenant, entityId,
                               Integer.parseInt(versionWithOrder[0].substring(VERSION_PREFIX.length())))
                        .withOrder(Long.parseLong(versionWithOrder[1]))
                        .withRevision(revisionString.substring(REVISION_PREFIX.length()));
            } else {
                // Revision is missing so we have all except Revision
                return FeatureUniformResourceName
                        .build(identifier, entityType, tenant, entityId,
                               Integer.parseInt(versionWithOrder[0].substring(VERSION_PREFIX.length())))
                        .withOrder(Long.parseLong(versionWithOrder[1]));
            }
        } else {
            // we don't have an order specified
            if (stringFragment.length == 7) {
                // Revision is precised
                final String revisionString = stringFragment[6];
                // so we have all fields exception Order
                return FeatureUniformResourceName
                        .build(identifier, entityType, tenant, entityId,
                               Integer.parseInt(versionWithOrder[0].substring(VERSION_PREFIX.length())))
                        .withRevision(revisionString.substring(REVISION_PREFIX.length()));
            } else {
                // Revision is missing so we have all except Revision and Order
                return FeatureUniformResourceName
                        .build(identifier, entityType, tenant, entityId,
                               Integer.parseInt(versionWithOrder[0].substring(VERSION_PREFIX.length())));
            }
        }
    }

    /**
     * Build a pseudo random UUID starting with 00000000-0000-0000-0000 for test purpose only.
     * Use {@link #build(FeatureIdentifier, EntityType, String, UUID, int, Long, String)} in production.
     */
    public static FeatureUniformResourceName pseudoRandomUrn(FeatureIdentifier identifier, EntityType entityType,
            String tenant, int version) {
        return FeatureUniformResourceName.build(identifier, entityType, tenant,
                                                UUID.fromString("0-0-0-0-" + (int) (Math.random() * Integer.MAX_VALUE)),
                                                version);
    }

    public static FeatureUniformResourceName clone(FeatureUniformResourceName template, Long order) {
        return FeatureUniformResourceName
                .build(FeatureIdentifier.valueOf(template.getIdentifier()), template.getEntityType(),
                       template.getTenant(), template.getEntityId(), template.getVersion())
                .withOrder(order);
    }
}
