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
package fr.cnes.regards.modules.entities.domain;

import java.util.Set;

import com.google.common.collect.Sets;

import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 *
 * List all available static properties
 *
 * @author Marc Sordi
 *
 */
public final class StaticProperties {

    // URN (duplicate as ID in feature)
    public static final String IP_ID = "ipId";

    // ##########-AbstractEntity-##########

    // Model
    public static final String MODEL_TYPE = "model";

    // String
    public static final String MODEL_NAME = MODEL_TYPE + ".name";

    // String
    public static final String MODEL_DESCRIPTION = MODEL_TYPE + ".description";

    // String
    public static final String MODEL_VERSION = MODEL_TYPE + ".version";

    // Date
    public static final String LAST_UPDATE = "lastUpdate";

    // Date
    public static final String CREATION_DATE = "creationDate";

    // Long
    public static final String ID = "id";

    // String list
    public static final String GROUPS = "groups";

    // ##########-EntityFeature-##########

    /**
     * Feature namespace
     */
    public static final String FEATURE_NS = "feature";

    // String
    public static final String SIP_ID = "sipId";

    // String
    public static final String LABEL = "label";

    // List of DataFile
    public static final String FILES = "files";

    // String list
    public static final String TAGS = "tags";

    // Geometry
    public static final String GEOMETRY = "geometry";

    // Wrappped dynamic properties
    public static final String PROPERTIES = "properties";

    // ##########-AbstractDataEntity-##########

    // DescriptionFile
    public static final String DESCRIPTION_FILE = "descriptionFile";

    // ##########-AbstractDescEntity-##########

    public static final String DATASOURCE_ID = "dataSourceId";

    // ##########-Collection-##########

    // ##########-DataObject-##########

    // Long list
    public static final String DATASET_MODEL_IDS = "datasetModelIds";

    // int
    public static final String SCORE = "score";

    // ##########-Dataset-##########

    // String
    public static final String DATA_MODEL = "dataModel";

    // String list
    public static final String QUOTATIONS = "quotations";

    // String
    public static final String LICENCE = "licence";

    public static final String ENTITY_TYPE = "entityType";

    // ##########-Document-##########

    // ##########-From GSON factory EntityAdapterFactory-##########

    private static final Set<String> staticPropertiesName = Sets
            .newHashSet(CREATION_DATE, DATA_MODEL, DATASET_MODEL_IDS, DATASOURCE_ID, DESCRIPTION_FILE, ENTITY_TYPE,
                        FILES, GEOMETRY, GROUPS, ID, IP_ID, LABEL, LAST_UPDATE, LICENCE, MODEL_DESCRIPTION, MODEL_NAME,
                        MODEL_VERSION, QUOTATIONS, SCORE, SIP_ID, TAGS);

    private StaticProperties() {
    }

    public static boolean isStaticProperty(String propertyName) {
        return staticPropertiesName.contains(propertyName);
    }

    /**
     * Build a "fake" {@link AttributeModel} corresponding to the given static property.
     * @param propertyName static property name
     * @return Built {@link AttributeModel} corresponding to the given static property. null if the attribute was not
     *         created
     */
    public static AttributeModel buildStaticAttributeModel(String propertyName) {
        switch (propertyName) {
            case CREATION_DATE:
                return AttributeModelBuilder.build(propertyName, AttributeType.DATE_ISO8601, propertyName).get();
            case DATA_MODEL:
                return AttributeModelBuilder.build(propertyName, AttributeType.STRING, propertyName).get();
            case DATASET_MODEL_IDS:
                return AttributeModelBuilder.build(propertyName, AttributeType.LONG_ARRAY, propertyName).get();
            case DATASOURCE_ID:
                return AttributeModelBuilder.build(propertyName, AttributeType.LONG, propertyName).get();
            case ENTITY_TYPE:
                return AttributeModelBuilder.build(propertyName, AttributeType.STRING, propertyName).get();
            case GROUPS:
                return AttributeModelBuilder.build(propertyName, AttributeType.STRING_ARRAY, propertyName).get();
            case ID:
                return AttributeModelBuilder.build(propertyName, AttributeType.LONG, propertyName).get();
            case IP_ID:
                return AttributeModelBuilder.build(propertyName, AttributeType.STRING, propertyName).get();
            case LABEL:
                return AttributeModelBuilder.build(propertyName, AttributeType.STRING, propertyName).get();
            case LAST_UPDATE:
                return AttributeModelBuilder.build(propertyName, AttributeType.DATE_ISO8601, propertyName).get();
            case LICENCE:
                return AttributeModelBuilder.build(propertyName, AttributeType.STRING, propertyName).get();
            case MODEL_DESCRIPTION:
                return AttributeModelBuilder.build(propertyName, AttributeType.STRING, propertyName).get();
            case MODEL_NAME:
                return AttributeModelBuilder.build(propertyName, AttributeType.STRING, propertyName).get();
            case MODEL_VERSION:
                return AttributeModelBuilder.build(propertyName, AttributeType.STRING, propertyName).get();
            case QUOTATIONS:
                return AttributeModelBuilder.build(propertyName, AttributeType.STRING_ARRAY, propertyName).get();
            case SCORE:
                return AttributeModelBuilder.build(propertyName, AttributeType.INTEGER, propertyName).get();
            case SIP_ID:
                return AttributeModelBuilder.build(propertyName, AttributeType.STRING, propertyName).get();
            case TAGS:
                return AttributeModelBuilder.build(propertyName, AttributeType.STRING_ARRAY, propertyName).get();
            default:
                return null;
        }
    }

}
