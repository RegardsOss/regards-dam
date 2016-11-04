/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// CHECKSTYLE:OFF
/**
 *
 * Code inspired from https://github.com/google/gson/blob/master/extras/src/main/java/com/google/gson/typeadapters/RuntimeTypeAdapterFactory.java
 */
// CHECKSTYLE:ON
package fr.cnes.regards.modules.models.domain.adapters.gson;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Generic polymorphic adapter factory. This adapter is based on a discriminator field to explicitly map an entity to
 * its implementation.<br/>
 * A hierarchy of class has to embed a discriminator attribute to be able to use this adapter.
 *
 * @author Marc Sordi
 *
 * @param <E>
 *            entity base type
 */
public class PolymorphicTypeAdapterFactory<E> implements TypeAdapterFactory {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PolymorphicTypeAdapterFactory.class);

    /**
     * Base polymorphic class
     */
    private final Class<E> baseType;

    /**
     * JSON discriminator field name
     */
    private final String discriminatorFieldName;

    /**
     * Map discriminator value to its corresponding explicit type
     */
    private final Map<String, Class<?>> discriminatorToSubtype = new LinkedHashMap<String, Class<?>>();

    protected PolymorphicTypeAdapterFactory(Class<E> pBaseType, String pDiscriminatorFieldName) {
        this.baseType = pBaseType;
        this.discriminatorFieldName = pDiscriminatorFieldName;
        LOG.info("Managing polymorphic adapter for class {} and discriminator field {}", baseType.getName(),
                 discriminatorFieldName);
    }

    /**
     * Register a mapping between a field value and an explicit type
     *
     * @param pType
     *            type
     * @param pDiscriminatorFieldValue
     *            field value
     */
    protected void registerSubtype(Class<? extends E> pType, String pDiscriminatorFieldValue) {
        Assert.notNull(pType);
        Assert.notNull(pDiscriminatorFieldValue);

        // Check if map not already contains value
        if (discriminatorToSubtype.containsKey(pDiscriminatorFieldValue)) {
            throw new IllegalArgumentException("Discrimator field value must be unique");
        }
        discriminatorToSubtype.put(pDiscriminatorFieldValue, pType);
    }

    /**
     * Register a mapping between an enumeration and an explicit type.
     *
     * @param pType
     *            type
     * @param pEnum
     *            enum value
     */
    protected void registerSubtype(Class<? extends E> pType, Enum<?> pEnum) {
        registerSubtype(pType, pEnum.toString());
    }

    // CHECKSTYLE:OFF
    @Override
    public <T> TypeAdapter<T> create(Gson pGson, TypeToken<T> pType) {
        if (pType.getRawType() != baseType) {
            return null;
        }

        final Map<String, TypeAdapter<?>> discriminatorToDelegate = new LinkedHashMap<String, TypeAdapter<?>>();
        final Map<Class<?>, TypeAdapter<?>> subtypeToDelegate = new LinkedHashMap<Class<?>, TypeAdapter<?>>();

        /**
         * Register TypeAdapter delegation mapping from discriminator and type
         */
        for (Map.Entry<String, Class<?>> mapping : discriminatorToSubtype.entrySet()) {
            final TypeAdapter<?> delegate = pGson.getDelegateAdapter(this, TypeToken.get(mapping.getValue()));
            discriminatorToDelegate.put(mapping.getKey(), delegate);
            subtypeToDelegate.put(mapping.getValue(), delegate);
        }

        return new TypeAdapter<T>() {

            /**
             * Delegate writing to default type adapter
             */
            @Override
            public void write(JsonWriter pOut, T pValue) throws IOException {

                final Class<?> srcType = pValue.getClass();
                // String label = subtypeToLabel.get(srcType);
                @SuppressWarnings("unchecked") // registration requires that subtype extends base type
                final TypeAdapter<T> delegate = (TypeAdapter<T>) subtypeToDelegate.get(srcType);
                if (delegate == null) {
                    throw new JsonParseException(
                            "Cannot serialize " + srcType.getName() + "; did you forget to register a subtype?");
                }
                Streams.write(delegate.toJsonTree(pValue), pOut);
            }

            /**
             * Delegate reading to type adapter mapped to extracted discriminator value
             */
            @Override
            public T read(JsonReader pIn) throws IOException {
                final JsonElement jsonElement = Streams.parse(pIn);
                final JsonElement discriminatorEl = jsonElement.getAsJsonObject().get(discriminatorFieldName);
                if (discriminatorEl == null) {
                    throw new JsonParseException("Cannot deserialize " + baseType
                            + " because it does not define a field named " + discriminatorFieldName);
                }
                final String discriminator = discriminatorEl.getAsString();
                @SuppressWarnings("unchecked") // registration requires that subtype extends T
                final TypeAdapter<T> delegate = (TypeAdapter<T>) discriminatorToDelegate.get(discriminator);
                if (delegate == null) {
                    throw new JsonParseException("Cannot deserialize " + baseType + " subtype named " + discriminator
                            + "; did you forget to register a subtype?");
                }
                return delegate.fromJsonTree(jsonElement);
            }
        }.nullSafe();
    }
    // CHECKSTYLE:ON
}
