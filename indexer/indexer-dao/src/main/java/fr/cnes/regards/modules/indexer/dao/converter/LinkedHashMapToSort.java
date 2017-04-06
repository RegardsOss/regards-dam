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

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.core.convert.converter.Converter#convert(java.lang.Object)
     */
    @Override
    public Sort convert(LinkedHashMap<String, Boolean> pSource) {
        if (pSource == null) {
            return null;
        }

        List<Order> orders = new ArrayList<>();
        BiConsumer<? super String, ? super Boolean> addNewOrder = (property, ascendance) -> orders
                .add(new Order(ascendance ? Direction.ASC : Direction.DESC, property));

        pSource.forEach(addNewOrder);
        return new Sort(orders);
    }

}