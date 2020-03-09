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
package fr.cnes.regards.modules.opensearch.service.parser;

import org.springframework.util.MultiValueMap;

import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.modules.dam.domain.entities.StaticProperties;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;

/**
 * This {@link IParser} implementation only handles the "exists" parameter (exists doesn't seem to exist in openSearch)
 * and returns an {@link ICriterion} testing that given field exists.
 * @author oroussel
 */
public class ImageOnlyParser implements IParser {

    public static final String IMAGE_ONLY_PARAM = "hasImage";

    @Override
    public ICriterion parse(MultiValueMap<String, String> parameters) {
        // either imageOnly is set or not
        if (parameters.containsKey(IMAGE_ONLY_PARAM)) {
            // add a criterion only if asked
            return Boolean.parseBoolean(parameters.getFirst(IMAGE_ONLY_PARAM)) ?
                    ICriterion.or(ICriterion.attributeExists(StaticProperties.FEATURE_FILES_PATH + "." + DataType.THUMBNAIL),
                                  ICriterion
                                          .attributeExists(StaticProperties.FEATURE_FILES_PATH + "." + DataType.QUICKLOOK_HD),
                                  ICriterion
                                          .attributeExists(StaticProperties.FEATURE_FILES_PATH + "." + DataType.QUICKLOOK_MD),
                                  ICriterion.attributeExists(
                                          StaticProperties.FEATURE_FILES_PATH + "." + DataType.QUICKLOOK_SD)) : null;
        } else {
            // if it is not set no criterion to add
            return null;
        }
    }
}
