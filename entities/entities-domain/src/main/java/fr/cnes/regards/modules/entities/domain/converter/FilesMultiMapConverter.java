package fr.cnes.regards.modules.entities.domain.converter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.modules.indexer.domain.DataFile;

import javax.persistence.AttributeConverter;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * This {@link AttributeConverter} allows to convert a LocalDate to persist with JPA.
 *
 * @author Christophe Mertz
 *
 */
//@Converter(autoApply = true)
public class FilesMultiMapConverter implements AttributeConverter<Multimap<DataType, DataFile>, String> {

    private Gson gson = new Gson();

    //private Type ourType = new TypeToken<HashMultimap<DataType, DataFile>>() {}.getType();
    private Type ourType = new TypeToken<Map<DataType, Collection<DataFile>>>() {}.getType();

    @Override
    public String convertToDatabaseColumn(Multimap<DataType, DataFile> attribute) {
        return gson.toJson(attribute.asMap());
    }

    @Override
    public Multimap<DataType, DataFile> convertToEntityAttribute(String dbData) {
        final HashMultimap<DataType, DataFile> result = HashMultimap.create();
        final Map<String, Collection<DataFile>> map = gson.fromJson(dbData, ourType);
        for (final Map.Entry<String, Collection<DataFile>> e : map.entrySet()) {
            final Collection<DataFile> value = e.getValue();
            result.putAll(DataType.valueOf(e.getKey()), value);
        }
        return result;
    }
}