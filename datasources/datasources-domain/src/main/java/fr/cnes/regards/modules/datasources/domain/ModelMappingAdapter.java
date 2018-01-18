/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.datasources.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fr.cnes.regards.framework.gson.annotation.GsonTypeAdapterBean;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * GSON adapter for {@link DataSourceModelMapping}
 * @author Christophe Mertz
 */
@GsonTypeAdapterBean(adapted = DataSourceModelMapping.class)
public class ModelMappingAdapter extends TypeAdapter<DataSourceModelMapping> {

    /**
     * Label for name field
     */
    private static final String MODEL_LABEL = "model";

    /**
     * Label for the attributes list defined for the mapping
     */
    private static final String MAPPINGS_LABEL = "attributesMapping";

    /**
     * Label for name field
     */
    private static final String NAME_LABEL = "name";

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
    private static final String NAME_DS_LABEL = "nameDS";

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
            if (attr.getNameSpace() != null) {
                pOut.name(NAMESPACE_LABEL).value(attr.getNameSpace());
            }
            pOut.name(NAME_DS_LABEL).value(attr.getNameDS());
            pOut.endObject();
        }
        pOut.endArray();
        pOut.endObject();
    }

    @Override
    public DataSourceModelMapping read(final JsonReader in) throws IOException {
        DataSourceModelMapping dataSourceModelMapping = new DataSourceModelMapping();
        final List<AbstractAttributeMapping> attributes = new ArrayList<>();

        in.beginObject();

        if (!in.nextName().equals(MODEL_LABEL)) {
            throw new IOException(MODEL_LABEL + " is expected");
        }

        dataSourceModelMapping.setModel(Long.parseLong(in.nextString()));

        if (!in.nextName().equals(MAPPINGS_LABEL)) {
            throw new IOException(MAPPINGS_LABEL + " is expected");
        }

        in.beginArray();
        // Compute the element's array
        while (in.hasNext()) {
            in.beginObject();

            // Add the new attribute to the list
            attributes.add(readMapping(in));
            in.endObject();
        }
        in.endArray();
        in.endObject();

        dataSourceModelMapping.setAttributesMapping(attributes);

        return dataSourceModelMapping;
    }

    /**
     * Read one attribute mapping and create a {@link AbstractAttributeMapping}
     * @param in the {@link JsonReader} used to read a JSon and to convert in a data object
     * @return a {@link AbstractAttributeMapping}
     * @throws IOException An error throw, the Json format format is no correct
     */
    private AbstractAttributeMapping readMapping(final JsonReader in) throws IOException {
        String name = null;
        String namespace = null;
        String nameDS = null;
        AttributeType attributeType = null;
        while (in.hasNext()) {
            switch (in.nextName()) {
                case NAME_LABEL:
                    name = in.nextString();
                    break;
                case NAMESPACE_LABEL:
                    namespace = in.nextString();
                    break;
                case NAME_DS_LABEL:
                    nameDS = in.nextString();
                    break;
                case TYPE_LABEL:
                    String val = in.nextString();
                    if (!val.isEmpty()) {
                        attributeType = AttributeType.valueOf(val);
                    }
                    break;
                default:
                    break;
            }
        }

        if (attributeType == null && Strings.isNullOrEmpty(namespace)) {
            return new StaticAttributeMapping(name, null, nameDS);
        } else {
            return new DynamicAttributeMapping(name, namespace, attributeType, nameDS);
        }
    }

}
