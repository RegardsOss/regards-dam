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
package fr.cnes.regards.modules.opensearch.service.parser;

import org.springframework.util.MultiValueMap;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;

/**
 * This {@link IParser} implementation only handles the "exists" parameter (exists doesn't seem to exist in openSearch)
 * and returns an {@link ICriterion} testing that given field exists.
 * @author oroussel
 */
public class FieldExistsParser implements IParser {

    private static final String EXISTS_PARAM = "exists";

    @Override
    public ICriterion parse(MultiValueMap<String, String> parameters) {
        return parameters.containsKey(EXISTS_PARAM) ? ICriterion.attributeExists(parameters.getFirst(EXISTS_PARAM))
                : null;
    }
}
