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
package fr.cnes.regards.modules.indexer.dao.converter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiConsumer;

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
public class LinkedHashMapToSort implements Converter<LinkedHashMap<String, Boolean>, Sort> {

    @Override
    public Sort convert(LinkedHashMap<String, Boolean> sortMap) {
        if (sortMap == null) {
            return Sort.by(Direction.ASC, "id");
        }
        List<Order> orders = new ArrayList<>();
        BiConsumer<? super String, ? super Boolean> addNewOrder = (property, ascendance) -> orders
                .add(new Order(ascendance ? Direction.ASC : Direction.DESC, property));

        sortMap.forEach(addNewOrder);
        return Sort.by(orders);
    }

}