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
package fr.cnes.regards.modules.datasources.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import fr.cnes.regards.modules.datasources.domain.AbstractAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.ModelMappingAdapterFactory;

/**
 * Test mapping deserialization
 * @author Marc Sordi
 *
 */
@Ignore
public class MappingTest {

    @Test
    public void test() throws UnsupportedEncodingException, IOException {

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new ModelMappingAdapterFactory());
        Gson gson = builder.create();

        Type type = new com.google.gson.reflect.TypeToken<List<AbstractAttributeMapping>>() {
        }.getType();

        try (JsonReader reader = new JsonReader(
                new InputStreamReader(this.getClass().getResourceAsStream("mapping.json"), "UTF-8"))) {
            List<AbstractAttributeMapping> aam = gson.fromJson(reader, type);
            Assert.assertNotNull(aam);
        }
    }
}
