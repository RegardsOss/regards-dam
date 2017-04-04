/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import fr.cnes.regards.modules.datasources.domain.DataSourceAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.DataSourceModelMapping;
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
public class ModelMappingAdapter extends TypeAdapter<DataSourceModelMapping> {

    /**
     * Label for name field
     */
    private static final String MODEL_LABEL = "model";

    /**
     * Label for the apings field
     */
    private static final String MAPPINGS_LABEL = "mappings";

    /**
     * Label for name field
     */
    private static final String NAME_LABEL = "name";

    /**
     * Label for primary key field
     */
    private static final String PRIMARY_KEY_LABEL = "pKey";

    /**
     * Label for last update field
     */
    private static final String LAST_UPDATE_DATE_LABEL = "last_update_date";

    /**
     * Label for type field
     */
    private static final String TYPE_LABEL = "type";

    /**
     * Label for namespace field
     */
    private static final String NAMESPACE_LABEL = "namespace";

    /**
     * Label for name field in data source
     */
    private static final String NAME_DS_LABEL = "nameDs";

    /**
     * Label for type field in data source
     */
    private static final String TYPE_DS_LABEL = "typeDs";

    @Override
    public void write(final JsonWriter pOut, final DataSourceModelMapping pValue) throws IOException {

        pOut.beginObject();
        pOut.name(MODEL_LABEL).value(pValue.getModel());
        pOut.name(MAPPINGS_LABEL);

        pOut.beginArray();
        for (final DataSourceAttributeMapping attr : pValue.getAttributesMapping()) {
            pOut.beginObject();
            pOut.name(NAME_LABEL).value(attr.getName());
            pOut.name(TYPE_LABEL).value(attr.getType().name());
            if (attr.isPrimaryKey()) {
                pOut.name(PRIMARY_KEY_LABEL).value(attr.isPrimaryKey());
            }
            if (attr.isLastUpdateDate()) {
                pOut.name(LAST_UPDATE_DATE_LABEL).value(attr.isLastUpdateDate());
            }
            if (attr.getNameSpace() != null) {
                pOut.name(NAMESPACE_LABEL).value(attr.getNameSpace());
            }
            pOut.name(NAME_DS_LABEL).value(attr.getNameDS());
            if (attr.getTypeDS() != null) {
                pOut.name(TYPE_DS_LABEL).value(attr.getTypeDS());
            }
            pOut.endObject();
        }
        pOut.endArray();
        pOut.endObject();
    }

    @Override
    public DataSourceModelMapping read(final JsonReader pIn) throws IOException {
        DataSourceModelMapping dataSourceModelMapping = new DataSourceModelMapping();
        final List<DataSourceAttributeMapping> attributes = new ArrayList<>();

        pIn.beginObject();

        if (!pIn.nextName().equals(MODEL_LABEL)) {
            throw new IOException(MODEL_LABEL + " is expected");
        }

        dataSourceModelMapping.setModel(Long.parseLong(pIn.nextString()));

        if (!pIn.nextName().equals(MAPPINGS_LABEL)) {
            throw new IOException(MAPPINGS_LABEL + " is expected");
        }

        pIn.beginArray();
        // Compute the element's array
        while (pIn.hasNext()) {
            pIn.beginObject();

            // Add the new attribute to the list
            attributes.add(readMapping(pIn));
            pIn.endObject();
        }
        pIn.endArray();
        pIn.endObject();

        dataSourceModelMapping.setAttributesMapping(attributes);

        return dataSourceModelMapping;
    }

    /**
     * Read one attribute mapping and create a {@link DataSourceAttributeMapping}
     *
     * @param pIn
     *            the {@link JsonReader} used to read a JSon and to convert in a data object
     * @return a {@link DataSourceAttributeMapping}
     * @throws IOException
     *             An error throw, the Json format format is no correct
     */
    private DataSourceAttributeMapping readMapping(final JsonReader pIn) throws IOException {
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
                case PRIMARY_KEY_LABEL:
                    attr.setIsPrimaryKey(pIn.nextBoolean());
                    break;
                case LAST_UPDATE_DATE_LABEL:
                    attr.setLastUpdateDate(pIn.nextBoolean());
                    break;
                default:
                    break;
            }
        }
        return attr;
    }

}
