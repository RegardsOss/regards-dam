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
package fr.cnes.regards.modules.indexer.dao.converter;

import java.util.LinkedHashMap;
import java.util.function.Consumer;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

/**
 * Implement the type conversion logic for a {@link Sort}t to a {@link LinkedHashMap} representation.<br>
 * The {@link Boolean} is <code>true</code> if the sort is asc and <code>false</code> if desc.
 *
 * @author Xavier-Alexandre Brochard
 */
public class SortToLinkedHashMap implements Converter<Sort, LinkedHashMap<String, Boolean>> {

    @Override
    public LinkedHashMap<String, Boolean> convert(Sort pSource) {
        if (pSource == null) {
            return null;
        }

        LinkedHashMap<String, Boolean> result = new LinkedHashMap<>();
        Consumer<? super Order> action = order -> result.put(order.getProperty(),
                                                             Direction.ASC.equals(order.getDirection()));

        pSource.forEach(action);
        return result;
    }

}