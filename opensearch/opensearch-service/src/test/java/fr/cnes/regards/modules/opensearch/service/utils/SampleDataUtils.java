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
package fr.cnes.regards.modules.opensearch.service.utils;

import java.util.List;

import com.google.common.collect.Lists;

import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.model.domain.attributes.Fragment;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;

/**
 * Define sample data for tests.
 * @author Xavier-Alexandre Brochard
 */
public class SampleDataUtils {

    public static final String BOOLEAN_FIELD = "isTrue";

    public static final String INTEGER_FIELD = "altitude";

    public static final String INTEGER_RANGE_FIELD = "age";

    public static final String DOUBLE_FIELD = "bpm";

    public static final String DOUBLE_RANGE_FIELD = "height";

    public static final String LONG_FIELD = "speed";

    public static final String LONG_RANGE_FIELD = "distance";

    public static final String STRING_FIELD = "title";

    public static final String STRING_FIELD_1 = "author";

    public static final String STRING_RANGE_FIELD = "movie";

    public static final String LOCAL_DATE_TIME_FIELD = "date";

    public static final String LOCAL_DATE_TIME_RANGE_FIELD = "arrival";

    public static final String INTEGER_ARRAY_FIELD = "years";

    public static final String DOUBLE_ARRAY_FIELD = "duration";

    public static final String LONG_ARRAY_FIELD = "boxoffice";

    public static final String STRING_ARRAY_FIELD = "cast";

    public static final String LOCAL_DATE_TIME_ARRAY = "releases";

    public static final String TAGS_FIELD = "tags";

    public static final Fragment TEST_FRAGMENT = Fragment.buildDefault();

    // Build some attribute models for all attribute types
    public static final AttributeModel BOOLEAN_ATTRIBUTE_MODEL = AttributeModelBuilder
            .build(BOOLEAN_FIELD, PropertyType.BOOLEAN, "ForTests").fragment(TEST_FRAGMENT).get();

    public static final AttributeModel INTEGER_ATTRIBUTE_MODEL = AttributeModelBuilder
            .build(INTEGER_FIELD, PropertyType.INTEGER, "ForTests").fragment(TEST_FRAGMENT).get();

    public static final AttributeModel DOUBLE_ATTRIBUTE_MODEL = AttributeModelBuilder
            .build(DOUBLE_FIELD, PropertyType.DOUBLE, "ForTests").fragment(TEST_FRAGMENT).get();

    public static final AttributeModel LONG_ATTRIBUTE_MODEL = AttributeModelBuilder
            .build(LONG_FIELD, PropertyType.LONG, "ForTests").fragment(TEST_FRAGMENT).get();

    public static final AttributeModel STRING_ATTRIBUTE_MODEL = AttributeModelBuilder
            .build(STRING_FIELD, PropertyType.STRING, "ForTests").fragment(TEST_FRAGMENT).get();

    public static final AttributeModel STRING_ATTRIBUTE_MODEL_1 = AttributeModelBuilder
            .build(STRING_FIELD_1, PropertyType.STRING, "ForTests").fragment(TEST_FRAGMENT).get();

    public static final AttributeModel LOCAL_DATE_TIME_ATTRIBUTE_MODEL = AttributeModelBuilder
            .build(LOCAL_DATE_TIME_FIELD, PropertyType.DATE_ISO8601, "ForTests").fragment(TEST_FRAGMENT).get();

    public static final AttributeModel INTEGER_RANGE_ATTRIBUTE_MODEL = AttributeModelBuilder
            .build(INTEGER_RANGE_FIELD, PropertyType.INTEGER_INTERVAL, "ForTests").fragment(TEST_FRAGMENT).get();

    public static final AttributeModel DOUBLE_RANGE_ATTRIBUTE_MODEL = AttributeModelBuilder
            .build(DOUBLE_RANGE_FIELD, PropertyType.DOUBLE_INTERVAL, "ForTests").fragment(TEST_FRAGMENT).get();

