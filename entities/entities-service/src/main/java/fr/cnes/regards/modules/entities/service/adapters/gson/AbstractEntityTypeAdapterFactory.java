/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.adapters.gson;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import fr.cnes.regards.modules.entities.domain.AbstractEntity;

/**
 * Allows to customize GSON (de)serialization for an entity inheriting from {@link AbstractEntity}.
 *
 * @author Marc Sordi
 *
 * @param <T>
 *            concrete entity class
 */
public abstract class AbstractEntityTypeAdapterFactory<T> implements TypeAdapterFactory {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEntityTypeAdapterFactory.class);

    /**
     * attributes
     */
    private static final String ATTRIBUTE_FIELD_NAME = "attributes";

    /**
     * Entity to customize
     */
    private final Class<T> entityClass;

    public AbstractEntityTypeAdapterFactory(Class<T> pEntityClass) {
        this.entityClass = pEntityClass;
        LOGGER.debug("Entity type adapter factory created for {}", pEntityClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> TypeAdapter<E> create(Gson pGson, TypeToken<E> pType) {
        final Class<? super E> requestedType = pType.getRawType();
        if (!AbstractEntity.class.isAssignableFrom(requestedType) || (requestedType != entityClass)) {
            return null;
        }

        return (TypeAdapter<E>) customTypeAdapter(pGson, (TypeToken<T>) pType);
    }

    /**
     * Create custom {@link TypeAdapter}
     *
     * @param pGson
     *            GSON instance
     * @param pType
     *            linked type
     * @return custom {@link TypeAdapter}
     */
    private TypeAdapter<T> customTypeAdapter(Gson pGson, TypeToken<T> pType) {
        final TypeAdapter<T> delegate = pGson.getDelegateAdapter(this, pType);
        final TypeAdapter<JsonElement> elementAdapter = pGson.getAdapter(JsonElement.class);
        return new TypeAdapter<T>() {

            @Override
            public void write(JsonWriter pOut, T pValue) throws IOException {
                JsonElement tree = delegate.toJsonTree(pValue);
                beforeWrite(pValue, tree);
                elementAdapter.write(pOut, tree);
            }

            @Override
            public T read(JsonReader pIn) throws IOException {
                JsonElement tree = elementAdapter.read(pIn);
                beforeRead(tree);
                return delegate.fromJsonTree(tree);
            }
        };
    }

    /**
     * Hook for manipulating serialization
     *
     * @param pSource
     *            {@link AbstractEntity} to write
     * @param pJsonElement
     *            JSON representation of {@link AbstractEntity}
     */
    protected void beforeWrite(T pSource, JsonElement pJsonElement) { // NOSONAR
        LOGGER.debug("Before write");

        if (!pJsonElement.isJsonObject()) {
            throw objectRequiredException(pJsonElement);
        }

        JsonObject entity = pJsonElement.getAsJsonObject();
        JsonElement attEl = entity.get(ATTRIBUTE_FIELD_NAME);
        if ((attEl != null) && !attEl.isJsonNull()) {
            if (attEl.isJsonArray()) {
                entity.add(ATTRIBUTE_FIELD_NAME, mergeArray(attEl.getAsJsonArray()));
            } else {
                String errorMessage = String.format("Unexpected JSON element %s. Array required.",
                                                    pJsonElement.toString());
                LOGGER.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
        }
    }

    /**
     * Hook to manipulate deserialization
     *
     * @param pJsonElement
     *            JSON representation of {@link AbstractEntity}
     */
    protected void beforeRead(JsonElement pJsonElement) {
        LOGGER.debug("Before read");

        if (!pJsonElement.isJsonObject()) {
            throw objectRequiredException(pJsonElement);
        }

        JsonObject entity = pJsonElement.getAsJsonObject();
        JsonElement attEl = entity.get(ATTRIBUTE_FIELD_NAME);
        if (attEl != null) {
            if (attEl.isJsonObject()) {
                entity.add(ATTRIBUTE_FIELD_NAME, restoreArray(attEl.getAsJsonObject()));
            } else {
                throw objectRequiredException(attEl);
            }
        }

    }

    /**
     * Merge {@link JsonArray} flattening elements in a single {@link JsonObject}
     *
     * @param pJsonArray
     *            {@link JsonArray} to flatten
     * @return {@link JsonObject}
     */
    private JsonObject mergeArray(JsonArray pJsonArray) {
        JsonObject mergedObject = new JsonObject();
        Iterator<JsonElement> nestedIter = pJsonArray.iterator();
        while (nestedIter.hasNext()) {
            JsonElement nested = nestedIter.next();
            if (nested.isJsonObject()) {
                JsonObject nestedObject = nested.getAsJsonObject();
                for (Map.Entry<String, JsonElement> e : nestedObject.entrySet()) {
                    mergedObject.add(e.getKey(), e.getValue());
                }
            } else {
                throw objectRequiredException(nested);
            }
        }
        return mergedObject;
    }

    /**
     * Restore {@link JsonArray} from flattened {@link JsonObject} elements (reverse merge)
     *
     * @param pJsonObject
     *            {@link JsonObject} to transform
     * @return {@link JsonArray}
     */
    private JsonArray restoreArray(JsonObject pJsonObject) {
        JsonArray restoredArray = new JsonArray();
        for (Map.Entry<String, JsonElement> nestedEntry : pJsonObject.entrySet()) {
            JsonObject nestedObject = new JsonObject();
            nestedObject.add(nestedEntry.getKey(), nestedEntry.getValue());
            restoredArray.add(nestedObject);
        }
        return restoredArray;
    }

    private IllegalArgumentException objectRequiredException(JsonElement pJsonElement) {
        String errorMessage = String.format("Unexpected JSON element %s. Object required.", pJsonElement.toString());
        LOGGER.error(errorMessage);
        return new IllegalArgumentException(errorMessage);
    }
}
