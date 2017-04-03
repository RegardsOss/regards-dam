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

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.core.convert.converter.Converter#convert(java.lang.Object)
     */
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