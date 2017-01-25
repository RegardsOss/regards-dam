/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 *
 * Class AttributeMappingAdapter
 *
 * GSON adapter for annotation {@link DataSourceAttributeMapping}
 *
 * @author Christophe Mertz
 * 
 */
public class AttributeMappingAdapter extends TypeAdapter<List<DataSourceAttributeMapping>> {

    /**
     * Label for name field
     */
    protected static final String NAME_LABEL = "name";

    /**
     * Label for description field
     */
    protected static final String TYPE_LABEL = "type";

    /**
     * Label for description field
     */
    protected static final String NAMESPACE_LABEL = "namespace";

    /**
     * Label for description field
     */
    protected static final String MAPPING_LABEL = "mapping";

    @Override
    public void write(final JsonWriter pOut, final List<DataSourceAttributeMapping> pValue) throws IOException {

        pOut.beginArray();
        for (final DataSourceAttributeMapping attr : pValue) {
            pOut.beginObject();
            pOut.name(NAME_LABEL).value(attr.getName());
            pOut.name(TYPE_LABEL).value(attr.getType().name());
            if (attr.getNameSpace() != null) {
                pOut.name(NAMESPACE_LABEL).value(attr.getNameSpace());
            }
            pOut.name(MAPPING_LABEL).value(attr.getMapping());
            pOut.endObject();
        }
        pOut.endArray();
    }

    @Override
    public List<DataSourceAttributeMapping> read(final JsonReader pIn) throws IOException {
        final List<DataSourceAttributeMapping> attributes = new ArrayList<>();

        pIn.beginArray();

        // Compute the element's array
        while (pIn.hasNext()) {

            // Compute one element
            pIn.beginObject();
            final DataSourceAttributeMapping attr = new DataSourceAttributeMapping();
            while (pIn.hasNext()) {
                switch (pIn.nextName()) {
                    case NAME_LABEL:
                        attr.setName(pIn.nextString());
                        break;
                    case NAMESPACE_LABEL:
                        attr.setNameSpace(pIn.nextString());
                        break;
                    case MAPPING_LABEL:
                        attr.setMapping(pIn.nextString());
                        break;
                    case TYPE_LABEL:
                        attr.setType(AttributeType.valueOf(pIn.nextString()));
                        break;
                    default:
                        break;
                }
            }

            // Add the new attribute to the list
            attributes.add(attr);
            pIn.endObject();
        }
        pIn.endArray();

        return attributes;
    }

}
