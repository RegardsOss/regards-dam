/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins.domain;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Types;
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
     * Label for type field
     */
    protected static final String TYPE_LABEL = "type";

    /**
     * Label for namespace field
     */
    protected static final String NAMESPACE_LABEL = "namespace";

    /**
     * Label for name field in data source
     */
    protected static final String NAME_DS_LABEL = "nameDs";

    /**
     * Label for type field in data source
     */
    protected static final String TYPE_DS_LABEL = "typeDs";

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
            pOut.name(NAME_DS_LABEL).value(attr.getNameDS());
            if (attr.getTypeDS()!=null) {
                pOut.name(TYPE_DS_LABEL).value(attr.getTypeDS());
            }
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
                    case NAME_DS_LABEL:
                        attr.setNameDS(pIn.nextString());
                        break;
                    case TYPE_DS_LABEL:
                        attr.setTypeDS(Integer.parseInt(pIn.nextString()));
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

    private Types getJdbcType(String jdbcType) throws IOException {
        Types val = null;

        // Get all field in java.sql.Types
        Field[] fields = Types.class.getFields();
        for (int i = 0; (val != null) && (i < fields.length); i++) {
            try {
                Integer value = (Integer) fields[i].get(null);
                if (Integer.parseInt(jdbcType) == value.intValue()) {
                    val = (Types) fields[i].get(null);
                }
            } catch (IllegalAccessException e) {
                throw new IOException(e);
            }
        }
        return val;
    }

}
