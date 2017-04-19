/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import fr.cnes.regards.framework.gson.annotation.GsonTypeAdapter;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 *
 * Class AttributeMappingAdapter
 *
 * GSON adapter for annotation {@link AbstractAttributeMapping}
 *
 * @author Christophe Mertz
 *
 */
@GsonTypeAdapter(adapted = DataSourceModelMapping.class)
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
     * Label for mapping options field
     */
    private static final String MAPPING_OPTIONS = "mapping_options";

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
        for (final AbstractAttributeMapping attr : pValue.getAttributesMapping()) {
            pOut.beginObject();
            if (attr.getName() != null) {
                pOut.name(NAME_LABEL).value(attr.getName());
            }
            pOut.name(TYPE_LABEL).value(attr.getType().name());
            pOut.name(MAPPING_OPTIONS).value(attr.getMappingOptions());
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
        final List<AbstractAttributeMapping> attributes = new ArrayList<>();

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
     * Read one attribute mapping and create a {@link AbstractAttributeMapping}
     *
     * @param pIn
     *            the {@link JsonReader} used to read a JSon and to convert in a data object
     * @return a {@link AbstractAttributeMapping}
     * @throws IOException
     *             An error throw, the Json format format is no correct
     */
    private AbstractAttributeMapping readMapping(final JsonReader pIn) throws IOException {
        String name = null;
        String namespace = null;
        String nameDS = null;
        Integer typeDS = null;
        AttributeType attributeType = null;
        short mappingOptions = AbstractAttributeMapping.NO_MAPPING_OPTIONS;
        while (pIn.hasNext()) {
            switch (pIn.nextName()) {
                case NAME_LABEL:
                    name = pIn.nextString();
                    break;
                case NAMESPACE_LABEL:
                    namespace = pIn.nextString();
                    break;
                case NAME_DS_LABEL:
                    nameDS = pIn.nextString();
                    break;
                case TYPE_DS_LABEL:
                    typeDS = Integer.parseInt(pIn.nextString());
                    break;
                case TYPE_LABEL:
                    attributeType = AttributeType.valueOf(pIn.nextString());
                    break;
                case MAPPING_OPTIONS:
                    mappingOptions = (short) pIn.nextInt();
                    break;
                default:
                    break;
            }
        }
        if (mappingOptions == AbstractAttributeMapping.NO_MAPPING_OPTIONS) {
            return new DynamicAttributeMapping(name, namespace, attributeType, nameDS, typeDS);
        } else {
            return new StaticAttributeMapping(attributeType, nameDS, typeDS, mappingOptions);
        }
    }

}