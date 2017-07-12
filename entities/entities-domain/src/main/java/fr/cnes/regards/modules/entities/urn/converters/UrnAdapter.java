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
package fr.cnes.regards.modules.entities.urn.converters;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import fr.cnes.regards.framework.gson.annotation.GsonTypeAdapter;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * This adapter is used by Gson via @JsonAdapter(UrnAdapter.class) on UniformResourceName class
 * @author Sylvain Vissiere-Guerinet
 */
@GsonTypeAdapter(adapted = UniformResourceName.class)
public class UrnAdapter extends TypeAdapter<UniformResourceName> {

    @Override
    public UniformResourceName read(JsonReader pArg0) throws IOException {
        return UniformResourceName.fromString(pArg0.nextString());
    }

    @Override
    public void write(JsonWriter pArg0, UniformResourceName pArg1) throws IOException {
        pArg0.value(pArg1.toString());
    }

}
