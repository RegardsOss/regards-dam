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
            return null;
        }
        List<Order> orders = new ArrayList<>();
        BiConsumer<? super String, ? super Boolean> addNewOrder = (property, ascendance) -> orders
                .add(new Order(ascendance ? Direction.ASC : Direction.DESC, property));

        sortMap.forEach(addNewOrder);
        return new Sort(orders);
    }

}