    public static final AttributeModel LONG_RANGE_ATTRIBUTE_MODEL = AttributeModelBuilder
            .build(LONG_RANGE_FIELD, PropertyType.LONG_INTERVAL, "ForTests").fragment(TEST_FRAGMENT).get();

    public static final AttributeModel LOCAL_DATE_TIME_RANGE_ATTRIBUTE_MODEL = AttributeModelBuilder
            .build(LOCAL_DATE_TIME_RANGE_FIELD, PropertyType.DATE_INTERVAL, "ForTests").fragment(TEST_FRAGMENT).get();

    public static final AttributeModel INTEGER_ARRAY_ATTRIBUTE_MODEL = AttributeModelBuilder
            .build(INTEGER_ARRAY_FIELD, PropertyType.INTEGER_ARRAY, "ForTests").fragment(TEST_FRAGMENT).get();

    public static final AttributeModel DOUBLE_ARRAY_ATTRIBUTE_MODEL = AttributeModelBuilder
            .build(DOUBLE_ARRAY_FIELD, PropertyType.DOUBLE_ARRAY, "ForTests").fragment(TEST_FRAGMENT).get();

    public static final AttributeModel LONG_ARRAY_ATTRIBUTE_MODEL = AttributeModelBuilder
            .build(LONG_ARRAY_FIELD, PropertyType.LONG_ARRAY, "ForTests").fragment(TEST_FRAGMENT).get();

    public static final AttributeModel STRING_ARRAY_ATTRIBUTE_MODEL = AttributeModelBuilder
            .build(STRING_ARRAY_FIELD, PropertyType.STRING_ARRAY, "ForTests").fragment(TEST_FRAGMENT).get();

    public static final AttributeModel LOCAL_DATE_TIME_ARRAY_ATTRIBUTE_MODEL = AttributeModelBuilder
            .build(LOCAL_DATE_TIME_ARRAY, PropertyType.DATE_ARRAY, "ForTests").fragment(TEST_FRAGMENT).get();

    public static final AttributeModel TAGS_ATTRIBUTE_MODEL = AttributeModelBuilder
            .build(TAGS_FIELD, PropertyType.STRING_ARRAY, "ForTests").fragment(TEST_FRAGMENT).get();

    /**
     * For mocking result of a call to {@link IAttributeModelService#getAttributes(null, null)}
     */
    public static final List<AttributeModel> LIST = Lists
            .newArrayList(BOOLEAN_ATTRIBUTE_MODEL, INTEGER_ATTRIBUTE_MODEL, DOUBLE_ATTRIBUTE_MODEL,
                          LONG_ATTRIBUTE_MODEL, STRING_ATTRIBUTE_MODEL, STRING_ATTRIBUTE_MODEL_1,
                          LOCAL_DATE_TIME_ATTRIBUTE_MODEL, INTEGER_RANGE_ATTRIBUTE_MODEL, DOUBLE_RANGE_ATTRIBUTE_MODEL,
                          LONG_RANGE_ATTRIBUTE_MODEL, LOCAL_DATE_TIME_RANGE_ATTRIBUTE_MODEL,
                          INTEGER_ARRAY_ATTRIBUTE_MODEL, DOUBLE_ARRAY_ATTRIBUTE_MODEL, LONG_ARRAY_ATTRIBUTE_MODEL,
                          STRING_ARRAY_ATTRIBUTE_MODEL, LOCAL_DATE_TIME_ARRAY_ATTRIBUTE_MODEL, TAGS_ATTRIBUTE_MODEL);

    /**
     * A query like the ones the REGARDS frontend is likely to use
     */
    public static final String SMALL_REAL_LIFE_QUERY = "tags:plop AND tags:(A\\:A OR B\\:B OR C\\:C)";

    /**
     * A query with double quotes and special characters
     */
    public static final String UNESCAPED_QUERY_WITH_DOUBLE_QUOTES_AND_CHARS_TO_ESCAPE = STRING_ATTRIBUTE_MODEL
            .getJsonPath() + ":\"texte avec:des caractères+spéciaux\"";
}
