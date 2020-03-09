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
package fr.cnes.regards.modules.dam.domain.datasources;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import fr.cnes.regards.framework.gson.annotation.GsonTypeAdapter;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeType;

/**
 * Converter to create an AbstractAttributeMapping from a Gson object.
 * This is used by plugins when a plugin parameter is a Collection<AbstractAttributeMapping>.
 * During collection deserialization we need to be able to transform an AbstractAttributeMapping to the good implementation
 * by reading the attribute type.
 *
 * @author sbinda
 */
@GsonTypeAdapter(adapted = AbstractAttributeMapping.class)
public class AbstractAttributeMappingConverter extends TypeAdapter<AbstractAttributeMapping> {

    @Override
    public void write(JsonWriter out, AbstractAttributeMapping value) throws IOException {
        throw new RuntimeException("Unable to write an abstract class to json format");
    }

    @Override
    public AbstractAttributeMapping read(JsonReader in) throws IOException {
        AttributeMappingEnum type = null;
        Map<String, String> elements = Maps.newHashMap();
        in.beginObject();

        while (in.hasNext()) {
            String name = in.nextName();
            if (name.equals(AbstractAttributeMapping.ATTRIBUTE_TYPE)) {
                type = AttributeMappingEnum.valueOf(in.nextString());
            } else {
                elements.put(name, in.nextString());
            }
        }

        if (type == null) {
            throw new RuntimeException("Invalid unknown attribute mapping type");
        } else {
            switch (type) {
                case DYNAMIC:
                    return new DynamicAttributeMapping(elements.get("name"), elements.get("namespace"),
                            AttributeType.valueOf(elements.get("type")), elements.get("nameDS"));
                case STATIC:
                    return new StaticAttributeMapping(elements.get("name"), AttributeType.valueOf(elements.get("type")),
                            elements.get("nameDS"));
                default:
                    throw new RuntimeException(String.format("Invalid attribute mapping type %s", type.toString()));
            }
        }
    }

}
